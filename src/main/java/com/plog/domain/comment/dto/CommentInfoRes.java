package com.plog.domain.comment.dto;

import com.plog.domain.comment.entity.Comment;

import java.time.LocalDateTime;

/**
 * 게시물 댓글 조회 시 클라이언트에게 전달되는 댓글 응답 DTO.
 * <p>
 * 게시물에 속한 댓글의 식별 정보와 내용을 포함하여
 * 댓글 목록 조회 API의 응답 데이터로 사용된다.
 * </p>
 *
 * <p><b>주요 생성자:</b><br>
 * {@link #CommentInfoRes(Comment)} <br>
 * 댓글 엔티티 {@link Comment}를 기반으로
 * 클라이언트 응답에 필요한 데이터만을 매핑하여 생성한다.
 * </p>
 *
 * <p><b>외부 모듈:</b><br>
 * Java Time API ({@link java.time.LocalDateTime})를 사용하여
 * 댓글의 생성 및 수정 시점을 표현한다.
 * </p>
 *
 * <p><b>설계 의도:</b><br>
 * 조회 전용 DTO로서 수정이나 비즈니스 로직을 포함하지 않으며,
 * API 응답 구조의 안정성과 엔티티 캡슐화를 보장한다.
 * </p>
 *
 * @author njwwn
 * @see Comment
 * @since 2026-01-19
 */
public record CommentInfoRes(
        long id,
        String content,
        long authorId,
        long postId,
        LocalDateTime createDate,
        LocalDateTime modifyDate
) {
    public CommentInfoRes(Comment comment){
        this(
                comment.getId(),
                comment.getContent(),
                comment.getAuthor().getId(),
                comment.getPost().getId(),
                comment.getCreateDate(),
                comment.getModifyDate()
        );
    }
}
