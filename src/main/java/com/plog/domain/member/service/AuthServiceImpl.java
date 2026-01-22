package com.plog.domain.member.service;


import com.plog.domain.member.dto.AuthSignInRes;
import com.plog.domain.member.entity.Member;
import com.plog.domain.member.repository.MemberRepository;
import com.plog.global.exception.errorCode.AuthErrorCode;
import com.plog.global.exception.exceptions.AuthException;
import com.plog.global.security.JwtUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

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

// TODO: MemberService 주입 받아서 중복 로직 리팩토링 필요
// TODO: Member -> MemberInfoRes 혹은 추가 Dto 생성 필요

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final MemberService memberService;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Override
    public String genAccessToken(Member member) {
        return jwtUtils.createAccessToken(Map.of(
                "id", member.getId(),
                "email", member.getEmail(),
                "nickname", member.getNickname()
        ));
    }

    @Override
    public String genRefreshToken(Member member) {
        return jwtUtils.createRefreshToken(member.getId());
    }

    @Override
    @Transactional
    public Long signUp(String email, String password, String nickname) {
        if (memberService.isDuplicateEmail(email)) {
            throw new AuthException(AuthErrorCode.USER_ALREADY_EXIST,
                    "[AuthServiceImpl#signUp] user email already exists.",
                    "이미 가입된 이메일입니다.");
        }

        if (memberService.isDuplicateNickname(nickname)) {
            throw new AuthException(AuthErrorCode.USER_ALREADY_EXIST,
                    "[AuthServiceImpl#signUp] user nickname already exists.",
                    "이미 사용 중인 닉네임입니다.");
        }

        password = passwordEncoder.encode(password);
        Member member = Member.builder()
                .email(email)
                .password(password)
                .nickname(nickname)
                .build();
        return memberRepository.save(member).getId();
    }

    @Override
    public Member signIn(String email, String password) {
        Member member = findByEmail(email);
        checkPassword(member, password);
        return member;
    }

    @Override
    public AuthSignInRes accessTokenReissue(String refreshToken) {
        if (refreshToken == null) {
            throw new AuthException(AuthErrorCode.TOKEN_INVALID);
        }

        try {
            Claims claims = jwtUtils.parseToken(refreshToken);
            Long memberId = claims.get("id", Long.class);
            Member member = findById(memberId);
            String newAccessToken = genAccessToken(member);
            return new AuthSignInRes(member.getNickname(), newAccessToken);

        } catch (ExpiredJwtException e) {
            throw new AuthException(AuthErrorCode.LOGIN_REQUIRED,
                    "[AuthServiceImpl#accessTokenReissue] Refresh Token expired",
                    "세션이 만료되었습니다. 다시 로그인해 주세요.");
        } catch (Exception e) {
            throw new AuthException(
                    AuthErrorCode.TOKEN_INVALID,
                    "[AuthServiceImpl] Unexpected reissue error: " + e.getMessage(),
                    "유효한 토큰이 아닙니다."
            );
        }
    }

    /**
     * 고유 식별자(ID)를 통해 회원을 조회하며, 존재하지 않을 경우 예외를 발생시킵니다.
     *
     * @param id 회원 고유 식별자
     * @return 조회된 회원 엔티티
     * @throws AuthException 회원을 찾을 수 없는 경우 발생
     */
    private Member findById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new AuthException(AuthErrorCode.INVALID_CREDENTIALS));
    }

    /**
     * 이메일을 통해 회원을 조회하며, 존재하지 않을 경우 예외를 발생시킵니다.
     *
     * @param email 회원 이메일
     * @return 조회된 회원 엔티티
     * @throws AuthException 해당 이메일의 회원이 없는 경우 발생
     */
    private Member findByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new AuthException(AuthErrorCode.INVALID_CREDENTIALS));
    }

    /**
     * 입력받은 평문 비밀번호와 DB에 저장된 암호화 비밀번호의 일치 여부를 검증합니다.
     *
     * @param member   검증 대상 회원 엔티티
     * @param password 입력받은 평문 비밀번호
     * @throws AuthException 비밀번호가 일치하지 않을 경우 발생
     */
    private void checkPassword(Member member, String password) {
        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new AuthException(AuthErrorCode.INVALID_CREDENTIALS);
        }
    }
}