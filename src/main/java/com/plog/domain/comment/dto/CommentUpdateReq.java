package com.plog.domain.comment.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 게시물 댓글 수정을 위한 요청 DTO.
 *
 * <p>
 * 기존에 작성된 댓글의 내용을 수정할 때 클라이언트로부터 전달받는 요청 객체이다.
 * 댓글의 식별자(commentId)는 URL 경로 변수로 전달되며,
 * 본 DTO는 수정할 댓글의 내용만을 포함한다.
 * </p>
 *
 * <p><b>주요 생성자:</b><br>
 * {@code UpdatePostCommentReq(String content)} <br>
 * 수정할 댓글 내용을 기반으로 요청 객체를 생성한다.
 * </p>
 *
 * <p><b>외부 모듈:</b><br>
 * Bean Validation({@link jakarta.validation.constraints.NotBlank})을 사용하여
 * 댓글 내용에 대한 유효성 검증을 수행한다.
 * </p>
 *
 * <p><b>설계 의도:</b><br>
 * 본 DTO는 요청 데이터 전달만을 책임지며,
 * 댓글 존재 여부 확인, 삭제 여부 판단, 수정 권한 검증 등의
 * 비즈니스 로직은 서비스 계층에서 처리한다.
 * </p>
 *
 * @author njwwn
 * @since 2026-01-19
 */
public record UpdateCommentReq(
        @NotBlank(message = "내용을 입력해주세요.")
        String content
) {}
