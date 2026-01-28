package com.plog.domain.member.service;


import com.plog.domain.member.dto.AuthSignUpReq;
import com.plog.domain.member.dto.MemberInfoRes;
import com.plog.domain.member.entity.Member;
import com.plog.domain.member.repository.MemberRepository;
import com.plog.global.exception.errorCode.AuthErrorCode;
import com.plog.global.exception.exceptions.AuthException;
import com.plog.global.security.JwtUtils;
import com.plog.global.security.TokenStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@link AuthService} 인터페이스의 구현체로, 인증 및 인가와 관련된 실제 비즈니스 로직을 수행합니다.
 * <p>
 * {@code BCryptPasswordEncoder}를 이용한 비밀번호 암호화와 {@link JwtUtils}를 활용한
 * 토큰 기반 인증 시스템을 구축합니다. 모든 예외 상황은 {@link AuthException}을 통해 관리됩니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@code AuthService} 인터페이스의 자식 구현 클래스
 *
 * <p><b>외부 모듈:</b><br>
 * 1. Spring Security Crypto (PasswordEncoder) <br>
 * 2. JJWT (io.jsonwebtoken)
 *
 * @author minhee
 * @see AuthService
 * @since 2026-01-15
 */

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final TokenStore tokenStore;

    @Override
    @Transactional
    public Long signUp(AuthSignUpReq req) {
        if (isDuplicateEmail(req.email())) {
            throw new AuthException(AuthErrorCode.USER_ALREADY_EXIST,
                    "[AuthServiceImpl#signUp] email dup",
                    "이미 가입된 이메일입니다.");
        }

        if (isDuplicateNickname(req.nickname())) {
            throw new AuthException(AuthErrorCode.USER_ALREADY_EXIST,
                    "[AuthServiceImpl#signUp] user dup",
                    "이미 사용 중인 닉네임입니다.");
        }

        String encodedPassword = passwordEncoder.encode(req.password());
        Member member = Member.builder()
                .email(req.email())
                .password(encodedPassword)
                .nickname(req.nickname())
                .build();
        return memberRepository.save(member).getId();
    }

    @Override
    public void logout(String refreshToken) {
        if (refreshToken != null) {
            try {
                String email = jwtUtils.parseToken(refreshToken).getSubject();
                tokenStore.delete(email);
            } catch (Exception e) {
                log.info("이미 만료되었거나 유효하지 않은 토큰으로 로그아웃 시도");
            }
        }
    }

    /**
     * 이메일 중복 여부를 확인합니다.
     *
     * @return 이미 사용 중인 이메일이면 {@code true}, 아니면 {@code false}
     * @throws IllegalArgumentException email 이 null 이거나 빈 값인 경우
     */
    private boolean isDuplicateEmail(String email) {
        return memberRepository.existsByEmail(email);
    }

    /**
     * 닉네임 중복 여부를 확인합니다.
     *
     * @param nickname 중복 여부를 확인할 닉네임
     * @return 이미 사용 중인 닉네임이면 {@code true}, 아니면 {@code false}
     * @throws IllegalArgumentException nickname 이 null 이거나 빈 값인 경우
     */
    private boolean isDuplicateNickname(String nickname) {
        return memberRepository.existsByNickname(nickname);
    }

    /**
     * 회원 ID를 기준으로 사용자 정보를 조회합니다.
     *
     * @param id 조회할 회원의 고유 식별자
     * @return 조회된 회원 정보
     * @throws IllegalArgumentException id가 null 인 경우
     * @throws AuthException 해당 ID에 대한 회원이 존재하지 않는 경우
     */
    @Override
    public MemberInfoRes findMemberWithId(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new AuthException(AuthErrorCode.USER_NOT_FOUND,
                        "[AuthServiceImpl#findMemberWithId] can't find user by id",
                        "존재하지 않는 사용자입니다."));

        return MemberInfoRes.from(member);
    }
}