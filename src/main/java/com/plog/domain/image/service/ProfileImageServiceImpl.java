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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

import static com.plog.global.exception.errorCode.AuthErrorCode.USER_NOT_FOUND;

/**
 * 프로필 이미지 관리를 담당하는 서비스 구현체입니다.
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
        // 0. 파일 검증
        validateFile(file);

        // 1. 회원 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new AuthException(USER_NOT_FOUND,
                        "[ProfileImageServiceImpl#uploadProfileImage] can't find user by id",
                        "존재하지 않는 사용자입니다."));

        // 2. 기존 프로필 이미지 삭제 (MinIO + DB)
        deleteOldProfileImage(member);

        // 3. 파일명 생성 (폴더 구조화: profile/image/{memberId}/...)
        String originalFilename = file.getOriginalFilename();
        String storedName = createStoredFileName(memberId, originalFilename);

        // 4. MinIO 업로드
        String accessUrl = objectStorage.upload(file, storedName);

        // 5. Image 엔티티 생성 및 저장
        Image newImage = Image.builder()
                .originalName(originalFilename)
                .storedName(storedName)
                .accessUrl(accessUrl)
                .build();

        imageRepository.save(newImage);

        // 6. Member 연결
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

    // --- Helper Methods ---

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

            // MinIO 삭제 (에러 발생해도 무시하고 DB는 삭제 진행)
            try {
                objectStorage.delete(oldImage.getStoredName());
            } catch (Exception ignored) {
                // 로그를 안 쓰기로 했으므로 예외 무시
            }

            // DB 관계 끊기 및 엔티티 삭제
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

        // 기존 이미지가 없으면 그냥 조용히 리턴 (에러 아님)
        if (member.getProfileImage() == null) {
            return;
        }

        // 기존 로직 재활용 (Helper Method 만들어뒀으니 편함!)
        deleteOldProfileImage(member);
    }
}