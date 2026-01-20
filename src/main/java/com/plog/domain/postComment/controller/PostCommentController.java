package com.plog.domain.postComment.controller;

import com.plog.domain.post.entity.Post;
import com.plog.domain.postComment.dto.CreatePostCommentReq;
import com.plog.domain.postComment.dto.GetPostCommentRes;
import com.plog.domain.postComment.dto.UpdatePostCommentReq;
import com.plog.domain.postComment.entity.PostComment;
import com.plog.domain.postComment.service.PostCommentService;
import com.plog.global.response.CommonResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
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
 * 실제 비즈니스 로직은 {@link PostCommentService}에 위임한다.
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
 * @see PostCommentService
 * @since 2026-01-19
 */

@RestController
@RequestMapping("/api/posts/{postId}/comments")
@RequiredArgsConstructor
public class PostCommentController {
    private final PostCommentService postCommentService;

    @GetMapping
    public ResponseEntity<CommonResponse<List<GetPostCommentRes>>> getComments(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page
    ) {
        List<GetPostCommentRes> responseList = postCommentService.getCommentsByPostId(postId, page);

        return ResponseEntity.ok(CommonResponse.success(responseList, "댓글 조회 성공"));
    }


    @PostMapping
    public ResponseEntity<CommonResponse<Long>> createComment(
            @PathVariable Long postId,
            @Valid @RequestBody CreatePostCommentReq req
            ){

        Long commentId = postCommentService.createComment(postId, req.content(), req.parentCommentId());

        CommonResponse<Long> response = CommonResponse.success(commentId, "댓글 작성 완료");

        return ResponseEntity
                .created(URI.create("/api/posts/" + postId + "/comments/" + commentId))
                .body(response);
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<CommonResponse<Long>> updateComment(
            @PathVariable Long commentId,
            @RequestBody @Valid UpdatePostCommentReq req
    ){
        PostComment updated = postCommentService.updateComment(commentId, req.content());

        return ResponseEntity.ok(
                CommonResponse.success(updated.getId(), "댓글 수정 완료")
        );
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<CommonResponse<Long>> deleteComment(
            @PathVariable Long commentId
    ){
        postCommentService.deleteComment(commentId);

        return ResponseEntity.ok(
                CommonResponse.success(commentId, "댓글 삭제 완료")
        );
    }

}
