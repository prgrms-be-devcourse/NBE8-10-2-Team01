package com.plog.global.storage;

import io.minio.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;

import java.io.InputStream;

/**
 * 코드에 대한 전체적인 역할을 적습니다.
 * <p>
 * 코드에 대한 작동 원리 등을 적습니다.
 *
 * <p><b>상속 정보:</b><br>
 * 상속 정보를 적습니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@code MinioStorage(String example)} <br>
 * 주요 생성자와 그 매개변수에 대한 설명을 적습니다. <br>
 *
 * <p><b>빈 관리:</b><br>
 * 필요 시 빈 관리에 대한 내용을 적습니다.
 *
 * <p><b>외부 모듈:</b><br>
 * 필요 시 외부 모듈에 대한 내용을 적습니다.
 *
 * @author Jaewon Ryu
 * @see
 * @since 2026-01-16
 */
@Component
@RequiredArgsConstructor
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
     * MultipartFile 형태의 파일을 업로드합니다.
     *
     * @param file        업로드할 파일
     * @param destination 저장할 파일 경로
     * @return 저장된 minio URL
     */
    @Override
    public String upload(MultipartFile file, String destination) {
        try {
            InputStream inputStream = file.getInputStream();

            minioClient.putObject(
                    PutObjectArgs.builder().bucket(bucket).object(destination).stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build());

            return endpoint + "/" + bucket + "/" + destination;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * path 경로에 저장된 파일을 삭제합니다.
     *
     * @param destination 삭제할 파일의 경로
     */
    @Override
    public void delete(String destination) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .object(destination)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 파일 URL에서 object 경로를 추출합니다.
     *
     * @param url 파일 전체 URL
     * @return 저장된 디렉토리 경로
     */
    @Override
    public String parsePath(String url) {
        int idx = (endpoint + "/" + bucket).length();
        return url.substring(idx);
    }

    /**
     * MinIO 버킷이 유효한지 확인합니다. 유효하지 않다면, 해당 이름으로 버킷을 생성합니다.
     */
    private void validateBucket() {
        try {
            boolean isExists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucket).build());
            if (isExists) return;

            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());

            String policyJson = "{\n" +
                    "    \"Version\": \"2012-10-17\",\n" +
                    "    \"Statement\": [\n" +
                    "        {\n" +
                    "            \"Effect\": \"Allow\",\n" +
                    "            \"Principal\": {\n" +
                    "                \"AWS\": [\n" +
                    "                    \"*\"\n" +
                    "                ]\n" +
                    "            },\n" +
                    "            \"Action\": [\n" +
                    "                \"s3:GetObject\"\n" +
                    "            ],\n" +
                    "            \"Resource\": [\n" +
                    "                \"arn:aws:s3:::" + bucket + "/*\"\n" +
                    "            ]\n" +
                    "        }\n" +
                    "    ]\n" +
                    "}";

            minioClient.setBucketPolicy(SetBucketPolicyArgs.builder()
                    .bucket(bucket)
                    .config(policyJson) // 수정한 JSON 문자열 넣기
                    .build());

        } catch (Exception e) {
            e.printStackTrace(); // 에러 로그 찍기
            throw new RuntimeException(e.getMessage());
        }
    }
}