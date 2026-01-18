package com.plog.domain.postComment.controller;

import com.plog.domain.postComment.dto.PostCommentDto;
import com.plog.domain.postComment.entity.PostComment;
import com.plog.domain.postComment.service.PostCommentService;
import com.plog.global.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.plog.domain.postComment.service.PostCommentService;


import java.util.List;

/**
 * 코드에 대한 전체적인 역할을 적습니다.
 * <p>
 * 코드에 대한 작동 원리 등을 적습니다.
 *
 * <p><b>상속 정보:</b><br>
 * 상속 정보를 적습니다.
 *
 * @author njwwn
 * @see
 * @since 2026-01-15
 */

@RestController
@Validated
@RequestMapping("/api/v1/posts/{postId}/comments")
@RequiredArgsConstructor
public class PostCommentController {

    private final PostCommentService postCommentService;

    record PostCommentWriteReqBody(
            @NotBlank(message = "댓글을 입력해주세요")
            @Size(min = 1, max = 100, message = "댓글은 100자 이내로 작성해야 합니다.")
            String content
    ) {
    }

    @PostMapping
    @Transactional
    public ResponseEntity<CommonResponse<PostCommentDto>> write(
            @PathVariable int postId,
            @Valid @RequestBody PostCommentWriteReqBody reqBody
    ){

        Optional<Post> postOptional = postService.findById(postId);

        if (postOptional.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(CommonResponse.fail("게시글을 찾을 수 없습니다."));
        }

        Post post = postOptional.get();

        PostComment postComment = postCommentService.write(post, reqBody.content);

        PostCommentDto dto = new PostCommentDto(postComment);

        return ResponseEntity.ok(CommonResponse.success(dto));
    }

    @DeleteMapping("/{commentId}")
    @Transactional
    @Operation(summary = "삭제")
    public ResponseEntity<CommonResponse<Void>> delete(
            @PathVariable long commentId
    ){

        PostComment postComment = postCommentService.findById(commentId).get();

        postCommentService.delete(postComment);

    }




}
