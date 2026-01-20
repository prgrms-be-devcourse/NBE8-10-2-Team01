package com.plog.domain.image.service;

import com.plog.domain.image.entity.Image;
import com.plog.domain.image.repository.ImageRepository;
import com.plog.global.exception.errorCode.ImageErrorCode;
import com.plog.global.exception.exceptions.ImageException;
import com.plog.global.minio.storage.ObjectStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * 코드에 대한 전체적인 역할을 적습니다.
 * <p>
 * 코드에 대한 작동 원리 등을 적습니다.
 *
 * <p><b>상속 정보:</b><br>
 * 상속 정보를 적습니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@code ImageServiceImpl(String example)} <br>
 * 주요 생성자와 그 매개변수에 대한 설명을 적습니다. <br>
 *
 * <p><b>빈 관리:</b><br>
 * 필요 시 빈 관리에 대한 내용을 적습니다.
 *
 * <p><b>외부 모듈:</b><br>
 * 필요 시 외부 모듈에 대한 내용을 적습니다.
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
    public String uploadImage(MultipartFile file) {
        if (file.isEmpty() || file.getOriginalFilename() == null) {
            throw new ImageException(ImageErrorCode.EMPTY_FILE);
        }
        String originalFileName = file.getOriginalFilename();
        String storedFileName = createStoredFileName(originalFileName);

        if (!isValidExtension(originalFileName)) {
            throw new ImageException(ImageErrorCode.INVALID_FILE_EXTENSION);
        }

        String accessUrl = objectStorage.upload(file, storedFileName);

        Image image = Image.builder()
                .originalName(originalFileName)
                .storedName(storedFileName)
                .accessUrl(accessUrl)
                .build();

        imageRepository.save(image);
        return accessUrl;

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
    public List<String> uploadImages(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return List.of();
        }

        return files.stream()
                .map(this::uploadImage)
                .toList();
    }
}
