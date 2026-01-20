package com.plog.domain.comment.controller;

import com.plog.domain.comment.dto.CommentCreateReq;
import com.plog.domain.comment.dto.CommentInfoRes;
import com.plog.domain.comment.dto.CommentUpdateReq;
import com.plog.domain.comment.entity.Comment;
import com.plog.domain.comment.service.CommentService;
import com.plog.global.response.CommonResponse;
import com.plog.global.response.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * 게시물 댓글(Comment)에 대한 REST 컨트롤러.
 *
 * <p>
 * 특정 게시물(postId)에 종속된 댓글의 조회, 생성, 수정, 삭제 기능을 제공한다.
 * 댓글 삭제는 물리 삭제가 아닌 논리 삭제(soft delete) 방식으로 처리되며,
 * 삭제된 댓글은 "[삭제된 댓글입니다.]"라는 대체 메시지로 관리된다.
 * </p>
 *
 * <p><b>작동 원리:</b><br>
 * 본 컨트롤러는 HTTP 요청을 받아 요청 데이터를 검증한 뒤,
 * 실제 비즈니스 로직은 {@link CommentService}에 위임한다.
 * 트랜잭션 경계는 Service 계층에서 관리하며,
 * 컨트롤러에서는 엔티티를 직접 노출하지 않는다.
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

    @GetMapping
    public ResponseEntity<Response<List<CommentInfoRes>>> getComments(
            @PathVariable Long postId
    ) {
        List<CommentInfoRes> commentList = commentService.getCommentsByPostId(postId);

        return ResponseEntity.ok(CommonResponse.success(commentList, "댓글 조회 성공"));
    }


    @PostMapping("/post/{postId}/comments")
    public ResponseEntity<Void> createComment(
            @PathVariable Long postId,
            @Valid @RequestBody CommentCreateReq req
            ){

        Long commentId = commentService.createComment(postId, req);

        return ResponseEntity
                .created(URI.create("/api/posts/" + postId + "/comments/" + commentId))
                .build();
    }

    @PutMapping("/comments/{commetId}")
    public ResponseEntity<Void> updateComment(
            @PathVariable Long commentId,
            @RequestBody @Valid CommentUpdateReq req
    ){
        commentService.updateComment(commentId, req.content());

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<CommonResponse<Long>> deleteComment(
            @PathVariable Long commentId
    ){
        commentService.deleteComment(commentId);

        return ResponseEntity.ok(
                CommonResponse.success(commentId, "댓글 삭제 완료")
        );
    }

}
