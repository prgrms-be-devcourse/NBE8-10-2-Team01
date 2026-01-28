package com.plog.domain.post.service;

import com.plog.domain.post.dto.PostTemplateInfoDto;
import com.plog.domain.post.dto.PostTemplateSummaryRes;
import com.plog.domain.post.dto.PostTemplateUpdateReq;

import java.util.List;

/**
 * 게시글 템플릿(Post Template)에 대한 비즈니스 기능을 정의하는 서비스 인터페이스입니다.
 *
 * <p>
 * 게시글 템플릿은 사용자가 자주 사용하는 게시글 구조를 저장해두고,
 * 이후 게시글 작성 시 재사용하기 위한 목적의 도메인입니다.
 * 본 인터페이스는 템플릿의 생성, 조회, 수정, 삭제에 대한 기능 계약을 정의합니다.
 *
 * @author jack8
 * @since 2026-01-26
 */
public interface PostTemplateService {

    /**
     * 게시글 템플릿을 생성합니다.
     *
     * @param dto 생성할 템플릿 정보
     * @return 생성된 템플릿의 식별자
     */
    Long createPostTemplate(Long memberId, PostTemplateInfoDto dto);


    /**
     * 특정 회원이 보유한 게시글 템플릿 목록을 조회합니다.
     *
     * @param memberId 회원 식별자
     * @return 템플릿 요약 정보 목록
     */
    List<PostTemplateSummaryRes> getTemplateListByMember(Long memberId);


    /**
     * 게시글 템플릿을 수정합니다.
     *
     * @param memberId 수정 요청 당사자
     * @param templateId 수정한 템플릿 아이디
     * @param dto 수정할 템플릿 정보
     */
    void updatePostTemplate(Long memberId, Long templateId, PostTemplateUpdateReq dto);


    /**
     * 게시글 템플릿 단건을 조회합니다.
     *
     * @param templateId 템플릿 식별자
     * @return 템플릿 상세 정보
     */
    PostTemplateInfoDto getTemplate(Long memberId, Long templateId);


    /**
     * 게시글 템플릿을 삭제합니다.
     *
     * @param templateId 삭제할 템플릿 식별자
     */
    void deleteTemplate(Long memberId, Long templateId);

    /**
     * 회원가입 시 사용자에게 기본 템플릿을 추가합니다.
     * @param memberId 추가할 사용자 아이디
     */
    void initTemplateSeedOfUser(Long memberId);
}
