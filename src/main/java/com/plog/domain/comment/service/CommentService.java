package com.plog.domain.comment.service;

import com.plog.domain.comment.dto.CommentCreateReq;
import com.plog.domain.comment.dto.CommentInfoRes;
import com.plog.domain.comment.dto.ReplyInfoRes;
import org.springframework.data.domain.Slice;


/**
 * 게시글 댓글(Comment) 도메인의 비즈니스 로직을 정의하는 서비스 인터페이스.
 *
 * <p>
 * 게시글(Post)에 종속된 댓글 및 대댓글의
 * <b>생성, 조회(페이징), 수정, 삭제</b> 기능을 추상화한다.
 * </p>
 *
 * <p>
 * 본 인터페이스는 구현체와의 명확한 책임 분리를 통해
 * 서비스 계층의 역할을 드러내고, 테스트 및 확장성을 고려하여 설계되었다.
 * </p>
 *
 * <p><b>설계 특징:</b></p>
 * <ul>
 *   <li>댓글과 대댓글을 단일 도메인으로 관리</li>
 *   <li>조회 시 페이징을 적용하여 대량 데이터 처리 성능 고려</li>
 *   <li>Controller 계층에서는 본 인터페이스에만 의존</li>
 * </ul>
 *
 * <p><b>빈 관리:</b></p>
 * <ul>
 *   <li>구현체는 {@code @Service}로 등록되어 Singleton Bean으로 관리된다.</li>
 * </ul>
 *
 * @author njwwn
 * @since 2026-01-19
 */


public interface CommentService {

    /**
     * 단일 게시글(Post)에 새로운 댓글 또는 대댓글을 작성한다.
     *
     * <p>
     * {@code parentCommentId}가 {@code null}인 경우 일반 댓글로 생성되며,
     * 값이 존재할 경우 해당 댓글을 부모로 하는 대댓글로 생성된다.
     * </p>
     *
     * @param postId 게시글 식별자
     * @param memberId 작성자 식별자
     * @param req 댓글 내용, 부모 댓글 식별자
     * @return 생성된 댓글의 식별자
     *
     * @throws IllegalArgumentException 게시글 또는 부모 댓글이 존재하지 않을 경우
     */
    Long createComment(Long postId, Long memberId, CommentCreateReq req);


    /**
     * 특정 게시글(Post)에 작성된 댓글 목록을 조회한다.
     * 페이징 기능: 최대 10개씩 조회
     *
     * <p>
     * 댓글만 조회된다.
     * </p>
     *
     * @param postId 게시글 식별자
     * @param pageNumber 조회할 댓글 pageNumber
     * @return 댓글 목록 응답 DTO 슬라이스
     */
    Slice<CommentInfoRes> getCommentsByPostId(Long postId, int pageNumber);


    /**
     * 특정 루트 댓글(Comment)에 작성된 대댓글 목록을 조회한다.
     * 페이징 기능: 최대 5개씩 조회
     *
     * <p>
     * 이벤트 발생 시에만 대댓글이 조회되도록 한다.
     * </p>
     *
     * @param commentId 부모 댓글 식별자
     * @param pageNumber 조회할 대댓글 pageNumber
     * @return 대댓글 목록 응답 DTO 슬라이스
     */
    Slice<ReplyInfoRes> getRepliesByCommentId(Long commentId, int pageNumber);


    /**
     * 이미 작성된 댓글(Comment)의 내용을 수정한다.
     *
     * <p>
     * 댓글 작성자 검증 및 수정 가능 여부 판단은
     * 구현체에서 처리한다.
     * </p>
     *
     * @param commentId 수정할 댓글 식별자
     * @param content 수정할 댓글 내용
     * @return 수정된 댓글 엔티티
     *
     * @throws IllegalArgumentException 댓글이 존재하지 않을 경우
     */
    void updateComment(Long commentId, Long memberId, String content);


    /**
     * 댓글(Comment)을 삭제 처리한다.
     *
     * <p>
     * 현재 댓글의 자식 댓글이 있는 경우에만 소프트 삭제를 기능하도록 하였다.
     * </p>
     *
     * @param commentId 삭제할 댓글 식별자
     * @param memberId 작성자 식별자
     *
     * @throws IllegalArgumentException 댓글이 존재하지 않을 경우
     */
    void deleteComment(Long commentId, Long memberId);
}
