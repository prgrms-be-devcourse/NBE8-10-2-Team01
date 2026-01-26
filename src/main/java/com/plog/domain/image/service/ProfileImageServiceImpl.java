package com.plog.domain.image.service;

import com.plog.domain.image.dto.ProfileImageUploadRes;
import com.plog.domain.image.entity.Image;
import com.plog.domain.image.repository.ImageRepository;
import com.plog.domain.member.entity.Member;
import com.plog.domain.member.repository.MemberRepository;
import com.plog.global.exception.errorCode.ImageErrorCode;
import com.plog.global.exception.exceptions.AuthException;
import com.plog.global.exception.exceptions.ImageException;
import com.plog.global.minio.storage.ObjectStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.support.TransactionSynchronization;

import java.util.UUID;

import static com.plog.global.exception.errorCode.AuthErrorCode.USER_NOT_FOUND;

/**
 * 프로필 이미지 업로드 및 생명주기 관리를 담당하는 서비스 구현체입니다.
 * <p>
 * {@link ProfileImageService} 인터페이스를 구현하여 실제 비즈니스 로직을 수행합니다.
 * {@link ObjectStorage}를 통해 물리적 파일을 관리하고, {@link ImageRepository}와 {@link MemberRepository}를 통해
 * DB 메타데이터 및 회원과의 연관관계를 관리합니다.
 * 모든 변경 작업은 {@code @Transactional} 안에서 원자적으로 수행됩니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@link ProfileImageService} 인터페이스를 구현합니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@code ProfileImageServiceImpl(MemberRepository, ImageRepository, ObjectStorage)} <br>
 * 롬복의 {@code @RequiredArgsConstructor}를 통해 필요한 의존성을 주입받습니다. <br>
 *
 * <p><b>빈 관리:</b><br>
 * {@code @Service} 어노테이션을 통해 스프링 빈으로 등록됩니다. <br>
 * 클래스 레벨에는 적용되지 않았으나, 메서드 레벨에서 {@code @Transactional}을 통해 트랜잭션을 관리합니다.
 *
 * <p><b>외부 모듈:</b><br>
 * {@code ObjectStorage}: 추상화된 파일 저장소 인터페이스를 사용합니다 (구현체: MinioStorage). <br>
 * {@code ImageRepository}: JPA를 통해 DB와 통신합니다. <br>
 * {@code MemberRepository}: 회원 정보를 조회하고 업데이트합니다.
 *
 * @author Jaewon Ryu
 * @since 2026-01-23
 */
@Service
@RequiredArgsConstructor
public class ProfileImageServiceImpl implements ProfileImageService {

    private final MemberRepository memberRepository;
    private final ImageRepository imageRepository;
    private final ObjectStorage objectStorage;

    @Override
    @Transactional
    public ProfileImageUploadRes uploadProfileImage(Long memberId, MultipartFile file) {
        // 1. 사용자 검증 및 조회
        validateFile(file);
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new AuthException(USER_NOT_FOUND,
                        "존재하지 않는 사용자입니다."));

        deleteOldProfileImage(member);

        String originalFilename = file.getOriginalFilename();
        String storedName = createStoredFileName(memberId, originalFilename);

        String accessUrl = objectStorage.upload(file, storedName);

        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    // 트랜잭션이 롤백된 경우에만 실행
                    if (status == STATUS_ROLLED_BACK) {
                        try {
                            objectStorage.delete(storedName);
                        } catch (Exception e) {
                            // 파일 삭제 중 에러가 나더라도, 원래 발생한 DB 트랜잭션 에러를 덮어쓰지 않도록 예외 무시
                        }
                    }
                }
            });
        }

        // 5. DB 저장
        Image newImage = Image.builder()
                .originalName(originalFilename)
                .storedName(storedName)
                .accessUrl(accessUrl)
                .build();

        imageRepository.save(newImage);
        member.updateProfileImage(newImage);

        return ProfileImageUploadRes.from(member);
    }

    @Override
    @Transactional(readOnly = true)
    public ProfileImageUploadRes getProfileImage(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new AuthException(USER_NOT_FOUND,
                        "[ProfileImageServiceImpl#getProfileImage] can't find user by id",
                        "존재하지 않는 사용자입니다."));

        return ProfileImageUploadRes.from(member);
    }


    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty() || file.getOriginalFilename() == null) {
            throw new ImageException(
                    ImageErrorCode.EMPTY_FILE,
                    "[ProfileImageServiceImpl#uploadProfileImage] file is empty",
                    "이미지 파일이 비어있습니다."
            );
        }

        String filename = file.getOriginalFilename();
        if (!isValidExtension(filename)) {
            throw new ImageException(
                    ImageErrorCode.INVALID_FILE_EXTENSION,
                    "[ProfileImageServiceImpl#uploadProfileImage] invalid extension: " + filename,
                    "지원하지 않는 파일 형식입니다."
            );
        }
    }

    private boolean isValidExtension(String filename) {
        String lowerName = filename.toLowerCase();
        return lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg") ||
                lowerName.endsWith(".png") || lowerName.endsWith(".gif");
    }

    private String createStoredFileName(Long memberId, String originalFilename) {
        String uuid = UUID.randomUUID().toString();
        String ext = originalFilename.substring(originalFilename.lastIndexOf("."));
        return "profile/image/" + memberId + "/" + uuid + ext;
    }

    private void deleteOldProfileImage(Member member) {
        if (member.getProfileImage() != null) {
            Image oldImage = member.getProfileImage();

            try {
                objectStorage.delete(oldImage.getStoredName());
            } catch (Exception ignored) {
            }

            member.updateProfileImage(null);
            imageRepository.delete(oldImage);
        }
    }
    @Override
    @Transactional
    public void deleteProfileImage(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new AuthException(USER_NOT_FOUND,
                        "[ProfileImageServiceImpl#deleteProfileImage] can't find user",
                        "존재하지 않는 사용자입니다."));

        if (member.getProfileImage() == null) {
            return;
        }

        deleteOldProfileImage(member);
    }
}