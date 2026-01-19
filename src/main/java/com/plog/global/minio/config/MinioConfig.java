package com.plog.global.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO 객체 스토리지 연동을 위한 설정 클래스입니다.
 * <p>
 * 애플리케이션 실행 시 MinIO 서버와의 연결 정보를 로드하고,
 * {@link MinioClient} 빈을 생성하여 스프링 컨테이너에 등록합니다.
 * 등록된 클라이언트는 이미지 파일 업로드 및 삭제 등의 작업에 사용됩니다.
 * <p><b>외부 모듈:</b><br>
 * implementation 'io.minio:minio:8.5.7'
 *
 * @author Jaewon Ryu
 * @see
 * @since 2026-01-16
 */
@Configuration
public class MinioConfig {

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.access_key}")
    private String accessKey;

    @Value("${minio.secret_key}")
    private String secretKey;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }
}