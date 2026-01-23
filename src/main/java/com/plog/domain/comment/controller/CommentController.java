package com.plog.domain.comment.controller;

import com.plog.domain.comment.constant.CommentConstants;
import com.plog.domain.comment.dto.CommentCreateReq;
import com.plog.domain.comment.dto.CommentInfoRes;
import com.plog.domain.comment.dto.CommentUpdateReq;
import com.plog.domain.comment.dto.ReplyInfoRes;
import com.plog.domain.comment.entity.Comment;
import com.plog.domain.comment.service.CommentService;
import com.plog.global.response.CommonResponse;
import com.plog.global.response.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

/**
 * 게시물 댓글(Comment)에 대한 REST 컨트롤러.
 *
 * <p>
 * 특정 게시물(postId)에 종속된 댓글의 조회, 생성, 수정, 삭제 기능을 제공한다.
 * 댓글 삭제는 물리 삭제가 아닌 논리 삭제(soft delete) 방식으로 처리되며,
 * 삭제된 댓글은 "[삭제된 댓글입니다.]"라는 대체 메시지로 관리된다.
 *
 * 댓글 조회 : 최대 10개의 댓글이 페이징되어 조회된다.
 * 대댓글 조회 : 최대 5개의 댓글이 페이징되어 조회된다.
 * </p>
 *
 * <p><b>주요 생성자:</b><br>
 * {@code PostCommentController(PostCommentService postCommentService)}<br>
 * 댓글 관련 비즈니스 로직을 수행하는 Service를 주입받는다.
 * </p>
 *
 * <p><b>빈 관리:</b><br>
 * {@link RestController}와 {@link RequiredArgsConstructor}를 통해
 * Spring 컨테이너에 의해 싱글톤 빈으로 관리된다.
 * </p>
 *
 * <p><b>외부 모듈:</b><br>
 * Spring Web MVC, Bean Validation을 사용한다.
 * </p>
 *
 * @author njwwn
 * @see CommentService
 * @since 2026-01-19
 */

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    /**
     * 해당 게시물의 루트 댓글들을 최대 10개씩 조회합니다.
     *
     * @param postId 해당 게시물 식별자
     * @param pageable 페이징(Paging)과 정렬(Sorting)의 '기본값'을 설정하는 설정값
     * @return 페이징된 댓글 정보와 조회 성공 메시지.
     */

    /**
     * 해당 댓글의 대댓글들을 최대 5개씩 조회합니다.
     *
     * @param commentId 해당 댓글 식별자
     * @param pageable 페이징(Paging)과 정렬(Sorting)의 '기본값'을 설정하는 설정값
     * @return 페이징된 대댓글 정보와 조회 성공 메시지.
     */
    @GetMapping({"/comments/{commentId}/replies"})
    public ResponseEntity<Response<Slice<ReplyInfoRes>>> getReplies(
            @PathVariable Long commentId,
            @PageableDefault(
                    size = CommentConstants.REPLY_PAGE_SIZE,
                    sort = CommentConstants.DEFAULT_SORT_FIELD,
                    direction = Sort.Direction.ASC) Pageable pageable
    ) {

        Slice<ReplyInfoRes> replyList = commentService.getRepliesByCommentId(commentId, pageable);

        return ResponseEntity.ok(CommonResponse.success(replyList, "댓글 조회 성공"));
    }


    /**
     * 해당 게시글에 새로운 댓글을 생성합니다.
     *
     * @param req 댓글 내용, 부모 댓글 식별자, 작성자 식별자
     * @return 해당 게시물과 생성된 댓글의 식별자를 포함하여 응답
     */
    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<Void> createComment(
            @PathVariable Long postId,
            @Valid @RequestBody CommentCreateReq req
            ){

        Long commentId = commentService.createComment(postId, req);

        return ResponseEntity
                .created(URI.create("/api/posts/" + postId + "/comments/" + commentId))
                .build();
    }

    /**
     * 기존의 댓글을 수정합니다.
     *
     * @param commentId 수정할 댓글의 식별자
     * @param req 수정될 댓글의 내용
     * @return 댓글 수정 성공의 상태 코드
     */
    @PutMapping("/comments/{commentId}")
    public ResponseEntity<Void> updateComment(
            @PathVariable("commentId") Long commentId,
            @RequestBody @Valid CommentUpdateReq req
    ){
        commentService.updateComment(commentId, req.content());

        return ResponseEntity.ok().build();
    }

    /**
     * 기존의 댓글을 삭제합니다.
     *
     * @param commentId 삭제할 댓글의 식별자
     * @return 해당 댓글 식별자와 댓글 삭제 완료 메시지
     */
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<CommonResponse<Long>> deleteComment(
            @PathVariable Long commentId
    ) {
        commentService.deleteComment(commentId);

        return ResponseEntity.ok(
                CommonResponse.success(commentId, "댓글 삭제 완료")
        );
    }

}
