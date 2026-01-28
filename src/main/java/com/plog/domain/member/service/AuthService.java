package com.plog.domain.member.service;


import com.plog.domain.member.dto.AuthSignUpReq;
import com.plog.domain.member.dto.MemberInfoRes;
import com.plog.global.exception.exceptions.AuthException;
import com.plog.global.security.TokenStore;

/**
 * 인증 및 인가와 관련된 비즈니스 로직을 정의하는 인터페이스입니다.
 * <p>
 * 회원가입, 로그인, 토큰 생성 및 재발급 등의 보안 관련 기능을 정의하며,
 * JWT(JSON Web Token) 기반의 인증 시스템을 구축하기 위한 명세를 제공합니다.
 *
 * <p><b>주요 기능 요약:</b><br>
 * 회원가입: 사용자 계정 생성 및 자격 증명 확인 <br>
 * 로그인, 토큰 재발급은 필터에서 처리합니다.
 *
 * @author minhee
 * @since 2026-01-15
 */

public interface AuthService {
    /**
     * 새로운 회원을 시스템에 등록합니다.
     * <p><b>실행 로직:</b><br>
     * 1. 입력받은 이메일의 중복 여부를 확인하며, 중복 시 {@code AuthException}을 발생시킵니다. <br>
     * 2. 보안을 위해 비밀번호를 단방향 해시 알고리즘({@code PasswordEncoder})으로 암호화합니다. <br>
     * 3. 회원 정보를 데이터베이스에 저장하고 생성된 고유 식별자(ID)를 반환합니다.
     *
     * @param req 회원가입 요청 데이터 (email, password, nickname)
     * @return 생성된 회원의 고유 식별자(ID)
     * @throws AuthException 이미 존재하는 이메일일 경우 발생
     */
    Long signUp(AuthSignUpReq req);

    /**
     * 리프레시 토큰을 무효화하여 로그아웃 처리를 수행합니다.
     * <p><b>구현 로직:</b><br>
     * 1. 전달받은 토큰이 유효한지 확인합니다.<br>
     * 2. 토큰에서 사용자 식별값(Email)을 추출합니다.<br>
     * 3. {@link TokenStore}에서 해당 사용자의 세션 정보(RefreshToken)를 삭제합니다.<br>
     * 유효하지 않은 토큰인 경우, 이미 로그아웃된 상태로 간주하여 예외를 던지지 않고 로그만 남깁니다.
     *
     * @param refreshToken 무효화할 리프레시 토큰
     */
    void logout(String refreshToken);

    /**
     * 회원 ID를 기준으로 사용자 정보를 조회합니다.
     *
     * @param id 조회할 회원의 고유 식별자
     * @return 조회된 회원 정보
     * @throws IllegalArgumentException id가 null 인 경우
     * @throws com.plog.global.exception.exceptions.AuthException
     *         해당 ID에 대한 회원이 존재하지 않는 경우
     */
    MemberInfoRes findMemberWithId(Long id);
}