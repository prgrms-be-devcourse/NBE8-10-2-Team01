package com.plog.domain.image.controller;

import com.plog.domain.image.dto.ProfileImageUploadRes;
import com.plog.domain.image.service.ProfileImageService;
import com.plog.global.security.JwtUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
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
class ProfileImageControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ProfileImageService profileImageService;

    @MockitoBean
    private JwtUtils jwtUtils;

    @Test
    @DisplayName("프로필 이미지 업로드 성공 시 변경된 URL을 반환한다")
    void uploadProfileImageSuccess() throws Exception {
        // [Given]
        Long memberId = 1L;
        MockMultipartFile file = new MockMultipartFile("file", "profile.jpg", "image/jpeg", "data".getBytes());
        
        ProfileImageUploadRes mockResponse = new ProfileImageUploadRes(memberId, "http://minio/new-profile.jpg");
        given(profileImageService.uploadProfileImage(eq(memberId), any())).willReturn(mockResponse);

        // [When & Then]
        mvc.perform(
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
        mvc.perform(get("/api/members/{memberId}/profile-image", memberId))
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
        mvc.perform(delete("/api/members/{memberId}/profile-image", memberId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("프로필 이미지가 삭제되었습니다."));
    }
}
