package com.plog.domain.comment;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.plog.domain.comment.dto.CommentCreateReq;
import com.plog.domain.comment.dto.CommentInfoRes;
import com.plog.domain.comment.dto.CommentUpdateReq;
import com.plog.domain.comment.entity.Comment;
import com.plog.domain.comment.repository.CommentRepository;
import com.plog.domain.comment.service.CommentService;
import com.plog.domain.member.entity.Member;
import com.plog.domain.member.repository.MemberRepository;
import com.plog.domain.post.entity.Post;
import com.plog.domain.post.entity.PostStatus;
import com.plog.domain.post.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.Slice;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper; // 이 패키지입니다.
import org.springframework.beans.factory.annotation.Autowired;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
// 또는 다른 메서드(get, put, delete)도 함께 쓰려면 아래를 권장합니다.
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

// 추가적으로 결과 검증을 위한 jsonPath와 status도 필요합니다.
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import org.springframework.http.MediaType;
import static org.assertj.core.api.Assertions.assertThat;
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
@SpringBootTest
@AutoConfigureMockMvc
@Transactional // 테스트 후 DB 롤백을 위해 필수
@ActiveProfiles("test")
@WithMockUser(username = "test@example.com")

class CommentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // @Autowired 제거 후 직접 생성
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule()); // 날짜 데이터 처리를 위해 필요

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private CommentService commentService;

    private Member testMember;
    private Post testPost;


    @Test
    @DisplayName("BatchSize 설정으로 인해 대댓글 조회 시 N+1이 발생하지 않아야 한다")
    void checkBatchFetchEfficiency() {
        // given: 댓글 3개 생성, 각 댓글당 대댓글 5개씩 생성
        Post post = postRepository.save(Post.builder().title("테스트").build());
        for (int i = 0; i < 3; i++) {
            Comment parent = commentRepository.save(Comment.builder().post(post).content("부모"+i).build());
            for (int j = 0; j < 5; j++) {
                commentRepository.save(Comment.builder().post(post).parent(parent).content("자식"+j).build());
            }
        }

        // when
        // Hibernate 로그를 통해 IN 절 쿼리가 나가는지 확인하세요.
        Slice<CommentInfoRes> result = commentService.getCommentsByPostId(post.getId(), 0);

        // then
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getContent().get(0).previewReplies().getContent()).hasSize(5);
    }



    @BeforeEach
    void setUp() {
        // 1. 멤버 생성 및 저장
        testMember = Member.builder()
                .email("test@example.com")
                .nickname("테스터")
                .password("password123!")
                .build();
        testMember = memberRepository.save(testMember); // 저장된 객체를 다시 할당

        // 2. 게시글 생성 및 저장 (여기가 97번 줄 근처일 것입니다)
        testPost = Post.builder()
                .title("통합 테스트 게시글")
                .content("게시글 본문 내용")
                .summary("요약")
                .status(PostStatus.PUBLISHED)
                .build(); // <--- build()가 누락되지 않았는지 확인!

        // 3. save() 호출 전 testPost가 null인지 확인하는 로직 (검증용)
        if (testPost == null) {
            throw new RuntimeException("testPost 객체가 생성되지 않았습니다!");
        }

        testPost = postRepository.save(testPost);
    }

    @Test
    @DisplayName("댓글 작성부터 조회까지의 전체 흐름을 검증한다.")
    void comment_full_flow_test() throws Exception {
        // 1. 댓글 생성 요청 (Create)
        CommentCreateReq createReq = new CommentCreateReq("통합 테스트 댓글입니다.", testMember.getId(), null);

        // 1. 댓글 생성 요청 (Create)
        String createResponse = mockMvc.perform(post("/api/posts/{postId}/comments", testPost.getId())
                        // testMember.getEmail()을 사용하여 실제 DB에 저장된 유저로 인증
                        .with(user(testMember.getEmail()).roles("USER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("Location");

        // 생성된 ID 추출 (예: /api/comments/1)
        Long createdCommentId = Long.parseLong(createResponse.substring(createResponse.lastIndexOf("/") + 1));

        // 2. 대댓글 생성 요청 (Reply)
        CommentCreateReq replyReq = new CommentCreateReq("통합 테스트 대댓글입니다.", testMember.getId(), createdCommentId);

        mockMvc.perform(post("/api/posts/{postId}/comments", testPost.getId())
                        .with(user(testMember.getEmail()).roles("USER")) // 1. 인증 정보 추가
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(replyReq)))
                .andExpect(status().isCreated());

        // 3. 게시글별 댓글 목록 조회 (Read)
        mockMvc.perform(get("/api/posts/{postId}/comments", testPost.getId())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].id").value(createdCommentId))
                .andExpect(jsonPath("$.data.content[0].content").value("통합 테스트 댓글입니다."))
                .andExpect(jsonPath("$.data.content[0].replyCount").value(1)); // 대댓글 개수 확인

        // 4. 댓글 수정 (Update)
        CommentUpdateReq updateReq = new CommentUpdateReq("수정된 통합 댓글");
        mockMvc.perform(put("/api/comments/{commentId}", createdCommentId)
                        .with(user(testMember.getEmail()).roles("USER")) // 1. 인증 정보 추가
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk());

        // 5. 댓글 삭제 (Delete)
        mockMvc.perform(delete("/api/comments/{commentId}", createdCommentId)
                        .with(user("test@example.com").roles("USER")) // 인증 추가
                        .with(csrf()))                                 // CSRF 추가
                .andExpect(status().isOk());

        // 최종 확인: 삭제된 댓글은 "삭제된 댓글입니다"로 표시되어야 함 (Soft Delete 시나리오)
        Comment deletedComment = commentRepository.findById(createdCommentId).get();
        assertThat(deletedComment.getContent()).contains("삭제된 댓글입니다");
    }
}
