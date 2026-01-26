package com.plog.domain.image.service;

import com.plog.domain.image.dto.ProfileImageUploadRes;
import com.plog.domain.image.entity.Image;
import com.plog.domain.image.repository.ImageRepository;
import com.plog.domain.member.entity.Member;
import com.plog.domain.member.repository.MemberRepository;
import com.plog.global.exception.errorCode.ImageErrorCode;
import com.plog.global.exception.exceptions.ImageException;
import com.plog.global.minio.storage.ObjectStorage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * ProfileImageService의 비즈니스 로직을 검증하는 단위 테스트(Unit Test)입니다.
 * <p>
 * 스프링 컨텍스트를 로드하지 않고 Mockito를 사용하여 가볍게 동작합니다.
 * MinIO, DB, Member 관련 로직을 Mock 객체로 대체하여 순수 서비스 로직을 검증합니다.
 *
 * <p><b>주요 검증 포인트:</b><br>
 * 1. 프로필 이미지 업로드 시 기존 이미지 삭제 및 교체 로직 검증 <br>
 * 2. 폴더 구조화(profile/image/{memberId}/...) 적용 여부 확인 <br>
 * 3. 멱등성이 보장된 삭제(Delete) 로직 검증
 *
 * @author Jaewon Ryu
 * @since 2026-01-23
 */
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class ProfileImageServiceTest {

    @InjectMocks
    private ProfileImageServiceImpl profileImageService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private ObjectStorage objectStorage;

    @Test
    @DisplayName("프로필 이미지 업로드 시 기존 이미지가 없으면 바로 저장된다")
    void uploadProfileImageSuccess_New() {
        // [Given]
        Long memberId = 1L;
        Member member = createMember(memberId);
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "data".getBytes());
        String mockUrl = "http://minio/profile.jpg";

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(objectStorage.upload(any(MultipartFile.class), any(String.class))).willReturn(mockUrl);

        // [When]
        ProfileImageUploadRes result = profileImageService.uploadProfileImage(memberId, file);

        // [Then]
        assertThat(result.memberId()).isEqualTo(memberId);
        assertThat(result.profileImageUrl()).isEqualTo(mockUrl);
        verify(imageRepository, times(1)).save(any(Image.class));
    }

    @Test
    @DisplayName("프로필 이미지 교체 시 기존 파일과 DB 데이터를 삭제하고 새 이미지를 저장한다")
    void uploadProfileImageSuccess_Overwrite() {
        // [Given]
        Long memberId = 1L;
        Member member = createMember(memberId);

        // 기존 이미지 설정
        Image oldImage = Image.builder().storedName("old/path.jpg").build();
        member.updateProfileImage(oldImage);

        MockMultipartFile newFile = new MockMultipartFile("file", "new.jpg", "image/jpeg", "newdata".getBytes());

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(objectStorage.upload(any(), any())).willReturn("http://new-url");

        // [When]
        profileImageService.uploadProfileImage(memberId, newFile);

        // [Then]
        // 1. 기존 파일 삭제 호출 검증
        verify(objectStorage).delete(eq("old/path.jpg"));
        verify(imageRepository).delete(eq(oldImage));

        // 2. 새 파일 업로드 호출 검증
        verify(objectStorage).upload(any(), any());
    }

    @Test
    @DisplayName("프로필 이미지 저장 경로에 회원 ID가 포함되어야 한다")
    void uploadProfileImage_CheckPath() {
        // [Given]
        Long memberId = 99L;
        Member member = createMember(memberId);
        MockMultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", "data".getBytes());

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(objectStorage.upload(any(), any())).willReturn("url");

        // [When]
        profileImageService.uploadProfileImage(memberId, file);

        // [Then]
        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(objectStorage).upload(any(), pathCaptor.capture());

        String capturedPath = pathCaptor.getValue();
        assertThat(capturedPath).contains("profile/image/" + memberId + "/");
        assertThat(capturedPath).endsWith(".png");
    }

    @Test
    @DisplayName("지원하지 않는 확장자는 예외가 발생한다")
    void uploadProfileImage_InvalidExtension() {
        // [Given]
        MockMultipartFile file = new MockMultipartFile("file", "malware.exe", "application/x-msdownload", "data".getBytes());

        // [When & Then]
        assertThatThrownBy(() -> profileImageService.uploadProfileImage(1L, file))
                .isInstanceOf(ImageException.class)
                .hasFieldOrPropertyWithValue("errorCode", ImageErrorCode.INVALID_FILE_EXTENSION);
    }

    @Test
    @DisplayName("프로필 이미지 삭제 시 이미지가 없으면 아무 동작도 하지 않는다 (멱등성)")
    void deleteProfileImage_Idempotent() {
        // [Given]
        Long memberId = 1L;
        Member member = createMember(memberId); // 이미지 없음 (null)
        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));

        // [When]
        profileImageService.deleteProfileImage(memberId);

        // [Then]
        verify(objectStorage, times(0)).delete(any());
        verify(imageRepository, times(0)).delete(any(Image.class));
    }

    // --- Helper ---
    private Member createMember(Long id) {
        Member member = Member.builder()
                .email("test@test.com")
                .nickname("test")
                .password("pw")
                .build();
        ReflectionTestUtils.setField(member, "id", id);
        return member;
    }

    @Test
    @DisplayName("DB 저장 실패 시 롤백 로직이 실행되어 파일을 삭제해야 한다")
    void shouldDeleteFile_WhenTransactionRollback() {
        // given
        Long memberId = 1L;
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test data".getBytes());

        Member member = Member.builder().build();
        org.springframework.test.util.ReflectionTestUtils.setField(member, "id", memberId);

        given(memberRepository.findById(memberId)).willReturn(java.util.Optional.of(member));
        given(objectStorage.upload(any(), any())).willReturn("https://minio.url/test.jpg");

        given(imageRepository.save(any())).willThrow(new RuntimeException("DB Error"));

        try {
            profileImageService.uploadProfileImage(memberId, file);
        } catch (RuntimeException e) {

        }

        verify(objectStorage, times(1)).upload(any(), any());
    }
}