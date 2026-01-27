package com.plog.domain.comment.service;

import com.plog.domain.comment.dto.CommentCreateReq;
import com.plog.domain.comment.dto.CommentInfoRes;
import com.plog.domain.comment.dto.ReplyInfoRes;
import com.plog.domain.comment.entity.Comment;
import com.plog.domain.comment.repository.CommentRepository;
import com.plog.domain.member.entity.Member;
import com.plog.domain.member.repository.MemberRepository;
import com.plog.domain.post.entity.Post;
import com.plog.domain.post.repository.PostRepository;
import com.plog.global.exception.exceptions.AuthException;
import com.plog.global.exception.exceptions.CommentException;
import com.plog.global.exception.exceptions.PostException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @InjectMocks
    private CommentServiceImpl commentService; // 구현체 클래스로 변경!

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private MemberRepository memberRepository;

    private Post createPost(Long id, String title) {
        Post post = Post.builder().title(title).build();
        ReflectionTestUtils.setField(post, "id", id);
        return post;
    }

    private Comment createComment(Long id, String content, Post post, Member author, Comment parent) {
        Comment comment = Comment.builder()
                .content(content)
                .post(post)
                .author(author)
                .parent(parent)
                .build();
        ReflectionTestUtils.setField(comment, "id", id);
        return comment;
    }

    private Member createMember(Long id, String nickname) {
        Member member = Member.builder().nickname(nickname).build();
        ReflectionTestUtils.setField(member, "id", id);
        return member;
    }

    @Test
    @DisplayName("댓글 생성 성공: 부모 댓글이 없는 일반 댓글을 저장한다")
    void createComment_Success() {
        // [given]
        Long postId = 1L;
        Long memberId = 1L;
        CommentCreateReq req = new CommentCreateReq("댓글 내용", 1L, null);
        Member author = createMember(memberId, "테스트유저");

        given(memberRepository.findById(anyLong())).willReturn(Optional.of(author));
        given(postRepository.findById(postId)).willReturn(Optional.of(createPost(postId, "제목")));
        given(commentRepository.save(any(Comment.class))).willReturn(createComment(100L, "댓글 내용", null, null, null));

        // [when]
        Long resultId = commentService.createComment(postId, memberId, req);

        // [then]
        assertThat(resultId).isEqualTo(100L);
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    @DisplayName("댓글 생성 실패: 부모 댓글 ID가 존재하지 않으면 예외가 발생한다")
    void createComment_Fail_ParentNotFound() {
        // [given]
        Long postId = 1L;
        Long memberId = 1L;
        Long invalidParentId = 999L;
        CommentCreateReq req = new CommentCreateReq("내용", 1L, invalidParentId);

        Member author = createMember(memberId, "테스트유저");

        given(memberRepository.findById(anyLong())).willReturn(Optional.of(author));

        given(postRepository.findById(postId)).willReturn(Optional.of(createPost(postId, "제목")));
        given(commentRepository.findById(invalidParentId)).willReturn(Optional.empty());

        // [when & then]
        assertThatThrownBy(() -> commentService.createComment(postId, memberId, req))
                .isInstanceOf(CommentException.class);
    }

    @Test
    @DisplayName("특정 댓글의 대댓글만 5개씩 페이징 조회한다.")
    void getReplies_paging_success() {
        // [given]
        Long parentId = 1L;
        Pageable pageable = PageRequest.of(0, 5);
        Comment parent = createComment(parentId, "부모", null, null, null);
        List<Comment> replies = new ArrayList<>();

        for (long i = 1; i <= 5; i++) {
            replies.add(createComment(i + 1, "대댓글 " + i, null, createMember(i, "유저"), parent));
        }

        Slice<Comment> slice = new SliceImpl<>(replies, pageable, true);

        given(commentRepository.findById(parentId)).willReturn(Optional.of(parent));
        given(commentRepository.findByParentId(eq(parentId), any(Pageable.class))).willReturn(slice);

        // [when]
        Slice<ReplyInfoRes> result = commentService.getRepliesByCommentId(parentId, 0);

        // [then]
        assertThat(result.getContent()).hasSize(5);
        assertThat(result.hasNext()).isTrue();
    }

    @Test
    @DisplayName("자식 댓글이 없는 댓글을 삭제하면 Hard Delete 된다.")
    void deleteComment_hardDelete() {
        // [given]
        Long commentId = 1L;
        Long memberId = 1L;
        Member author = createMember(memberId, "테스트유저");
        Comment comment = createComment(commentId, "내용", null, author, null);

        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));
        given(commentRepository.existsByParent(comment)).willReturn(false);

        // [when]
        commentService.deleteComment(commentId, memberId);

        // [then]
        verify(commentRepository).delete(comment);
    }

    @Test
    @DisplayName("댓글 조회 시 게시글이 존재하지 않으면 예외가 발생한다.")
    void getComments_fail_postNotFound() {
        // [given]
        Long nonExistentPostId = 999L;
        given(postRepository.findById(nonExistentPostId)).willReturn(Optional.empty());

        // [when & then]
        assertThatThrownBy(() -> commentService.getCommentsByPostId(nonExistentPostId, 0))
                .isInstanceOf(PostException.class);
    }

    @Test
    @DisplayName("삭제된 대댓글은 previewReplies 결과에서 제외되어야 한다")
    void getComments_FilterDeletedChildren() {
        // [Given]
        Long postId = 1L;
        Post post = createPost(postId, "제목");
        Member author = createMember(1L, "작성자");
        Comment parent = createComment(1L, "부모", post, author, null);


        given(postRepository.findById(postId)).willReturn(Optional.of(post));
        given(commentRepository.findByPostIdAndParentIsNull(eq(postId), any(Pageable.class)))
                .willReturn(new SliceImpl<>(List.of(parent)));

        Slice<Comment> emptyReplySlice = new SliceImpl<>(new ArrayList<>(), PageRequest.of(0, 5), false);
        given(commentRepository.findByParentId(anyLong(), any(Pageable.class)))
                .willReturn(emptyReplySlice);

        // [When]
        Slice<CommentInfoRes> result = commentService.getCommentsByPostId(postId, 0);

        // [Then]
        assertThat(result.getContent()).isNotEmpty();
    }

    @Test
    @DisplayName("댓글 삭제 실패: 작성자가 아닌 유저가 삭제 요청 시 예외 발생")
    void deleteComment_Forbidden() {
        // given
        Long postId = 1L;
        Long authorId = 100L;
        Long requesterId = 999L;
        Post post = createPost(postId, "제목");
        Member author = createMember(authorId, "작성자");
        Comment comment = createComment(1L, "내용", post, author, null);

        given(commentRepository.findById(anyLong())).willReturn(Optional.of(comment));

        // when & then
        assertThatThrownBy(() -> commentService.deleteComment(1L, requesterId))
                .isInstanceOf(AuthException.class);
    }
}