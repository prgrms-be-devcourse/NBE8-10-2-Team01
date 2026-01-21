package com.plog.domain.member.service;


import com.plog.domain.member.entity.Member;
import com.plog.domain.member.repository.MemberRepository;
import com.plog.global.exception.errorCode.AuthErrorCode;
import com.plog.global.exception.exceptions.AuthException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

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
 * @author yyj96
 * @see
 * @since 2026-01-15
 */

// TODO: service 바로 쓰지 말고 implement로 만들기

@Service
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {
    private final SecretKey cachedKey;
    private final long accessTokenExpiration;
    private final MemberRepository memberRepository;

    public AuthServiceImpl(
            @Value("${custom.jwt.secretKey}") String secretKey,
            @Value("${custom.jwt.access-expiration}") long accessTokenExpiration,
            MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.cachedKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiration = accessTokenExpiration;
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public String genAccessToken(Member member) {
        return Jwts.builder()
                .claim("id", member.getId())
                .claim("email", member.getEmail())
                .claim("nickname", member.getNickname())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(cachedKey)
                .compact();
    }

    @Override
    @Transactional
    public Long signUp(String email, String password, String nickname) {
        memberRepository.findByEmail(email)
                .ifPresent(_member -> {
                            throw new AuthException(AuthErrorCode.USER_ALREADY_EXIST,
                                    "[AuthServiceImpl#signUp] user already exists.",
                                    "이미 가입 완료된 이메일입니다."
                                    );
                });

        password = passwordEncoder.encode(password);
        Member member = Member.builder()
                .email(email)
                .password(password)
                .nickname(nickname)
                .build();
        return memberRepository.save(member).getId();
    }

    private final PasswordEncoder passwordEncoder;
}