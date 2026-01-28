package com.plog.domain.comment.dto;

import com.plog.domain.comment.entity.Comment;
import org.springframework.data.domain.Slice;

import java.time.LocalDateTime;

/**
 * 게시물 댓글 조회 시 클라이언트에게 전달되는 댓글 응답 DTO입니다.
 * <p>
 * 게시물에 속한 댓글의 식별 정보와 내용을 포함하여
 * 댓글 목록 조회 API의 응답 데이터로 사용된다.
 * </p>
 *
 * <p><b>주요 생성자:</b><br>
 * {@link #CommentInfoRes(Comment, Slice<ReplyInfoRes>)} <br>
 * 댓글 엔티티 {@link Comment}를 기반으로
 * 클라이언트 응답에 필요한 데이터만을 매핑하여 생성한다.
 * </p>
 *
 * <p><b>외부 모듈:</b><br>
 * Java Time API ({@link java.time.LocalDateTime})를 사용하여
 * 댓글의 생성 및 수정 시점을 표현한다.
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
        String nickname,
        String email,
        String profileUrl,
        long postId,
        LocalDateTime createDate,
        LocalDateTime modifyDate,
        long replyCount,
        Slice<ReplyInfoRes> previewReplies
) {
    public CommentInfoRes(Comment comment, Slice<ReplyInfoRes> previewReplies) {
        this(
                comment.getId(),
                comment.getContent(),
                comment.getAuthor().getId(),
                comment.getAuthor().getNickname(),
                comment.getAuthor().getEmail(),
                (comment.getAuthor().getProfileImage() != null)
                        ? comment.getAuthor().getProfileImage().getAccessUrl()
                        : null,
                comment.getPost().getId(),
                comment.getCreateDate(),
                comment.getModifyDate(),
                comment.getReplyCount(),
                previewReplies
        );
    }
}
