package com.plog.domain.post.service;

import com.plog.domain.member.entity.Member;
import com.plog.domain.member.repository.MemberRepository;
import com.plog.domain.post.dto.PostTemplateInfoDto;
import com.plog.domain.post.dto.PostTemplateSeed;
import com.plog.domain.post.dto.PostTemplateSummaryRes;
import com.plog.domain.post.dto.PostTemplateUpdateReq;
import com.plog.domain.post.entity.PostTemplate;
import com.plog.domain.post.repository.PostTemplateRepository;
import com.plog.global.exception.errorCode.PostErrorCode;
import com.plog.global.exception.exceptions.PostException;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 게시글 템플릿(PostTemplate)에 대한 비즈니스 로직을 처리하는 서비스 구현체입니다.
 *
 * <p>
 * 사용자가 저장한 게시글 템플릿을 생성, 조회, 수정, 삭제하는 기능을 제공하며,
 * 모든 변경·조회 작업에 대해 템플릿 소유자 검증을 수행합니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@link PostTemplateService} 인터페이스를 구현합니다.
 *
 * <p><b>빈 관리:</b><br>
 * {@code @Service}로 등록되어 Spring 컨테이너에서 관리되며,
 * 트랜잭션 경계는 메서드 단위로 설정됩니다.
 *
 * @author jack8
 * @since 2026-01-26
 */
@Service
@RequiredArgsConstructor
public class PostTemplateServiceImpl implements PostTemplateService {

    private final PostTemplateRepository postTemplateRepository;
    private final MemberRepository memberRepository;

    @Getter
    private List<PostTemplateSeed> seeds;

    @PostConstruct
    public void init() {
        try {
            PathMatchingResourcePatternResolver resolver =
                    new PathMatchingResourcePatternResolver();

            Resource[] resources = resolver.getResources("classpath:postTemplate/*.md");
            List<PostTemplateSeed> loads = new ArrayList<>();

            for(Resource resource : resources) {
                String filename = resource.getFilename();
                if(filename == null) continue;

                String title = filename.replace(".md", "").replace("_", " ");
                String context = new String(
                        resource.getInputStream().readAllBytes(),
                        StandardCharsets.UTF_8
                );

                loads.add(PostTemplateSeed.builder()
                        .title(title)
                        .name(title)
                        .content(context)
                        .build());
            }
            this.seeds = List.copyOf(loads);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "[PostTemplateServiceImpl#init] failed to load postTemplate/*.md", e
            );
        }
    }


    @Override
    @Transactional
    public Long createPostTemplate(Long memberId, PostTemplateInfoDto dto) {
        Member author = memberRepository.getReferenceById(memberId);

        PostTemplate postTemplate = PostTemplate.builder()
                .name(dto.name())
                .title(dto.title())
                .content(dto.content())
                .member(author)
                .build();

        PostTemplate saved = postTemplateRepository.save(postTemplate);
        return saved.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostTemplateSummaryRes> getTemplateListByMember(Long memberId) {
        List<PostTemplate> getList = postTemplateRepository.findAllByMember_Id(memberId);

        return getList.stream().map(PostTemplateSummaryRes::to).toList();
    }

    @Override
    @Transactional
    public void updatePostTemplate(Long memberId, Long templateId, PostTemplateUpdateReq dto) {
        PostTemplate postTemplate = findByTemplateId(templateId);

        validateOwner(memberId, postTemplate);

        postTemplate.update(dto.name(), dto.title(), dto.content());

        postTemplateRepository.save(postTemplate);
    }

    @Override
    @Transactional(readOnly = true)
    public PostTemplateInfoDto getTemplate(Long memberId, Long templateId) {
        PostTemplate postTemplate = findByTemplateId(templateId);

        validateOwner(memberId, postTemplate);

        return PostTemplateInfoDto.to(postTemplate);
    }

    @Override
    @Transactional
    public void deleteTemplate(Long memberId, Long templateId) {
        PostTemplate postTemplate = findByTemplateId(templateId);

        validateOwner(memberId, postTemplate);

        postTemplateRepository.delete(postTemplate);

    }

    @Override
    @Transactional
    public void initTemplateSeedOfUser(Long memberId) {
        Member member = memberRepository.getReferenceById(memberId);
        List<PostTemplate> templates = new ArrayList<>();
        seeds.forEach(seed -> templates.add(PostTemplate.builder()
                .member(member)
                .name(seed.name())
                .title(seed.title())
                .content(seed.content())
                .build()));

        postTemplateRepository.saveAll(templates);
    }

    private PostTemplate findByTemplateId(Long id) {
        return postTemplateRepository.findById(id)
                .orElseThrow(() -> new PostException(PostErrorCode.POST_TEMPLATE_NOT_FOUND,
                        "[PostTemplateServiceImpl#findByTemplateId] unknown template id = " + id));
    }

    private void validateOwner(Long memberId, PostTemplate template) {
        if(!Objects.equals(template.getMember().getId(), memberId)) {
            throw new PostException(PostErrorCode.POST_TEMPLATE_AUTH_FAIL,
                    "[PostTemplateServiceImpl#validateOwner] request user is " + memberId +
                    " , but actual owner is " + template.getMember().getId());
        }
    }
}
