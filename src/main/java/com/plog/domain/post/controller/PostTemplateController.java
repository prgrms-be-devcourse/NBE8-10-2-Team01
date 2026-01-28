package com.plog.domain.post.controller;

import com.plog.domain.post.dto.PostTemplateInfoDto;
import com.plog.domain.post.dto.PostTemplateSummaryRes;
import com.plog.domain.post.dto.PostTemplateUpdateReq;
import com.plog.domain.post.service.PostTemplateService;
import com.plog.global.response.CommonResponse;
import com.plog.global.response.Response;
import com.plog.global.security.SecurityUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * postTemplate 관련한 API 엔드포인트입니다.
 *
 * @author jack8
 * @since 2026-01-26
 */
@RestController
@RequestMapping("/api/posts/templates")
@RequiredArgsConstructor
public class PostTemplateController {

    private final PostTemplateService postTemplateService;

    @PostMapping
    public ResponseEntity<Void> createPostTemplate(@Valid @RequestBody PostTemplateInfoDto request,
                                                   @AuthenticationPrincipal SecurityUser user) {
        Long templateId = postTemplateService.createPostTemplate(user.getId(), request);

        return ResponseEntity.created(URI.create("/api/posts/templates/" + templateId)).build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response<PostTemplateInfoDto>> getPostTemplate(
            @PathVariable(name = "id") Long id,
            @AuthenticationPrincipal SecurityUser securityUser
    ) {
        PostTemplateInfoDto response = postTemplateService.getTemplate(securityUser.getId(), id);

        return ResponseEntity.ok(CommonResponse.success(response, "post template 조회"));
    }

    @GetMapping
    public ResponseEntity<Response<List<PostTemplateSummaryRes>>> getPostTemplates(
            @AuthenticationPrincipal SecurityUser securityUser
    ) {
        List<PostTemplateSummaryRes> response = postTemplateService.getTemplateListByMember(securityUser.getId());

        return ResponseEntity.ok(CommonResponse.success(response, "post template 리스트 조회"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Response<Void>> updatePostTemplate(
            @PathVariable(name = "id") Long id,
            @AuthenticationPrincipal SecurityUser securityUser,
            @Valid @RequestBody PostTemplateUpdateReq request
            ) {
        postTemplateService.updatePostTemplate(securityUser.getId(), id, request);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePostTemplate(
            @PathVariable(name = "id") Long id,
            @AuthenticationPrincipal SecurityUser securityUser
    ) {
        postTemplateService.deleteTemplate(securityUser.getId(), id);

        return ResponseEntity.noContent().build();
    }

}
