package com.plog.domain.image.service;

import com.plog.domain.image.repository.ImageRepository;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * ImageService의 비즈니스 로직을 검증하는 단위 테스트(Unit Test)입니다.
 * <p>
 * 스프링 컨텍스트를 로드하지 않고({@code @SpringBootTest} 제외),
 * {@code @ExtendWith(MockitoExtension.class)}를 사용하여 가볍고 빠르게 동작합니다.
 * 외부 의존성(MinIO 스토리지, DB Repository)은 Mock 객체로 대체하여,
 * 순수하게 서비스 계층의 파일명 변환 로직, 확장자 검사, 예외 처리 등을 검증합니다.
 *
 * <p><b>테스트 환경:</b><br>
 * JUnit 5, Mockito, AssertJ 사용
 *
 * <p><b>주요 검증 포인트:</b><br>
 * 1. 파일 업로드 시 UUID 기반 파일명 생성 여부 확인 <br>
 * 2. 지원하지 않는 파일 확장자 및 빈 파일에 대한 예외 처리 검증 <br>
 * 3. 다중 파일 업로드 시 반복 호출 로직 검증
 *
 * @author Jaewon Ryu
 * @see ImageServiceImpl
 * @since 2026-01-21
 */

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class ImageServiceTest {

    @InjectMocks
    private ImageServiceImpl imageService; // 구현체를 주입 (인터페이스가 아닌 구현 클래스명 확인 필요)

    @Mock
    private ObjectStorage objectStorage;

    @Mock
    private ImageRepository imageRepository;

    @Test
    @DisplayName("이미지 업로드 시 UUID가 적용된 고유한 파일명으로 저장소에 전달된다")
    void uploadImageSuccess() {
        // [Given]
        String originalFilename = "test-image.jpg";
        MockMultipartFile file = new MockMultipartFile(
                "file", originalFilename, "image/jpeg", "content".getBytes()
        );

        given(objectStorage.upload(any(MultipartFile.class), anyString()))
                .willReturn("http://minio-url/bucket/uuid-filename.jpg");

        // [When]
        imageService.uploadImage(file);

        // [Then] - 팀원분 스타일: ArgumentCaptor 사용
        // objectStorage.upload(InputStream, String filename)의 두 번째 인자인 filename을 캡처
        ArgumentCaptor<String> filenameCaptor = ArgumentCaptor.forClass(String.class);

        verify(objectStorage).upload(any(MultipartFile.class), filenameCaptor.capture());

        String savedFilename = filenameCaptor.getValue();

        // 검증 1: 원본 파일명이 그대로 쓰이지 않고 변환되었는가? (UUID 적용 확인)
        assertThat(savedFilename).isNotEqualTo(originalFilename);
        // 검증 2: 확장자는 유지되었는가?
        assertThat(savedFilename).endsWith(".jpg");
    }

    @Test
    @DisplayName("다중 이미지 업로드 시 각 파일마다 별도의 저장소 호출이 발생한다")
    void uploadImagesSuccess() {
        // [Given]
        List<MultipartFile> files = List.of(
                new MockMultipartFile("f1", "a.png", "image/png", "d1".getBytes()),
                new MockMultipartFile("f2", "b.jpg", "image/jpeg", "d2".getBytes())
        );

        given(objectStorage.upload(any(MultipartFile.class), anyString()))
                .willReturn("http://mock-url/img");

        // [When]
        List<String> results = imageService.uploadImages(files);

        // [Then]
        assertThat(results).hasSize(2);

        // 호출 횟수 검증
        verify(objectStorage, times(2)).upload(any(MultipartFile.class), anyString());
    }

    @Test
    @DisplayName("지원하지 않는 확장자는 예외가 발생한다")
    void uploadImageInvalidExtension() {
        // [Given]
        MockMultipartFile txtFile = new MockMultipartFile(
                "file", "danger.exe", "application/x-msdownload", "content".getBytes()
        );

        // [When & Then]
        assertThatThrownBy(() -> imageService.uploadImage(txtFile))
                .isInstanceOf(ImageException.class)
                .hasFieldOrPropertyWithValue("errorCode", ImageErrorCode.INVALID_FILE_EXTENSION);
    }

    @Test
    @DisplayName("빈 파일 업로드 시 예외가 발생한다")
    void uploadImageEmptyFile() {
        // [Given]
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "empty.jpg", "image/jpeg", new byte[0]
        );

        // [When & Then]
        assertThatThrownBy(() -> imageService.uploadImage(emptyFile))
                .isInstanceOf(ImageException.class);
    }
}