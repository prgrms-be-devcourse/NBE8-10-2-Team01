package com.plog.global.minio.storage;

import org.springframework.web.multipart.MultipartFile;

/**
 * 파일 스토리지(File Storage) 기능을 추상화한 인터페이스입니다.
 * <p>
 * 비즈니스 로직이 구체적인 저장소 기술(MinIO, AWS S3, Local Disk 등)에 종속되지 않도록
 * 표준화된 파일 업로드 및 관리 메서드를 정의합니다.
 * 이 인터페이스를 통해 서비스 계층은 저장소가 바뀌어도 코드를 수정할 필요가 없습니다.
 * <p><b>빈 관리:</b><br>
 * @Component 등을 사용하여 빈으로 등록해야 합니다.
 *
 * <p><b>외부 모듈:</b><br>
 * 파일 처리를 위해 Spring의 {@link MultipartFile}을 사용합니다.
 *
 * @author Jaewon Ryu
 * @see
 * @since 2026-01-16
 */
public interface ObjectStorage {
    String upload(MultipartFile file, String destination);

    void delete(String destination);

    String parsePath(String url);
}