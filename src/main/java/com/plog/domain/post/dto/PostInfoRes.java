package com.plog.domain.post.dto;

import com.plog.domain.comment.dto.CommentInfoRes;
import com.plog.domain.hashtag.entity.PostHashTag;
import com.plog.domain.post.entity.Post;
import org.springframework.data.domain.Slice;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 게시물 상세 정보를 클라이언트에게 전달하기 위한 응답 데이터 레코드입니다.
 * <p>
 * 데이터베이스 엔티티({@link Post})를 직접 노출하지 않고,
 * API 스펙에 필요한 필드만을 선택적으로 포함하여 보안성과 유지보수성을 높입니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@link java.lang.Record} 클래스를 암시적으로 상속받으며, 모든 필드는 final로 선언됩니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@code PostInfoRes(Long id, String title, String content, int viewCount, LocalDateTime createDate, LocalDateTime modifyDate, Slice<CommentInfoRes> comments, String nickname, String profileImage)} <br>
 * 레코드 정의에 따른 표준 생성자를 사용합니다.
 *
 * <p><b>빈 관리:</b><br>
 * 별도의 빈으로 관리되지 않으며, 서비스나 컨트롤러 계층에서 필요 시 생성하여 반환합니다.
 *
 * <p><b>외부 모듈:</b><br>
 * 외부 의존성 없이 표준 Java API만을 사용합니다.
 *
 * @author MintyU
 * @see com.plog.domain.post.entity.Post
 * @since 2026-01-16
 */
public record PostInfoRes(
        Long id,
        String title,
        String content,
        int viewCount,
        LocalDateTime createDate,
        LocalDateTime modifyDate,
        Slice<CommentInfoRes> comments,
        List<String> hashtags,
        String nickname,
        String profileImage
) {
    /**
     * Post 엔티티 객체를 PostInfoRes DTO로 변환하는 정적 팩토리 메서드입니다.
     *
     * @param post 변환 대상 엔티티
     * @return 필드값이 매핑된 PostInfoRes 객체
     */
    public static PostInfoRes from(Post post) {
        return new PostInfoRes(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getViewCount(),
                post.getCreateDate(),
                post.getModifyDate(),
                null,
                post.getPostHashTags().stream()
                        .map(PostHashTag::getDisplayName)
                        .toList(),

                post.getMember().getNickname(),
                post.getMember().getProfileImage() != null ? post.getMember().getProfileImage().getAccessUrl() : null
        );
    }
    /**
     * Post 엔티티와 댓글 목록을 받아 PostInfoRes DTO로 변환하는 정적 팩토리 메서드입니다.
     *
     * @param post     변환 대상 엔티티
     * @param comments 게시물에 속한 댓글 슬라이스 데이터
     * @return 필드값과 댓글 목록이 매핑된 PostInfoRes 객체
     */
    public static PostInfoRes from(Post post, Slice<CommentInfoRes> comments) {
        return new PostInfoRes(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getViewCount(),
                post.getCreateDate(),
                post.getModifyDate(),
                comments,
                post.getPostHashTags().stream()
                        .map(PostHashTag::getDisplayName)
                        .toList(),
                post.getMember().getNickname(),
                post.getMember().getProfileImage() != null ? post.getMember().getProfileImage().getAccessUrl() : null
        );
    }
}