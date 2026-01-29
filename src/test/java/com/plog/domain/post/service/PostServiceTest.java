package com.plog.domain.post.service;

import com.plog.domain.comment.repository.CommentRepository;
import com.plog.domain.hashtag.repository.PostHashTagRepository;
import com.plog.domain.member.entity.Member;
import com.plog.domain.member.repository.MemberRepository;
import com.plog.domain.post.dto.PostCreateReq;
import com.plog.domain.post.dto.PostInfoRes;
import com.plog.domain.post.dto.PostListRes;
import com.plog.domain.post.dto.PostUpdateReq;
import com.plog.domain.post.entity.Post;
import com.plog.domain.post.repository.PostRepository;
import com.plog.global.exception.exceptions.AuthException;
import com.plog.global.exception.exceptions.PostException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class PostServiceTest {
    @InjectMocks
    private PostServiceImpl postService;

    @Mock
    private PostRepository postRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostHashTagRepository postHashTagRepository;

    @Test
    @DisplayName("게시글 저장 시 마크다운이 제거된 요약글이 자동 생성")
    void createPostSuccess() {

        Long memberId = 1L;
        PostCreateReq requestDto = new PostCreateReq("테스트 제목", "# Hello\n**Spring Boot**", List.of(), "example.com");

        Member mockMember = Member.builder().build();

        ReflectionTestUtils.setField(mockMember, "id", memberId);

        given(memberRepository.getReferenceById(memberId)).willReturn(mockMember);

        given(postRepository.save(any(Post.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        postService.createPost(memberId, requestDto);

        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(postCaptor.capture());

        Post savedPost = postCaptor.getValue();
        assertThat(savedPost.getTitle()).isEqualTo("테스트 제목");
        assertThat(savedPost.getSummary()).isEqualTo("Hello\nSpring Boot");
        assertThat(savedPost.getMember().getId()).isEqualTo(memberId);
    }

    @Test
    @DisplayName("본문이 150자를 초과하면 요약글은 150자까지만 저장되고 말줄임표가 붙는다")
    void createPostSuccessSummaryTruncation() {

        Long memberId = 1L;
        String longContent = "가".repeat(200);
        PostCreateReq requestDto = new PostCreateReq("제목", longContent, List.of(), "example.com");

        Member mockMember = Member.builder().build();

        ReflectionTestUtils.setField(mockMember, "id", memberId);

        given(memberRepository.getReferenceById(memberId)).willReturn(mockMember);

        given(postRepository.save(any(Post.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        postService.createPost(memberId, requestDto);

        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(postCaptor.capture());

        Post savedPost = postCaptor.getValue();

        assertThat(savedPost.getSummary().length()).isEqualTo(153);
        assertThat(savedPost.getSummary()).endsWith("...");
    }

    @Test
    @DisplayName("전체 게시글 조회 시 리포지토리의 결과를 Slice DTO로 변환하여 반환한다")
    void getPostsSuccess() {
        // [Given]
        Member author = new Member("email", "password", "nickname", null);
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id"));
        Post post = Post.builder()
                .title("테스트 제목")
                .content("테스트 내용")
                .member(author)
                .build();

        // 리포지토리는 Page를 반환 (Page는 Slice를 상속함)
        Page<Post> mockPage = new PageImpl<>(List.of(post), pageable, 1);

        given(postRepository.findAllWithMember(any(Pageable.class))).willReturn(mockPage);

        // [When]
        Slice<PostListRes> result = postService.getPosts(pageable);

        // [Then]
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).title()).isEqualTo("테스트 제목");
        assertThat(result.getNumber()).isEqualTo(0);
        assertThat(result.isLast()).isTrue();

        verify(postRepository).findAllWithMember(pageable);
    }

    @Test
    @DisplayName("게시글 수정 시 본문에 맞춰 요약본이 새롭게 생성되어야 한다")
    void updatePostSuccess() {
        // [Given]
        Long memberId = 1L;
        Long postId = 1L;

        Member member = Member.builder().build();
        ReflectionTestUtils.setField(member, "id", memberId);

        Post existingPost = Post.builder()
                .title("기존 제목")
                .content("기존 본문")
                .member(member)
                .summary("기존 요약")
                .build();

        given(postRepository.findById(postId)).willReturn(Optional.of(existingPost));

        String newTitle = "수정된 제목";
        String newContent = "수정된 본문 내용입니다. 이 내용은 150자 미만이므로 그대로 요약이 됩니다.";

        // [When]
        postService.updatePost(memberId, postId, new PostUpdateReq(newTitle, newContent, null, null));

        // [Then]
        // 더티 체킹에 의해 변경될 엔티티의 상태를 검증합니다.
        assertThat(existingPost.getTitle()).isEqualTo(newTitle);
        assertThat(existingPost.getContent()).isEqualTo(newContent);
        assertThat(existingPost.getSummary()).contains("수정된 본문"); // 요약본 갱신 확인
    }

    @Test
    @DisplayName("존재하지 않는 게시글 수정 시 PostException이 발생한다")
    void updatePostFailNotFound() {
        // [Given]
        Long memberId = 1L;
        given(postRepository.findById(anyLong())).willReturn(Optional.empty());

        // [When & Then]
        assertThatThrownBy(() -> postService.updatePost(memberId, 99L, new PostUpdateReq("제목", "내용", null, null)))
                .isInstanceOf(PostException.class)
                .hasMessageContaining("존재하지 않는 게시물입니다.");
    }

    @Test
    @DisplayName("작성자가 아닌 사용자가 게시글 수정을 시도하면, AuthException이 발생한다")
    void updatePostFailForbidden() {
        // [Given]
        Long ownerId = 1L;
        Long otherMemberId = 2L;
        Long postId = 1L;

        Member owner = Member.builder().build();

        ReflectionTestUtils.setField(owner, "id", ownerId);

        Post post = Post.builder().member(owner).build();

        given(postRepository.findById(postId)).willReturn(Optional.of(post));

        // [When & Then]
        assertThatThrownBy(() -> postService.updatePost(otherMemberId, postId, new PostUpdateReq("제목", "내용", null, null)))
                .isInstanceOf(AuthException.class)
                .hasMessageContaining("수정할 권한이 없습니다.");
    }

    @Test
    @DisplayName("게시글 삭제 시 해당 ID의 게시글이 존재하면 삭제를 수행한다")
    void deletePostSuccess() {
        // [Given]
        Long memberId = 1L;
        Long postId = 1L;

        Member member = Member.builder().build();
        ReflectionTestUtils.setField(member, "id", memberId);

        Post post = Post.builder()
                .title("삭제될 제목")
                .content("삭제될 본문")
                .member(member)
                .build();

        // findById 호출 시 삭제할 게시글이 있다고 가정합니다.
        given(postRepository.findById(postId)).willReturn(Optional.of(post));

        // [When]
        postService.deletePost(memberId, postId);

        // [Then]
        // 1. findById가 호출되었는지 확인
        verify(postRepository).findById(postId);
        // 2. 실제 리포지토리의 delete 메서드가 해당 엔티티로 호출되었는지 확인
        verify(postRepository).delete(post);
        verify(commentRepository).deleteParentsByPostId(postId);
        verify(commentRepository).deleteRepliesByPostId(postId);
    }

    @Test
    @DisplayName("존재하지 않는 ID로 삭제 요청 시 PostException이 발생한다")
    void deletePostFailNotFound() {
        // [Given]
        Long memberId = 1L;
        Long postId = 99L;

        // findById 호출 시 빈 값을 반환한다고 가정합니다.
        given(postRepository.findById(postId)).willReturn(Optional.empty());

        // [When & Then]
        // 예외가 발생하는지 확인합니다.
        assertThatThrownBy(() -> postService.deletePost(memberId, postId))
                .isInstanceOf(PostException.class)
                .hasMessageContaining("존재하지 않는 게시물입니다.");

        // 예외가 발생했으므로 실제 delete 메서드는 호출되지 않아야 합니다.
        verify(postRepository, never()).delete(any(Post.class));
    }

    @Test
    @DisplayName("작성자가 아닌 사용자가 게시글 삭제를 시도하면 AuthException이 발생한다")
    void deletePostFailForbidden() {
        // [Given]
        Long ownerId = 1L;
        Long otherMemberId = 2L;
        Long postId = 1L;

        Member owner = Member.builder().build();
        ReflectionTestUtils.setField(owner, "id", ownerId);

        Post post = Post.builder().member(owner).build();

        given(postRepository.findById(postId)).willReturn(Optional.of(post));

        // [When & Then]
        assertThatThrownBy(() -> postService.deletePost(otherMemberId, postId))
                .isInstanceOf(AuthException.class) //
                .hasMessageContaining("삭제할 권한이 없습니다."); //

        verify(postRepository, never()).delete(any(Post.class));
    }

    @Test
    @DisplayName("회원 ID로 조회 시 엔티티가 PostInfoRes의 모든 필드로 올바르게 변환되어야 한다")
    void getPostsByMemberSuccess() {
        // [Given]
        Long memberId = 1L;
        Member author = new Member("email", "password", "nickname", null);
        // 페이징 정보 설정 (0페이지, 10개씩 조회)
        Pageable pageable = PageRequest.of(0, 10);

        Post post = Post.builder()
                .title("테스트 제목")
                .content("테스트 본문")
                .summary("테스트 요약")
                .member(author)
                .viewCount(10)
                .build();

        // SliceImpl을 사용하여 리포지토리 반환값 모킹 (데이터 1개, 다음 페이지 없음)
        Slice<Post> mockSlice = new SliceImpl<>(List.of(post), pageable, false);

        given(postRepository.findAllByMemberId(memberId, pageable))
                .willReturn(mockSlice);

        // [When]
        Slice<PostInfoRes> result = postService.getPostsByMember(memberId, pageable);

        // [Then]
        // 1. Slice 자체에 대한 검증
        assertThat(result.getContent()).hasSize(1); // 실제 데이터 개수 확인
        assertThat(result.hasNext()).isFalse();    // 다음 페이지 여부 확인

        // 2. DTO 필드 매핑 검증 (첫 번째 요소 추출)
        PostInfoRes dto = result.getContent().get(0);
        assertThat(dto.title()).isEqualTo("테스트 제목");
        assertThat(dto.content()).isEqualTo("테스트 본문");
        assertThat(dto.viewCount()).isEqualTo(10);

        // 3. 리포지토리 호출 확인 (새로운 메서드와 파라미터 기준)
        verify(postRepository).findAllByMemberId(memberId, pageable);
    }
}
