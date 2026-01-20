package com.plog.global.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ClaimsBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

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

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(Map<String, Object> body) {
        ClaimsBuilder claimsBuilder = Jwts.claims();

        for (Map.Entry<String, Object> entry : body.entrySet()) {
            claimsBuilder.add(entry.getKey(), entry.getValue());
        }

        Claims claims = claimsBuilder.build();
        Date issuedAt = new Date();
        Date expiration = new Date(issuedAt.getTime() + accessTokenExpiration);

        return Jwts.builder()
                .claims(claims)
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(getSigningKey())
                .compact();
    }

    public String createRefreshToken(Long id) {
        Date issuedAt = new Date();
        Date expiration = new Date(issuedAt.getTime() + refreshTokenExpiration);

        return Jwts.builder()
                .claim("id", id)
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(getSigningKey())
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}