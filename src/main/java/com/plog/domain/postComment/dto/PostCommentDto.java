package com.plog.domain.postComment.dto;

import com.plog.domain.postComment.entity.PostComment;

import java.time.LocalDateTime;

/**
 * 댓글 DTO
 * <p>
 * 순서대로 댓글 아이디, 내용, 작성자, 해당 글, 최초 작성 시각, 최근 수정 시각
 *
 * <p><b>상속 정보:</b><br>
 * 상속 정보를 적습니다.
 *
 * @author njwwn
 * @see
 * @since 2026-01-15
 */
public record PostCommentDto(
        int id,
        String content,
        int authorId,
        int postId,
        LocalDateTime createDate,
        LocalDateTime modifyDate
) {
    public PostCommentDto(PostComment postComment){
        this(
                postComment.getId(),
                postComment.getContent(),
                postComment.getAuthor().getId(),
                postComment.getPost().getId(),
                postComment.getCreateDate(),
                postComment.getModifyDate()
        );
    }

}
