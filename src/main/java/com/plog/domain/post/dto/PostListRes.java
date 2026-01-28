package com.plog.domain.post.dto;

import com.plog.domain.post.entity.Post;

import java.time.LocalDateTime;

/**
 * 게시물 정보 목록을 클라이언트에게 전달하기 위한 응답 데이터 레코드입니다.
 * <p>
 * 데이터베이스 엔티티({@link Post})를 직접 노출하지 않고,
 * API 스펙에 필요한 필드만을 선택적으로 포함하여 보안성과 유지보수성을 높입니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@link java.lang.Record} 클래스를 암시적으로 상속받으며, 모든 필드는 final로 선언됩니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@code PostListRes(Long id, String title, String summary, int viewCount, LocalDateTime createDate, LocalDateTime modifyDate, String thumbnail, String nickname, String profileImage)} <br>
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
 * @since 2026-01-28
 */
public record PostListRes(
        Long id,
        String title,
        String summary,
        int viewCount,
        LocalDateTime createDate,
        LocalDateTime modifyDate,
        String thumbnail,
        String nickname,
        String profileImage
) {
    /**
     * Post 엔티티 객체를 PostResponse DTO로 변환하는 정적 팩토리 메서드입니다.
     *
     * @param post 변환 대상 엔티티
     * @return 필드값이 매핑된 PostListRes 객체
     */
    public static PostListRes from(Post post) {
        return new PostListRes(
                post.getId(),
                post.getTitle(),
                post.getSummary(),
                post.getViewCount(),
                post.getCreateDate(),
                post.getModifyDate(),
                post.getThumbnail(),
                post.getMember().getNickname(),
                post.getMember().getProfileImage() != null ? post.getMember().getProfileImage().getAccessUrl() : null
        );
    }
}
