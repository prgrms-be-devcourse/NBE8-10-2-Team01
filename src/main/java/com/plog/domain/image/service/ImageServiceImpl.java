package com.plog.domain.image.service;

import com.plog.domain.image.dto.ImageUploadRes;
import com.plog.domain.image.entity.Image;
import com.plog.domain.image.repository.ImageRepository;
import com.plog.global.exception.errorCode.ImageErrorCode;
import com.plog.global.exception.exceptions.ImageException;
import com.plog.global.minio.storage.ObjectStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 이미지 업로드 및 메타데이터 관리를 담당하는 서비스 구현체입니다.
 * <p>
 * {@link ImageService} 인터페이스를 구현하여 실제 비즈니스 로직을 수행합니다.
 * {@link ObjectStorage}를 통해 물리적 파일을 저장하고, {@link ImageRepository}를 통해 DB에 메타데이터를 저장합니다.
 * 모든 업로드 작업은 {@code @Transactional} 안에서 원자적으로 수행됩니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@link ImageService} 인터페이스를 구현합니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@code ImageServiceImpl(ObjectStorage objectStorage, ImageRepository imageRepository)} <br>
 * 롬복의 {@code @RequiredArgsConstructor}를 통해 필요한 의존성을 주입받습니다. <br>
 *
 * <p><b>빈 관리:</b><br>
 * {@code @Service} 어노테이션을 통해 스프링 빈으로 등록됩니다. <br>
 * 클래스 레벨에는 적용되지 않았으나, 메서드 레벨에서 {@code @Transactional}을 통해 트랜잭션을 관리합니다.
 *
 * <p><b>외부 모듈:</b><br>
 * {@code ObjectStorage}: 추상화된 파일 저장소 인터페이스를 사용합니다 (구현체: MinioStorage). <br>
 * {@code ImageRepository}: JPA를 통해 DB와 통신합니다.
 *
 * @author Jaewon Ryu
 * @see
 * @since 2026-01-20
 */

@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {

    private final ObjectStorage objectStorage;
    private final ImageRepository imageRepository;

    @Override
    @Transactional
    public ImageUploadRes uploadImage(MultipartFile file) {
        if (file.isEmpty() || file.getOriginalFilename() == null) {
            throw new ImageException(
                    ImageErrorCode.EMPTY_FILE,
                    "[ImageServiceImpl#uploadImage] file is empty or filename is null",
                    "이미지 파일이 비어있거나 잘못된 요청입니다."
            );
        }
        String originalFileName = file.getOriginalFilename();
        String storedFileName = createStoredFileName(originalFileName);

        if (!isValidExtension(originalFileName)) {
            throw new ImageException(
                    ImageErrorCode.INVALID_FILE_EXTENSION,
                    "[ImageServiceImpl#uploadImage] invalid file extension request: " + originalFileName,
                    "지원하지 않는 파일 형식입니다. (jpg, jpeg, png, gif만 가능)"
            );
        }

        String accessUrl = objectStorage.upload(file, storedFileName);

        Image image = Image.builder()
                .originalName(originalFileName)
                .storedName(storedFileName)
                .accessUrl(accessUrl)
                .build();

        imageRepository.save(image);
        return new ImageUploadRes(List.of(accessUrl), List.of());

    }

    private String createStoredFileName(String originalFilename) {
        String uuid = UUID.randomUUID().toString();
        String ext = originalFilename.substring(originalFilename.lastIndexOf("."));
        return uuid + ext;
    }


    private boolean isValidExtension(String filename) {
        String lowerName = filename.toLowerCase();
        return lowerName.endsWith(".jpg") ||
                lowerName.endsWith(".jpeg") ||
                lowerName.endsWith(".png") ||
                lowerName.endsWith(".gif");
    }

    @Override
    @Transactional
    public ImageUploadRes uploadImages(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return new ImageUploadRes(List.of(), List.of());
        }

        List<String> successUrls = new ArrayList<>();
        List<String> failedFilenames = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                // uploadImage 결과에서 successUrls만 추출
                ImageUploadRes singleResult = uploadImage(file);
                successUrls.addAll(singleResult.successUrls());
            } catch (Exception e) {
                // 실패한 파일명 기록
                String filename = file.getOriginalFilename() != null
                        ? file.getOriginalFilename()
                        : "unknown-file";
                failedFilenames.add(filename);
            }
        }

        return new ImageUploadRes(successUrls, failedFilenames);
    }
}