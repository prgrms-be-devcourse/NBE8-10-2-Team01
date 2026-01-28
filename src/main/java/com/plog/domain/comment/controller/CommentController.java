package com.plog.domain.comment.controller;

import com.plog.domain.comment.dto.CommentCreateReq;
import com.plog.domain.comment.dto.CommentInfoRes;
import com.plog.domain.comment.dto.CommentUpdateReq;
import com.plog.domain.comment.dto.ReplyInfoRes;
import com.plog.domain.comment.service.CommentService;
import com.plog.global.response.CommonResponse;
import com.plog.global.response.Response;
import com.plog.global.security.SecurityUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
     * @param pageNumber 조회할 댓글 pageNumber
     * @param securityUser 현재 인증된 사용자 정보 (비로그인 시 null)
     * @return 페이징된 댓글 정보와 조회 성공 메시지.
     */

    @GetMapping({"posts/{postId}/comments"})
    public ResponseEntity<Response<Slice<CommentInfoRes>>> getComments(
            @PathVariable Long postId,
            @RequestParam(name = "pageNumber", defaultValue = "0") int pageNumber,
            @AuthenticationPrincipal SecurityUser securityUser
    ) {
        Slice<CommentInfoRes> commentList = commentService.getCommentsByPostId(postId, pageNumber);

        return ResponseEntity.ok(CommonResponse.success(commentList, "댓글 조회 성공"));
    }

    /**
     * 해당 댓글의 대댓글들을 최대 5개씩 조회합니다.
     *
     * @param commentId 해당 댓글 식별자
     * @param pageNumber 조회할 대댓글 pageNumber
     * @param securityUser 현재 인증된 사용자 정보 (비로그인 시 null)
     * @return 페이징된 대댓글 정보와 조회 성공 메시지.
     */
    @GetMapping({"/comments/{commentId}/replies"})
    public ResponseEntity<Response<Slice<ReplyInfoRes>>> getReplies(
            @PathVariable Long commentId,
            @RequestParam(name = "pageNumber", defaultValue = "0") int pageNumber,
            @AuthenticationPrincipal SecurityUser securityUser
    ) {

        Long currentMemberId = (securityUser != null) ? securityUser.getId() : null;

        Slice<ReplyInfoRes> replyList = commentService.getRepliesByCommentId(commentId, pageNumber);

        return ResponseEntity.ok(CommonResponse.success(replyList, "댓글 조회 성공"));
    }


    /**
     * 해당 게시글에 새로운 댓글을 생성합니다.
     *
     * @param postId 현재 게시글 식별자
     * @param req 댓글 내용, 부모 댓글 식별자, 작성자 식별자
     * @param securityUser 현재 인증된 사용자 정보
     * @return 해당 게시물과 생성된 댓글의 식별자를 포함하여 응답
     */
    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<Void> createComment(
            @PathVariable Long postId,
            @Valid @RequestBody CommentCreateReq req,
            @AuthenticationPrincipal SecurityUser securityUser
            ){

        Long commentId = commentService.createComment(postId, securityUser.getId(), req);

        return ResponseEntity
                .created(URI.create("/api/posts/" + postId + "/comments/" + commentId))
                .build();
    }

    /**
     * 기존의 댓글을 수정합니다.
     *
     * @param commentId 수정할 댓글의 식별자
     * @param req 수정될 댓글의 내용
     * @param securityUser 현재 인증된 사용자 정보
     *
     * @return 댓글 수정 성공의 상태 코드
     */
    @PutMapping("/comments/{commentId}")
    public ResponseEntity<Void> updateComment(
            @PathVariable("commentId") Long commentId,
            @RequestBody @Valid CommentUpdateReq req,
            @AuthenticationPrincipal SecurityUser securityUser
    ){
        commentService.updateComment(commentId, securityUser.getId(), req.content());

        return ResponseEntity.ok().build();
    }

    /**
     * 기존의 댓글을 삭제합니다.
     *
     * @param commentId 삭제할 댓글의 식별자
     * @param securityUser 현재 인증된 사용자 정보
     * @return 해당 댓글 식별자와 댓글 삭제 완료 메시지
     */
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<CommonResponse<Long>> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal SecurityUser securityUser
    ) {
        commentService.deleteComment(commentId, securityUser.getId());

        return ResponseEntity.ok(
                CommonResponse.success(commentId, "댓글 삭제 완료")
        );
    }

}
