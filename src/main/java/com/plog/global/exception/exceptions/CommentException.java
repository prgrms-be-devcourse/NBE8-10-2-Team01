package com.plog.global.exception.exceptions;

import com.plog.global.exception.errorCode.PostCommentErrorCode;

/**
 * 게시글 댓글 도메인에서 발생하는 비즈니스 예외를 처리하는 클래스입니다.
 * <p>
 * {@code PostCommentException}은 댓글 생성, 조회, 수정, 삭제 과정에서 발생하는
 * 도메인 수준의 오류를 표현하기 위해 사용된다.
 *
 * <p><b>상속 정보:</b><br>
 * {@link BaseException}을 상속받는다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@link PostCommentException(PostCommentErrorCode)}  <br>
 * 기본 메시지를 그대로 사용하는 단순 예외 <br>
 *
 * {@link PostCommentException(PostCommentErrorCode, String)}  <br>
 * 내부 로그 메시지를 분리하여 기록하고자 할 때 사용 <br>
 *
 * {@link PostCommentException(PostCommentErrorCode, String, String)}  <br>
 * 클라이언트 응답 메시지를 커스터마이징해야 하는 경우 사용 <br>
 *
 *
 * @author njwwn
 * @see BaseException
 * @see PostCommentErrorCode
 * @since 2026-01-20
 */
public class PostCommentException extends BaseException {
    public PostCommentException(PostCommentErrorCode errorCode) {
        super(errorCode);
    }

    public PostCommentException(PostCommentErrorCode errorCode, String logMessage) {
        super(errorCode, logMessage);
    }

    public PostCommentException(PostCommentErrorCode errorCode, String logMessage, String clientMessage) {
        super(errorCode, logMessage, clientMessage);
    }
}
