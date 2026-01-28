package com.plog.domain.image.controller;

import com.plog.domain.image.dto.ProfileImageUploadRes;
import com.plog.domain.image.service.ProfileImageService;
import com.plog.global.security.SecurityUser;
import com.plog.testUtil.SecurityTestConfig;
import com.plog.testUtil.WebMvcTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ProfileImageController의 웹 계층 단위 테스트입니다.
 * <p>
 * <b>테스트 범위:</b> Controller Layer <br>
 * <b>검증 대상:</b> API URL 매핑, 파라미터(MemberId) 바인딩, 응답 포맷 <br>
 *
 * @see ProfileImageController
 */
@WebMvcTest(ProfileImageController.class)
@ActiveProfiles("test")
@Import(SecurityTestConfig.class) // [추가] SecurityTestConfig 적용
class ProfileImageControllerTest extends WebMvcTestSupport {

    @MockitoBean
    private ProfileImageService profileImageService;

    // [추가] 테스트 실행 전 가짜 인증 정보 주입
    @BeforeEach
    void setUpUser() {
        // 1. SecurityUser를 Mock으로 생성 (생성자 로직 회피)
        SecurityUser mockUser = mock(SecurityUser.class);

        // 2. 인증 토큰 생성
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(mockUser, null, null);

        // 3. SecurityContext에 설정 (컨트롤러의 @AuthenticationPrincipal이 이 정보를 읽어감)
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    @DisplayName("프로필 이미지 업로드 성공 시 변경된 URL을 반환한다")
    void uploadProfileImageSuccess() throws Exception {
        // [Given]
        Long memberId = 1L;
        MockMultipartFile file = new MockMultipartFile("file", "profile.jpg", "image/jpeg", "data".getBytes());

        ProfileImageUploadRes mockResponse = new ProfileImageUploadRes(memberId, "http://minio/new-profile.jpg");
        given(profileImageService.uploadProfileImage(eq(memberId), any())).willReturn(mockResponse);

        // [When & Then]
        mockMvc.perform(
                        multipart("/api/members/{memberId}/profile-image", memberId)
                                .file(file)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.memberId").value(memberId))
                .andExpect(jsonPath("$.data.profileImageUrl").value("http://minio/new-profile.jpg"));
    }

    @Test
    @DisplayName("프로필 이미지 조회 성공 시 URL을 반환한다")
    void getProfileImageSuccess() throws Exception {
        // [Given]
        Long memberId = 1L;
        ProfileImageUploadRes mockResponse = new ProfileImageUploadRes(memberId, "http://minio/profile.jpg");
        given(profileImageService.getProfileImage(memberId)).willReturn(mockResponse);

        // [When & Then]
        mockMvc.perform(get("/api/members/{memberId}/profile-image", memberId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.profileImageUrl").value("http://minio/profile.jpg"));
    }

    @Test
    @DisplayName("프로필 이미지 삭제 요청이 성공하면 200 OK를 반환한다")
    void deleteProfileImageSuccess() throws Exception {
        // [Given]
        Long memberId = 1L;
        // void 메서드는 별도 given 설정 불필요 (기본적으로 아무 동작 안 함)

        // [When & Then]
        mockMvc.perform(delete("/api/members/{memberId}/profile-image", memberId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("프로필 이미지가 삭제되었습니다."));
    }
}
