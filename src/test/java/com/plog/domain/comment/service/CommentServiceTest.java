package com.plog.domain.comment.service;

import com.plog.domain.comment.dto.CommentCreateReq;
import com.plog.domain.comment.dto.CommentInfoRes;
import com.plog.domain.comment.dto.ReplyInfoRes;
import com.plog.domain.comment.entity.Comment;
import com.plog.domain.comment.repository.CommentRepository;
import com.plog.domain.member.entity.Member;
import com.plog.domain.member.repository.MemberRepository;
import com.plog.domain.post.dto.PostInfoRes;
import com.plog.domain.post.entity.Post;
import com.plog.domain.post.entity.PostStatus;
import com.plog.domain.post.repository.PostRepository;
import com.plog.domain.post.service.PostService;
import com.plog.global.exception.exceptions.CommentException;
import com.plog.global.exception.exceptions.PostException;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

/**
 * 코드에 대한 전체적인 역할을 적습니다.
 * <p>
 * 코드에 대한 작동 원리 등을 적습니다.
 *
 * <p><b>상속 정보:</b><br>
 * 상속 정보를 적습니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@code ExampleClass(String example)}  <br>
 * 주요 생성자와 그 매개변수에 대한 설명을 적습니다. <br>
 *
 * <p><b>빈 관리:</b><br>
 * 필요 시 빈 관리에 대한 내용을 적습니다.
 *
 * <p><b>외부 모듈:</b><br>
 * 필요 시 외부 모듈에 대한 내용을 적습니다.
 *
 * @author njwwn
 * @see
 * @since 2026-01-21
 */
@SpringBootTest(properties = {
        "SECRET_KEY=plog_project_secret_key_for_test_1234567890_abcdefg", // 32자 이상 아무거나
        "MYSQL_HOST=localhost",
        "MYSQL_PORT=3306",
        "MYSQL_USER=root",
        "MYSQL_PWD=root",
        "MINIO_ACCESS_KEY=test",
        "MINIO_SECRET_KEY=test"
})
@ActiveProfiles("dev")
@Transactional
@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Autowired
    private EntityManager em;

    @Autowired
    private CommentService commentService;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private PostService postService;

    private Long savedPostId;
    private Member savedMember;

    private Post post;
    private Member author;

    @BeforeEach
    void setUp() {
        // 이제 .save() 호출 시 실제 DB에 저장되고 ID가 포함된 객체가 반환됩니다.
        author = memberRepository.save(Member.builder()
                .email("test" + System.currentTimeMillis() + "@example.com") // 중복 방지
                .password("password123")
                .nickname("테스터")
                .build());
        this.savedMember = author;

        post = Post.builder()
                .title("테스트 제목")
                .content("테스트 내용")
                .status(PostStatus.PUBLISHED) // 엔티티 설정에 따라 필수값 확인
                .build();

        Post savedPost = postRepository.save(post);
        this.savedPostId = savedPost.getId(); // 이제 savedPost가 null이 아니므로 통과!
    }

    @Test
    @DisplayName("게시글 상세 조회 시 조회수가 증가하고, 댓글과 대댓글 5개가 포함된다")
    void getPostDetail_Integration_Success() {
        // [Given] 1. 게시글 생성
        Post post = postRepository.save(Post.builder()
                .title("통합 테스트 제목")
                .content("내용")
                .status(PostStatus.PUBLISHED)
                .build());

        // [Given] 2. 부모 댓글 1개 생성
        Comment parent = commentRepository.save(Comment.builder()
                .post(post)
                .author(author)
                .content("부모 댓글")
                .build());

        // [Given] 3. 대댓글 10개 생성
        for (int i = 1; i <= 10; i++) {
            commentRepository.save(Comment.builder()
                    .post(post)
                    .author(author)
                    .parent(parent)
                    .content("대댓글 " + i)
                    .build());
        }

        // 영속성 컨텍스트 초기화 (조회수 증가 및 @Formula 반영 확인용)
        em.flush();
        em.clear();

        // [When] 게시글 상세 조회 실행
        PostInfoRes result = postService.getPostDetail(post.getId(), 0);

        // [Then] 1. 게시글 기본 정보 및 조회수 검증
        assertThat(result.title()).isEqualTo("통합 테스트 제목");
        assertThat(result.viewCount()).isEqualTo(1); // incrementViewCount 작동 확인

        // [Then] 2. 댓글 슬라이싱 검증
        Slice<CommentInfoRes> commentSlice = result.comments();
        assertThat(commentSlice.getContent()).hasSize(1);

        // [Then] 3. 대댓글 미리보기(5개) 및 전체 개수(10개) 검증
        CommentInfoRes commentDetail = commentSlice.getContent().get(0);
        assertThat(commentDetail.replyCount()).isEqualTo(10L); // @Formula
        assertThat(commentDetail.previewReplies().getContent()).hasSize(5); // 슬라이싱
        assertThat(commentDetail.previewReplies().hasNext()).isTrue();
        assertThat(commentDetail.previewReplies().getContent().get(0).content()).isEqualTo("대댓글 1");
    }

    @Test
    @DisplayName("부모 댓글 조회 시 대댓글은 최대 5개까지만 미리보기로 포함된다")
    void getComments_WithLimit5Replies() {
        // [Given] 1. 부모 댓글 1개 생성 및 저장
        Comment parent = commentRepository.save(Comment.builder()
                .post(post) // setUp에서 생성한 post
                .author(author) // setUp에서 생성한 member
                .content("부모 댓글")
                .build());

        // [Given] 2. 대댓글 10개 생성 및 저장
        for (int i = 1; i <= 10; i++) {
            commentRepository.save(Comment.builder()
                    .post(post)
                    .author(author)
                    .parent(parent) // 부모 지정
                    .content("대댓글 " + i)
                    .build());
        }

        // [Given] 3. 영속성 컨텍스트 초기화 (Formula 및 DB 쿼리 반영을 위해 필수)
        em.flush();
        em.clear();

        // [When] 서비스 호출
        Slice<CommentInfoRes> result = commentService.getCommentsByPostId(post.getId(), 0);

        // [Then]
        CommentInfoRes response = result.getContent().get(0);

        // 1. @Formula 검증: 전체 대댓글 개수는 10개여야 함
        assertThat(response.replyCount()).isEqualTo(10L);

        // 2. 미리보기 개수 검증: 리스트에는 딱 5개만 들어있어야 함
        assertThat(response.previewReplies().getContent()).hasSize(5);

        // 3. 슬라이싱 검증: 10개 중 5개만 가져왔으므로 다음 페이지가 있어야 함
        assertThat(response.previewReplies().hasNext()).isTrue();

        // 4. 내용 검증: 첫 번째 대댓글의 내용 확인
        assertThat(response.previewReplies().getContent().get(0).content()).isEqualTo("대댓글 1");
    }

    @Test
    @DisplayName("부모 댓글 조회 시 대댓글은 최대 5개까지만 포함되어야 한다 (Pre-fetch 로직)")
    void getComments_WithRepliesLimit5() {
        // given
        Long postId = 1L;
        int offset = 0;

        // 1. Given: 실제 데이터 준비
        // setUp에서 생성된 savedPostId와 savedMember를 사용합니다.
        Post post = postRepository.findById(savedPostId).orElseThrow();

        // 부모 댓글 저장 (id 필드는 save 후 자동으로 생깁니다)
        Comment parent = commentRepository.save(Comment.builder()
                .post(post)
                .author(savedMember)
                .content("부모 댓글")
                .build());

        // 대댓글 10개 저장
        for (int i = 1; i <= 10; i++) {
            commentRepository.save(Comment.builder()
                    .post(post)
                    .author(savedMember)
                    .parent(parent)
                    .content("대댓글 " + i)
                    .build());
        }
        commentRepository.flush();

        em.clear();

        // when
        Slice<CommentInfoRes> result = commentService.getCommentsByPostId(postId, offset);

        // then
        assertThat(result.getContent()).isNotEmpty();
        CommentInfoRes response = result.getContent().get(0);


        // 핵심 검증
        assertThat(response.replyCount()).isEqualTo(10); // 전체 개수는 10개 유지
        assertThat(response.previewReplies().getContent()).hasSize(5); // 미리보기는 5개로 제한
        assertThat(response.previewReplies().hasNext()).isTrue(); // 10개 중 5개만 가져왔으므로 다음이 있음
    }

    private Comment createCommentWithId(Long id, Comment parent) {
        Comment comment = Comment.builder()
                .content("테스트 댓글 " + id)
                // 필요한 다른 필드들 (author, post 등)이 있다면 여기에 추가
                .parent(parent)
                .build();

        // 1. private 필드인 id 강제 주입
        ReflectionTestUtils.setField(comment, "id", id);

        // 2. 대댓글(children) 리스트 초기화 (NPE 방지)
        // 실제 엔티티에서 new ArrayList<>()로 초기화되어 있지 않을 경우를 대비합니다.
        ReflectionTestUtils.setField(comment, "children", new ArrayList<Comment>());

        return comment;
    }

    private Comment createMockCommentWithReplies(int replyCount) {
        // 1. 가짜 작성자와 게시글 생성 (필요한 정보만 세팅)
        Member mockAuthor = Member.builder().nickname("작성자").build();
        ReflectionTestUtils.setField(mockAuthor, "id", 1L);

        Post mockPost = Post.builder().title("제목").build();
        ReflectionTestUtils.setField(mockPost, "id", 1L);

        // 2. 부모 댓글(Root Comment) 생성
        Comment parent = Comment.builder()
                .content("부모 댓글 내용")
                .author(mockAuthor)
                .post(mockPost)
                .build();
        ReflectionTestUtils.setField(parent, "id", 100L);

        // 3. 지정된 개수만큼 대댓글(Children) 생성 및 추가
        for (int i = 1; i <= replyCount; i++) {
            Comment child = Comment.builder()
                    .content("대댓글 " + i)
                    .author(mockAuthor)
                    .post(mockPost)
                    .parent(parent) // 부모 설정
                    .build();
        }

        commentRepository.flush();
        em.clear();

        return commentRepository.findById(parent.getId()).orElseThrow();
    }

    @Test
    @DisplayName("삭제된 대댓글은 previewReplies 결과에서 제외되어야 한다")
    void getComments_FilterDeletedChildren() {
        // 1. 데이터 준비
        Post post = postRepository.findById(savedPostId).orElseThrow();

        // 부모 댓글 저장
        Comment parent = commentRepository.save(Comment.builder()
                .content("부모 댓글")
                .post(post).author(savedMember).build());

        // 대댓글 2개 생성 (하나는 deleted = false, 하나는 deleted = true)
        commentRepository.save(Comment.builder()
                .content("보여야 하는 대댓글")
                .post(post)
                .author(savedMember)
                .parent(parent)
                .deleted(false)
                .build());

        commentRepository.save(Comment.builder()
                .content("숨겨야 하는 대댓글")
                .post(post)
                .author(savedMember)
                .parent(parent)
                .deleted(true)
                .build());

        em.flush();
        em.clear();

        // 2. 실행
        Slice<CommentInfoRes> result = commentService.getCommentsByPostId(savedPostId, 0);

        // 3. 검증
        CommentInfoRes parentDto = result.getContent().get(0);

        List<ReplyInfoRes> replies = parentDto.previewReplies().getContent();

        assertThat(replies).hasSize(1);
        assertThat(replies.get(0).content()).isEqualTo("보여야 하는 대댓글");

        // @Formula로 계산된 개수 검증 (삭제된 것 제외 1개)
        assertThat(parentDto.replyCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("댓글 조회 시 대댓글은 삭제되지 않은 것 중 최대 5개만 포함되어야 한다")
    void getComments_Success_WithLimitAndFilter() {
        // 1. 데이터 준비 (부모 댓글 5개 생성)
        Post post = postRepository.findById(savedPostId).orElseThrow();

        for (int i = 1; i <= 5; i++) {
            commentRepository.save(Comment.builder()
                    .content("루트 댓글 " + i)
                    .post(post)
                    .author(savedMember)
                    .parent(null) // 부모 없음 (루트 댓글)
                    .deleted(false)
                    .build());
        }

        // 2. 중요: DB에 강제로 반영하고 캐시를 비웁니다.
        // 이렇게 해야 SELECT 쿼리가 실제 DB 데이터를 정확히 긁어옵니다.
        em.flush();
        em.clear();

        // 3. 실행
        PageRequest pageRequest = PageRequest.of(0, 5);
        Slice<CommentInfoRes> result = commentService.getCommentsByPostId(savedPostId, 0);

        // 4. 검증
        assertThat(result.getContent()).hasSize(5); // 이제 0이 아닌 5가 나올 것입니다.
        assertThat(result.hasNext()).isFalse(); // 5개 요청에 5개 왔으니 다음 페이지 없음
    }

    @Test
    @DisplayName("루트 댓글이 여러 개일 때 대댓글 조회 시 Batch Fetching이 작동해야 한다")
    void getComments_BatchFetching_Check() {
        // given: 루트 댓글 3개 생성, 각각 대댓글 3개씩 보유
        for (int i = 0; i < 3; i++) {
            Comment p = createCommentEntity(null, "부모 " + i);
            for (int j = 0; j < 3; j++) {
                createCommentEntity(p, "자식 " + j);
            }
        }

        // when
        // 이 시점에 하이버네이트 로그를 확인하세요!
        Slice<CommentInfoRes> result = commentService.getCommentsByPostId(savedPostId, 0);

        // then
        assertThat(result.getContent()).hasSize(3);
        // 로그에 'where parent_id in (?, ?, ?)' 형태의 쿼리가 1번만 찍혀야 함 (N+1 방지 확인)
    }

    private Comment createCommentEntity(Comment parent, String content) {
        Comment comment = Comment.builder()
                .content(content)
                .post(postRepository.getReferenceById(savedPostId))
                .author(memberRepository.findAll().get(0))
                .parent(parent)
                .build();
        return commentRepository.save(comment);
    }


    @Test
    @DisplayName("댓글 생성 성공: 부모 댓글이 없는 일반 댓글을 저장한다")
    void createComment_Success() {
        // given
        Long postId = 1L;
        CommentCreateReq req = new CommentCreateReq("댓글 내용", 1L, null);

        Post post = Post.builder().title("제목").build();
        ReflectionTestUtils.setField(post, "id", postId);
        Comment savedComment = Comment.builder().id(100L).content(req.content()).post(post).build();

        // Mocking: postRepository가 post를 반환하도록 설정
        given(postRepository.findById(postId)).willReturn(Optional.of(post));
        // Mocking: commentRepository가 저장된 객체를 반환하도록 설정
        given(commentRepository.save(any(Comment.class))).willReturn(savedComment);

        // when
        Long resultId = commentService.createComment(postId, req);

        // then
        assertThat(resultId).isEqualTo(100L);
        verify(postRepository, times(1)).findById(postId);
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    @DisplayName("대댓글 생성 성공: 부모 댓글이 존재하는 경우 함께 저장한다")
    void createSubComment_Success() {
        // given
        Long postId = 1L;
        Long parentId = 50L;
        CommentCreateReq req = new CommentCreateReq("대댓글 내용", 1L, parentId);

        Post post = Post.builder().title("제목").build();
        ReflectionTestUtils.setField(post, "id", postId);
        Comment parentComment = Comment.builder().id(parentId).build();
        Comment subComment = Comment.builder().id(101L).parent(parentComment).build();

        given(postRepository.findById(postId)).willReturn(Optional.of(post));
        given(commentRepository.findById(parentId)).willReturn(Optional.of(parentComment));
        given(commentRepository.save(any(Comment.class))).willReturn(subComment);

        // when
        Long resultId = commentService.createComment(postId, req);

        // then
        assertThat(resultId).isEqualTo(101L);
        verify(commentRepository).findById(parentId); // 부모 댓글 조회 로직 확인
    }

    @Test
    @DisplayName("댓글 생성 실패: 부모 댓글 ID가 존재하지 않으면 예외가 발생한다")
    void createComment_Fail_ParentNotFound() {
        // given
        Long postId = 1L;
        Long invalidParentId = 999L;
        CommentCreateReq req = new CommentCreateReq("내용", 1L, invalidParentId);

        given(postRepository.findById(postId)).willReturn(Optional.of(Post.builder().build()));
        // 부모 댓글이 없다고 가정
        given(commentRepository.findById(invalidParentId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> commentService.createComment(postId, req))
                .isInstanceOf(CommentException.class)
                .hasMessageContaining("부모 댓글이 존재하지 않습니다.");
    }

    @Test
    @DisplayName("댓글 조회 시 게시글이 존재하지 않으면 예외가 발생한다.")
    void getComments_fail_postNotFound() {
        // given
        // 실제 DB에 존재하지 않을 것이 확실한 ID를 사용합니다.
        Long nonExistentPostId = 999_999L;

        // when & then
        // 별도의 Mocking 없이 바로 서비스를 호출하여 예외 발생을 확인합니다.
        assertThatThrownBy(() -> commentService.getCommentsByPostId(nonExistentPostId, 0))
                .isInstanceOf(PostException.class);
    }

    @Test
    @DisplayName("특정 댓글의 대댓글만 5개씩 페이징 조회한다.")
    void getReplies_paging_success() {
        // 1. 데이터 준비: 부모 댓글 1개와 대댓글 8개 생성
        Post post = postRepository.findById(savedPostId).orElseThrow();
        Comment parent = commentRepository.save(Comment.builder()
                .content("부모 댓글")
                .post(post).author(savedMember).build());

        for (int i = 1; i <= 8; i++) {
            commentRepository.save(Comment.builder()
                    .content("대댓글 " + i)
                    .post(post).author(savedMember)
                    .parent(parent).build());
        }

        em.flush();
        em.clear();

        // 2. 실행: 첫 번째 페이지(size 5) 조회
        // commentService에 대댓글만 따로 페이징 조회하는 메서드가 있다고 가정합니다 (예: getReplies)
        Slice<ReplyInfoRes> result = commentService.getRepliesByCommentId(parent.getId(), 0);

        // 3. 검증
        assertThat(result.getContent()).hasSize(5); // 첫 페이지 5개
        assertThat(result.hasNext()).isTrue();      // 다음 페이지(남은 3개)가 있으므로 true
        assertThat(result.getContent().get(0).content()).isEqualTo("대댓글 1");
    }


    @Test
    @DisplayName("댓글 수정에 성공한다.")
    void updateComment_success() {
        // given
        Long commentId = 1L;
        String newContent = "수정된 댓글 내용입니다.";
        Comment comment = Comment.builder()
                .id(commentId)
                .content("원래 내용")
                .build();

        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

        // when
        commentService.updateComment(commentId, newContent);

        // then
        assertThat(comment.getContent()).isEqualTo(newContent);
    }

    @Test
    @DisplayName("자식 댓글이 있는 댓글을 삭제하면 Soft Delete 된다.")
    void deleteComment_softDelete() {
        // given
        Long commentId = 1L;
        Comment comment = Comment.builder().id(commentId).content("부모 댓글").build();

        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));
        // 자식 댓글이 존재한다고 설정
        given(commentRepository.existsByParent(comment)).willReturn(true);

        // when
        commentService.deleteComment(commentId);

        // then
        assertThat(comment.isDeleted()).isTrue();
        assertThat(comment.getContent()).contains("삭제된 댓글입니다");
        // 실제 DB 삭제 메서드는 호출되지 않아야 함
        verify(commentRepository, never()).delete(any(Comment.class));
    }

    @Test
    @DisplayName("자식 댓글이 없는 댓글을 삭제하면 DB에서 완전히 삭제(Hard Delete) 된다.")
    void deleteComment_hardDelete() {
        // given
        Long commentId = 1L;
        Comment comment = Comment.builder().id(commentId).content("일반 댓글").build();

        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));
        // 자식 댓글이 없다고 설정
        given(commentRepository.existsByParent(comment)).willReturn(false);

        // when
        commentService.deleteComment(commentId);

        // then
        // delete 메서드가 해당 객체로 호출되었는지 검증
        verify(commentRepository, times(1)).delete(comment);
    }

    @Test
    @DisplayName("이미 삭제된 댓글을 다시 삭제 요청하면 아무 일도 일어나지 않는다.")
    void deleteComment_alreadyDeleted() {
        // given
        Long commentId = 1L;
        Comment comment = Comment.builder().id(commentId).build();
        comment.softDelete(); // 이미 삭제 상태

        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

        // when
        commentService.deleteComment(commentId);

        // then
        verify(commentRepository, never()).existsByParent(any());
        verify(commentRepository, never()).delete(any());
    }

    @Test
    @DisplayName("게시글의 최상위 댓글 목록을 페이징 조회한다.")
    void getComments_success() {
        // given
        Long postId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        Post post = Post.builder().title("제목").build();
        ReflectionTestUtils.setField(post, "id", postId);

        Member author = Member.builder().nickname("작성자").build();
        ReflectionTestUtils.setField(author, "id", 1L);

        Comment comment = Comment.builder().post(post).author(author).content("루트댓글").build();
        ReflectionTestUtils.setField(comment, "id", 100L);

        given(postRepository.findById(postId)).willReturn(Optional.of(post));
        given(commentRepository.findByPostIdAndParentIsNull(postId, pageable))
                .willReturn(new SliceImpl<>(List.of(comment), pageable, false));

        // when
        Slice<CommentInfoRes> result = commentService.getCommentsByPostId(postId, 0);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).content()).isEqualTo("루트댓글");
    }

}
