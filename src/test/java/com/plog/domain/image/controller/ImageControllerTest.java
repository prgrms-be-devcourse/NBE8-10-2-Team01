package com.plog.domain.image.controller;

import com.plog.domain.image.service.ImageService;
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
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ImageController의 웹 계층 단위 테스트입니다.
 * <p>
 * <b>테스트 범위:</b> Controller Layer (Service, Repository 제외) <br>
 * <b>검증 대상:</b> API 요청 매핑, 파라미터 유효성 검사, 응답 Status 및 Body 포맷 <br>
 * <b>Mocking:</b> {@code ImageService}는 Mock 객체로 대체하여 비즈니스 로직과 격리합니다.
 *
 * @see ImageController
 */

@WebMvcTest(ImageController.class)
@ActiveProfiles("test")
class ImageControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ImageService imageService;

    @MockitoBean
    private JwtUtils jwtUtils;

    @Test
    @DisplayName("이미지 업로드 성공 시 URL을 포함한 응답을 반환한다")
    void uploadImageSuccess() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", "test data".getBytes()
        );
        String mockUrl = "http://minio/bucket/images/uuid.jpg";

        given(imageService.uploadImage(any())).willReturn(mockUrl);


        ResultActions resultActions = mvc
                .perform(
                        multipart("/api/images")
                                .file(file)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                )
                .andDo(print());


        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.imageUrls[0]").value(mockUrl)) // List 형태 확인
                .andExpect(jsonPath("$.message").value("이미지 업로드 성공"));
    }

    @Test
    @DisplayName("다중 이미지 업로드 성공 시 URL 리스트를 반환한다")
    void uploadImagesSuccess() throws Exception {
        MockMultipartFile file1 = new MockMultipartFile(
                "files", "test1.jpg", "image/jpeg", "data1".getBytes()
        );
        MockMultipartFile file2 = new MockMultipartFile(
                "files", "test2.jpg", "image/jpeg", "data2".getBytes()
        );

        List<String> mockUrls = List.of(
                "http://minio/bucket/images/uuid1.jpg",
                "http://minio/bucket/images/uuid2.jpg"
        );

        given(imageService.uploadImages(anyList())).willReturn(mockUrls);

        ResultActions resultActions = mvc
                .perform(
                        multipart("/api/images/bulk")
                                .file(file1)
                                .file(file2)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                )
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.imageUrls").isArray())
                .andExpect(jsonPath("$.data.imageUrls[0]").value(mockUrls.get(0)))
                .andExpect(jsonPath("$.data.imageUrls[1]").value(mockUrls.get(1)))
                .andExpect(jsonPath("$.message").value("다중 이미지 업로드 성공"));
    }

    @Test
    @DisplayName("지원하지 않는 파일 형식이면 서비스 예외를 400으로 처리한다")
    void uploadImageInvalidExtension() throws Exception {

        MockMultipartFile txtFile = new MockMultipartFile(
                "file", "test.txt", "text/plain", "content".getBytes()
        );


        given(imageService.uploadImage(any()))
                .willThrow(new com.plog.global.exception.exceptions.ImageException(
                        com.plog.global.exception.errorCode.ImageErrorCode.INVALID_FILE_EXTENSION,
                        "지원하지 않는 파일 형식입니다."
                ));


        ResultActions resultActions = mvc
                .perform(
                        multipart("/api/images")
                                .file(txtFile)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                )
                .andDo(print());


        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("fail"))
                .andExpect(jsonPath("$.message").value("지원하지 않는 파일 형식입니다."));
    }

    @Test
    @DisplayName("파일 없이 요청하면 400 Bad Request가 발생한다")
    void uploadImageWithoutFile() throws Exception {

        ResultActions resultActions = mvc
                .perform(
                        multipart("/api/images")

                                .contentType(MediaType.MULTIPART_FORM_DATA)
                )
                .andDo(print());


        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Missing request part"));
    }
}