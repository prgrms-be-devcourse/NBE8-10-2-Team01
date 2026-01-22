package com.plog.domain.image.service;


import com.plog.domain.image.entity.Image;
import com.plog.domain.image.dto.ImageUploadRes;
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
        String mockUrl = "http://minio-url/bucket/uuid-filename.jpg";
        given(objectStorage.upload(any(MultipartFile.class), anyString()))
                .willReturn(mockUrl);

        // [When]
        ImageUploadRes result = imageService.uploadImage(file);  // ← 타입 변경

        // [Then]
        assertThat(result.successUrls()).hasSize(1);
        assertThat(result.successUrls().get(0)).isEqualTo(mockUrl);
        assertThat(result.failedFilenames()).isEmpty();

        // 파일명 변환 검증 (기존 로직 유지)
        ArgumentCaptor<String> filenameCaptor = ArgumentCaptor.forClass(String.class);
        verify(objectStorage).upload(any(MultipartFile.class), filenameCaptor.capture());
        String savedFilename = filenameCaptor.getValue();
        assertThat(savedFilename).isNotEqualTo(originalFilename);
        assertThat(savedFilename).endsWith(".jpg");

        // DB 저장 검증
        verify(imageRepository).save(any(Image.class));
    }

    @Test
    @DisplayName("다중 이미지 업로드 성공 시 모든 파일의 URL을 반환한다")
    void uploadImagesSuccess() {
        // [Given]
        List<MultipartFile> files = List.of(
                new MockMultipartFile("f1", "a.png", "image/png", "d1".getBytes()),
                new MockMultipartFile("f2", "b.jpg", "image/jpeg", "d2".getBytes())
        );
        String mockUrl = "http://mock-url/img";
        given(objectStorage.upload(any(MultipartFile.class), anyString()))
                .willReturn(mockUrl);

        // [When]
        ImageUploadRes result = imageService.uploadImages(files);  // ← 타입 변경

        // [Then]
        assertThat(result.successUrls()).hasSize(2);
        assertThat(result.failedFilenames()).isEmpty();

        // 호출 횟수 검증 (기존 로직 유지)
        verify(objectStorage, times(2)).upload(any(MultipartFile.class), anyString());
        verify(imageRepository, times(2)).save(any(Image.class));
    }

    @Test
    @DisplayName("다중 이미지 업로드 부분 실패 시 성공/실패 파일을 구분하여 반환한다")
    void uploadImagesPartialFailure() {
        // [Given]
        MockMultipartFile validFile = new MockMultipartFile("f1", "ok.jpg", "image/jpeg", "data".getBytes());
        MockMultipartFile invalidFile = new MockMultipartFile("f2", "bad.exe", "app/exe", "bad".getBytes());

        given(objectStorage.upload(any(MultipartFile.class), anyString()))
                .willReturn("http://mock.jpg");

        // [When]
        ImageUploadRes result = imageService.uploadImages(List.of(validFile, invalidFile));

        // [Then]
        assertThat(result.successUrls()).hasSize(1);
        assertThat(result.failedFilenames()).containsExactly("bad.exe");
        verify(objectStorage, times(1)).upload(any(MultipartFile.class), anyString());  // 1개만 성공
    }

    @Test
    @DisplayName("지원하지 않는 확장자는 예외가 발생한다")
    void uploadImageInvalidExtension() {
        MockMultipartFile txtFile = new MockMultipartFile(
                "file", "danger.exe", "application/x-msdownload", "content".getBytes()
        );

        assertThatThrownBy(() -> imageService.uploadImage(txtFile))
                .isInstanceOf(ImageException.class)
                .hasFieldOrPropertyWithValue("errorCode", ImageErrorCode.INVALID_FILE_EXTENSION);
    }

    @Test
    @DisplayName("빈 파일 업로드 시 예외가 발생한다")
    void uploadImageEmptyFile() {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "empty.jpg", "image/jpeg", new byte[0]
        );

        assertThatThrownBy(() -> imageService.uploadImage(emptyFile))
                .isInstanceOf(ImageException.class);
    }
}