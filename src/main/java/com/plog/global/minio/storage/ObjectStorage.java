package com.plog.global.minio.storage;

import com.plog.global.exception.errorCode.ImageErrorCode;
import com.plog.global.exception.exceptions.ImageException;
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

    /**
     * MultipartFile 형태의 파일을 MinIO 스토리지에 업로드합니다.
     *
     * @param file        업로드할 파일 객체
     * @param destination 저장될 파일의 전체 경로 (파일명 포함)
     * @return 저장된 파일의 전체 URL (Endpoint + Bucket + Path)
     * @throws ImageException 파일 업로드 실패 시 {@link ImageErrorCode#IMAGE_UPLOAD_FAILED} 예외 발생
     */

    String upload(MultipartFile file, String destination);

    /**
     * 지정된 경로의 파일을 MinIO 스토리지에서 삭제합니다.
     *
     * @param destination 삭제할 파일의 경로 (파일명 포함)
     * @throws ImageException 파일 삭제 실패 시 {@link ImageErrorCode#IMAGE_DELETE_FAILED} 예외 발생
     */

    void delete(String destination);

    /**
     * 전체 URL에서 스토리지 내부 저장 경로(Object Key)를 추출합니다.
     *
     * @param url 파일의 전체 URL
     * @return 버킷 내부의 파일 경로 (Endpoint와 Bucket명을 제외한 나머지 경로)
     */

    String parsePath(String url);
}