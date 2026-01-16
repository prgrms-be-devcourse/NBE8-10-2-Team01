package com.plog.domain.post.controller;

import com.plog.domain.post.dto.PostCreateRequest;
import com.plog.domain.post.dto.PostResponse;
import com.plog.domain.post.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

/**
 * 게시물 관련 HTTP 요청을 처리하는 컨트롤러 클래스입니다.
 * <p>
 * 클라이언트로부터 받은 요청 데이터를 DTO로 매핑하고,
 * 비즈니스 로직을 수행한 후 적절한 HTTP 상태 코드와 데이터를 반환합니다.
 *
 * <p><b>상속 정보:</b><br>
 * 상속 정보 없음.
 *
 * <p><b>주요 생성자:</b><br>
 * {@code PostController(PostService postService)} <br>
 * 생성자 주입을 통해 PostService 빈을 주입받습니다.
 *
 * <p><b>빈 관리:</b><br>
 * {@code @RestController}를 사용하여 스프링 컨테이너의 빈으로 관리되며,
 * 모든 메서드의 반환값은 JSON으로 직렬화됩니다.
 *
 * <p><b>외부 모듈:</b><br>
 * Spring Web, Jakarta Validation 등의 모듈을 사용합니다.
 *
 * @author MintyU
 * @since 2026-01-16
 */
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    /**
     * 새로운 게시물을 생성합니다.
     *
     * @param request 제목과 본문이 담긴 DTO
     * @return 생성된 게시물의 상세 정보와 201 Created 상태 코드
     */
    @PostMapping
    public ResponseEntity<Long> createPost(@Valid @RequestBody PostCreateRequest request) {
        Long postId = postService.createPost(request.title(), request.content());
        return ResponseEntity.created(URI.create("/api/posts/" + postId)).body(postId);
    }
}