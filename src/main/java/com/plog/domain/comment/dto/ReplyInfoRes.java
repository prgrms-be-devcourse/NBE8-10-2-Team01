package com.plog.domain.comment.dto;

import com.plog.domain.comment.entity.Comment;

import java.time.LocalDateTime;

/**
 * 대댓글의 조회 시 클라이언트에게 전달되는 응답 DTO입니다.
 * <p>
 *
 * <p><b>주요 생성자:</b><br>
 * {@link #ReplyInfoRes(Comment)} <br>
 * 댓글 엔티티 {@link Comment}를 기반으로
 * 클라이언트 응답에 필요한 데이터만을 매핑하여 생성한다.
 * </p>
 *
 * <p><b>외부 모듈:</b><br>
 * {@link java.time.LocalDateTime}을 사용하여
 * 대댓글 생성 시각과 수정 시각을 표현한다.
 *
 * @author njwwn
 * @see Comment
 * @since 2026-01-21
 */
public record ReplyInfoRes(
        long id,
        String content,
        long parentCommentId,
        long authorId,
        String nickname,
        String email,
        String profileUrl,
        LocalDateTime createDate,
        LocalDateTime modifyDate
) {
    public ReplyInfoRes(Comment comment){
        this(
                comment.getId(),
                comment.getContent(),
                comment.getParent().getId(),
                comment.getAuthor().getId(),
                comment.getAuthor().getNickname(),
                comment.getAuthor().getEmail(),
                (comment.getAuthor().getProfileImage() != null)
                        ? comment.getAuthor().getProfileImage().getAccessUrl()
                        : null,
                comment.getCreateDate(),
                comment.getModifyDate()
        );
    }
}
