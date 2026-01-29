package com.plog.domain.post.controller;

import com.plog.domain.post.dto.PostCreateReq;
import com.plog.domain.post.dto.PostInfoRes;
import com.plog.domain.post.dto.PostListRes;
import com.plog.domain.post.dto.PostUpdateReq;
import com.plog.domain.post.service.PostService;
import com.plog.global.response.CommonResponse;
import com.plog.global.response.Response;
import com.plog.global.security.SecurityUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
     * @param request 게시물 제목과 본문 데이터
     * @return 생성된 게시물의 ID를 포함한 공통 응답 객체 (201 Created)
     */
    @PostMapping
    public ResponseEntity<Void> createPost(
            @AuthenticationPrincipal SecurityUser user,
            @Valid @RequestBody PostCreateReq request
    ) {
        Long postId = postService.createPost(user.getId(), request);

        return ResponseEntity.created(URI.create("/api/posts/" + postId)).build();
    }

    /**
     * 특정 ID의 게시물을 상세 조회합니다.
     *
     * @param id 게시물 고유 식별자
     * @return 조회된 게시물 정보와 성공 메시지를 포함한 공통 응답 객체 (200 OK)
     */
    @GetMapping("/{id}")
    public ResponseEntity<Response<PostInfoRes>> getPost(
            @PathVariable Long id,
            @RequestParam(name = "comment_offset", defaultValue = "0") int pageNumber
    ) {
        PostInfoRes response = postService.getPostDetail(id, pageNumber);
        return ResponseEntity.ok(CommonResponse.success(response, "게시글 조회 성공"));
    }

    /**
     * 모든 게시물 목록을 페이징(Slice)하여 조회합니다.
     * <p>
     * {@link Slice}를 사용하여 전체 카운트 쿼리 없이 다음 페이지 존재 여부만 확인합니다.
     * 이는 무한 스크롤 구현에 최적화된 방식입니다.
     *
     * @param pageable 페이징 및 정렬 정보 (기본값: 10개씩, 생성일 내림차순)
     * @return 게시물 데이터 슬라이스와 성공 메시지를 포함한 공통 응답 객체
     */
    @GetMapping
    public ResponseEntity<Response<Slice<PostListRes>>> getPosts(
            @PageableDefault(size = 10, sort = "createDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Slice<PostListRes> posts = postService.getPosts(pageable);
        return ResponseEntity.ok(CommonResponse.success(posts, "게시글 목록 조회 성공"));
    }

    /**
     * 기존 게시물의 제목, 본문, 썸네일, 해시태그를 수정합니다.
     *
     * <p><b>처리 프로세스:</b><br>
     * 1. 요청된 ID로 게시물을 찾아 제목과 본문을 업데이트합니다. <br>
     * 2. 수정된 본문을 바탕으로 요약본(Summary)을 자동으로 재생성하여 저장합니다. <br>
     * 3. 해시태그 정보를 업데이트합니다. <br>
     * 4. 성공 시 별도의 응답 본문 없이 {@code 204 No Content} 상태 코드를 반환합니다.
     *
     * @param id      수정할 게시물의 고유 식별자(ID)
     * @param request 수정할 제목과 본문, 썸네일 및 해시태그 정보({@link PostUpdateReq})
     * @return {@code 204 No Content} 응답
     */
    @PutMapping("/{id}")
    public ResponseEntity<Void> updatePost(
            @AuthenticationPrincipal SecurityUser user,
            @PathVariable Long id,
            @Valid @RequestBody PostUpdateReq request) {

        postService.updatePost(user.getId(), id, request);

        return ResponseEntity.noContent().build();
    }

    /**
     * 기존 게시물을 삭제합니다.
     *
     * <p><b>처리 프로세스:</b><br>
     * 1. 요청된 ID의 게시물을 시스템에서 영구적으로 제거합니다. <br>
     * 2. 삭제 성공 시 별도의 응답 본문 없이 {@code 204 No Content} 상태 코드를 반환합니다.
     *
     * @param id 삭제할 게시물의 고유 식별자(ID)
     * @return {@code 204 No Content} 응답
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(
            @AuthenticationPrincipal SecurityUser user,
            @PathVariable Long id
    ) {

        postService.deletePost(user.getId(), id);

        return ResponseEntity.noContent().build();
    }

    /**
     * 특정 사용자가 작성한 게시물 목록을 페이징(Slice)하여 조회합니다.
     * <p>
     * {@link Slice} 방식을 사용하여 전체 게시물 개수(Count)를 조회하지 않고,
     * 다음 페이지 존재 여부만을 확인하여 반환합니다. 이는 무한 스크롤이나 '더 보기'
     * 형태의 UI 구현에 최적화되어 있습니다.
     *
     * @param memberId 조회할 사용자의 고유 식별자(ID)
     * @param pageable 페이징 및 정렬 정보 (기본값: 10개씩, 생성일 내림차순 정렬)
     * @return 게시물 데이터 슬라이스와 성공 메시지를 포함한 공통 응답 객체
     */
    @GetMapping("/members/{memberId}")
    public ResponseEntity<Response<Slice<PostInfoRes>>> getPostsByMember(
            @PathVariable Long memberId,
            @PageableDefault(size = 10, sort = "createDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Slice<PostInfoRes> posts = postService.getPostsByMember(memberId, pageable);

        return ResponseEntity.ok(CommonResponse.success(posts, "사용자 게시글 목록 조회 성공"));
    }
}