package com.plog.global.security;


import org.springframework.stereotype.Component;

/**
 * 인증에 필요한 리프레시 토큰(Refresh Token)의 저장 및 관리를 위한 추상 인터페이스입니다.
 * <p>
 * 토큰의 저장소(In-Memory, DB, NoSQL 등)에 상관없이 일관된 접근 방식을 제공하며,
 * 보안 세션 유지 및 로그아웃 처리를 위한 핵심 기능을 정의합니다.
 *
 * @author minhee
 * @since 2026-01-27
 */

public interface TokenStore {

    /**
     * 사용자 식별 정보를 키로 하여 리프레시 토큰을 저장합니다.
     *
     * @param email 사용자 식별자
     * @param refreshToken 저장할 리프레시 토큰 값
     */
    void save(String email, String refreshToken);

    /**
     * 사용자 식별 정보를 통해 저장된 리프레시 토큰을 조회합니다.
     *
     * @param email 사용자 식별자
     * @return 조회된 리프레시 토큰 (존재하지 않을 경우 null)
     */
    String get(String email);

    /**
     * 사용자 식별 정보와 연결된 리프레시 토큰을 삭제(무효화)합니다.
     * 로그아웃 요청 시 주로 사용됩니다.
     *
     * @param email 사용자 식별자
     */
    void delete(String email);
}