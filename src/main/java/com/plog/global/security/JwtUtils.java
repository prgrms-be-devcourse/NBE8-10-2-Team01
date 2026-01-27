package com.plog.global.security;

import com.plog.domain.member.dto.MemberInfoRes;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ClaimsBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT의 생성 및 검증을 담당하는 클래스입니다.
 * <p>
 * 서비스 내 인증, 인가를 위해 Access Token과 Refresh Token을 발급합니다.
 * 전달받은 토큰의 유효성을 검사하고 페이로드(Claims)를 추출하는 기능을 제공합니다.
 *
 * <p><b>작동 원리:</b><br>
 * 설정 파일에 정의된 custom.jwt.secretKey를 기반으로 HMAC-SHA 알고리즘을 사용하여 서명된 토큰을 생성합니다.
 *
 * <p><b>빈 관리:</b><br>
 * Spring의 {@link @Component} 어노테이션을 통해 싱글톤 빈으로 관리합니다.
 * 필요한 서비스 레이어에서 의존성 주입(DI)을 통해 사용됩니다.
 *
 * @author minhee
 * @since 2026-01-16
 */

@Component
public class JwtUtils {
    private final String secretKey;
    private final long accessTokenExpiration; // 30분
    private final long refreshTokenExpiration; // 약 100년 -> 추후 유효기간 로직 추가 시 수정

    public JwtUtils(
            @Value("${custom.jwt.secretKey}") String secretKey,
            @Value("${custom.jwt.access-expiration}") long accessTokenExpiration,
            @Value("${custom.jwt.refresh-expiration}") long refreshTokenExpiration) {
        this.secretKey = secretKey;
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    /**
     * 주입된 비밀키 문자열을 바탕으로 HMAC-SHA 알고리즘용 SecretKey 객체를 생성합니다.
     *
     * @return 알고리즘 규격에 맞는 {@link SecretKey}
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 사용자 정보를 포함한 인증용 Access Token을 생성합니다.
     * <p>
     * <b>포함 정보:</b> Subject(Email), ID Claim(Long PK), Nickname Claim.
     *
     * @param dto 토큰에 담을 사용자 정보 객체
     * @return 생성된 JWT Access Token 문자열
     */
    public String createAccessToken(MemberInfoRes dto) {
        return Jwts.builder()
                .subject(dto.email()) // Project username
                .claim("id", dto.id())
                .claim("nickname", dto.nickname())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 장기간 인증 유지를 위한 Refresh Token을 생성합니다.
     * <p>
     * 보안 정석에 따라 사용자의 식별자(Email == username)만 포함합니다.
     *
     * @param email 사용자를 식별할 이메일 주소
     * @return 생성된 JWT Refresh Token 문자열
     */
    public String createRefreshToken(String email) {
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * JWT 문자열의 서명을 검증하고 페이로드(Claims)를 추출합니다.
     * <p>
     * 토큰이 만료되었거나 구조가 잘못된 경우 관련 {@link io.jsonwebtoken.JwtException}을 던집니다.
     *
     * @param token 검증할 JWT 문자열
     * @return 추출된 {@link Claims} 객체
     * @throws io.jsonwebtoken.ExpiredJwtException 토큰 만료 시 발생
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}