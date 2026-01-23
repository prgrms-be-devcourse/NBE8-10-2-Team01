package com.plog.global.minio.storage;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * MinIO가 비활성화되었을 때 사용하는 가짜 저장소 구현체입니다.
 */
@Component
@ConditionalOnProperty(prefix = "minio", name = "enabled", havingValue = "false", matchIfMissing = true)
public class NoOpStorage implements ObjectStorage {
    @Override
    public String upload(MultipartFile file, String destination) {
        return "http://localhost:8080/temp-url/" + destination; // 실제 업로드는 하지 않음
    }

    @Override
    public void delete(String destination) { /* 아무것도 하지 않음 */ }

    @Override
    public String parsePath(String url) { return ""; }
}