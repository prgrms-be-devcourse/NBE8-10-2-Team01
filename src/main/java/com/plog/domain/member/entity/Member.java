package com.plog.domain.member.entity;

import com.plog.domain.image.entity.Image;
import com.plog.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor
public class Member extends BaseEntity {
    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false)
    private String nickname;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_image_id")
    private Image profileImage;

    /**
     * 엔티티의 update 메서드입니다.
     * <pre>
     *     해당 메서드를 이런 방식으로 구현한 까닭은 다음과 같습니다.
     *     1. update 가 필요한 필드를 setter 로 두는 것보다, 도메인 상 기능이 명확한 메서드로 묶는 편이 좋다고 봤습니다.
     *     2. entity 는 dto 의 존재를 좋지 않다고 생각합니다. DTO는 entity 의 데이터를 가공하여 운송하는 책임을
     *       가지고 있지만, entity 에서 dto의 존재를 아는 순간 둘 사이 간의 결합이 발생합니다.
     * </pre>
     * @param nickname 변경할 파라미터 중 닉네임
     * @return 변경된 엔티티
     */
    public Member update(String nickname) {
        this.nickname = nickname;

        return this;
    }

    public void updateProfileImage(Image profileImage) {
        this.profileImage = profileImage;
    }
}