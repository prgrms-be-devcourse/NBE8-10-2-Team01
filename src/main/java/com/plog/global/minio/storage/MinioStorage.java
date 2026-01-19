package com.plog.global.minio.storage;

import com.plog.global.exception.errorCode.ImageErrorCode;
import com.plog.global.exception.exceptions.ImageException;
import io.minio.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * MinIO 객체 스토리지와의 통신을 담당하는 구현체 클래스입니다.
 * <p>
 * {@link ObjectStorage} 인터페이스를 구현하여 파일 업로드, 삭제, 경로 파싱 등의 기능을 수행합니다.
 * 애플리케이션 초기화 시점에 버킷 존재 여부를 확인하고, 없을 경우 자동으로 생성합니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@link ObjectStorage} 인터페이스를 구현합니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@code MinioStorage(MinioClient minioClient)} <br>
 * MinIO 클라이언트 객체를 주입받아 초기화합니다. <br>
 *
 * <p><b>빈 관리:</b><br>
 * {@code @Component} 어노테이션을 통해 스프링 빈으로 등록됩니다. <br>
 * {@code @ConditionalOnProperty(name = "enabled", havingValue = "true")} 설정에 의해
 * application.yml의 minio.enabled 속성이 true일 경우에만 빈이 생성됩니다.
 *
 * <p><b>외부 모듈:</b><br>
 * {@code io.minio:minio} 라이브러리를 사용하여 MinIO 서버와 통신합니다.
 *
 * @author Jaewon Ryu
 * @since 2026-01-16
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "minio", name = "enabled", havingValue = "true")
public class MinioStorage implements ObjectStorage {

    private final MinioClient minioClient;

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.bucket}")
    private String bucket;

    @PostConstruct
    public void init() {
        validateBucket();
    }

    /**
     * MultipartFile 형태의 파일을 MinIO 스토리지에 업로드합니다.
     *
     * @param file        업로드할 파일 객체
     * @param destination 저장될 파일의 전체 경로 (파일명 포함)
     * @return 저장된 파일의 전체 URL (Endpoint + Bucket + Path)
     * @throws ImageException 파일 업로드 실패 시 {@link ImageErrorCode#IMAGE_UPLOAD_FAILED} 예외 발생
     */
    @Override
    public String upload(MultipartFile file, String destination) {
        try {
            InputStream inputStream = file.getInputStream();

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(destination)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build());

            return endpoint + "/" + bucket + "/" + destination;

        } catch (Exception e) {
            throw new ImageException(ImageErrorCode.IMAGE_UPLOAD_FAILED,
                    "[MinioStorage#upload] failed. dest=" + destination + ", cause=" + e.getMessage(),
                    "이미지 업로드 중 오류가 발생했습니다.");
        }
    }

    /**
     * 지정된 경로의 파일을 MinIO 스토리지에서 삭제합니다.
     *
     * @param destination 삭제할 파일의 경로 (파일명 포함)
     * @throws ImageException 파일 삭제 실패 시 {@link ImageErrorCode#IMAGE_DELETE_FAILED} 예외 발생
     */
    @Override
    public void delete(String destination) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .object(destination)
                    .build());
        } catch (Exception e) {
            throw new ImageException(ImageErrorCode.IMAGE_DELETE_FAILED,
                    "[MinioStorage#delete] failed. dest=" + destination + ", cause=" + e.getMessage(),
                    "이미지 삭제 중 오류가 발생했습니다.");
        }
    }

    /**
     * 전체 URL에서 스토리지 내부 저장 경로(Object Key)를 추출합니다.
     *
     * @param url 파일의 전체 URL
     * @return 버킷 내부의 파일 경로 (Endpoint와 Bucket명을 제외한 나머지 경로)
     */
    @Override
    public String parsePath(String url) {
        if (url == null || !url.contains(endpoint + "/" + bucket)) {
            return "";
        }
        int idx = (endpoint + "/" + bucket).length();
        // URL 구조상 '/'가 포함되어 있으므로 인덱스 조정 (+1)
        return url.substring(idx + 1);
    }

    /**
     * MinIO 버킷의 존재 여부를 확인하고, 없을 경우 생성합니다.
     *
     * @throws ImageException 초기화 실패 시 {@link ImageErrorCode#BUCKET_INIT_FAILED} 예외 발생
     */
    private void validateBucket() {
        try {
            boolean isExists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucket).build());
            if (isExists) return;

            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());

        } catch (Exception e) {
            throw new ImageException(ImageErrorCode.BUCKET_INIT_FAILED,
                    "[MinioStorage#validateBucket] init failed. bucket=" + bucket + ", cause=" + e.getMessage(),
                    "이미지 저장소 연결에 실패했습니다.");
        }
    }
}
