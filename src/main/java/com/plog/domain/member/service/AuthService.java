package com.plog.domain.member.service;


import com.plog.domain.member.dto.AuthLoginResult;
import com.plog.domain.member.dto.AuthSignInReq;
import com.plog.domain.member.dto.AuthSignUpReq;
import com.plog.domain.member.dto.MemberInfoRes;
import com.plog.domain.member.entity.Member;
import com.plog.global.exception.exceptions.AuthException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 인증 및 인가와 관련된 비즈니스 로직을 정의하는 인터페이스입니다.
 * <p>
 * 회원가입, 로그인, 토큰 생성 및 재발급 등의 보안 관련 기능을 정의하며,
 * JWT(JSON Web Token) 기반의 인증 시스템을 구축하기 위한 명세를 제공합니다.
 *
 * <p><b>주요 기능 요약:</b><br>
 * 1. 회원가입 및 로그인: 사용자 계정 생성 및 자격 증명 확인 <br>
 * 2. 토큰 관리: Access/Refresh Token 생성 및 만료 시 재발급 처리 <br>
 * 3. 보안 검증: 비밀번호 암호화 비교 및 토큰 유효성 검증
 *
 * @author yyj96
 * @since 2026-01-15
 */

@Service
@Transactional
public interface AuthService {

    /**
     * 사용자의 정보를 바탕으로 Access Token을 생성합니다.
     * <p><b>실행 로직:</b><br>
     * 1. {@link Member} 엔티티에서 식별자(ID), 이메일, 닉네임을 추출합니다. <br>
     * 2. 추출된 정보를 JWT의 Claim으로 삽입합니다. <br>
     * 3. 설정된 비밀키와 알고리즘을 사용하여 단기 유효 기간을 가진 토큰을 발행합니다.
     *
     * @param member 토큰에 담을 정보를 보유한 회원 엔티티
     * @return 생성된 JWT Access Token 문자열
     */

    String genAccessToken(MemberInfoRes member);

    /**
     * 사용자의 보안 유지를 위한 Refresh Token을 생성합니다.
     * <p><b>실행 로직:</b><br>
     * 1. 회원의 식별자(ID)를 기반으로 토큰을 생성합니다. <br>
     * 2. Access Token보다 긴 유효 기간을 설정합니다.
     * 3. 해당 토큰은 클라이언트의 보안 쿠키(HttpOnly)에 저장됩니다.
     *
     * @param member 토큰 생성 대상 회원 엔티티
     * @return 생성된 JWT Refresh Token 문자열
     */

    String genRefreshToken(MemberInfoRes member);

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
     * 사용자의 자격 증명을 확인하여 로그인을 처리합니다.
     * <p><b>실행 로직:</b><br>
     * 1. 이메일을 통해 데이터베이스에서 회원 정보를 검색합니다. <br>
     * 2. 입력된 평문 비밀번호와 저장된 암호화 비밀번호를 비교 검증합니다. <br>
     * 3. 검증에 성공하면 회원 엔티티를 반환하며, 실패 시 인증 예외를 발생시킵니다.
     *
     * @param req 로그인 요청 데이터 (email, password)
     * @return nickname, Access Token, Refresh Token이 포함된 로그인 결과 DTO
     * @throws AuthException 이메일 미존재 또는 비밀번호 불일치 시 발생
     */
    AuthLoginResult signIn(AuthSignInReq req);

    /**
     * 만료된 Access Token을 Refresh Token을 사용하여 재발급합니다.
     * <p><b>실행 로직:</b><br>
     * 1. 전달받은 Refresh Token의 유효성 및 만료 여부를 검증합니다. <br>
     * 2. 토큰 내부에 저장된 회원 식별자(ID)를 추출하여 회원을 조회합니다. <br>
     * 3. 새로운 Access Token을 생성하고, 사용자 닉네임과 함께 DTO 객체에 담아 반환합니다.
     *
     * @param refreshToken 클라이언트로부터 전달받은 Refresh Token
     * @return 갱신된 Access/Refresh Token과 사용자 정보를 포함한 결과 DTO
     * @throws AuthException 토큰이 유효하지 않거나 만료되었을 때 발생
     */
    AuthLoginResult tokenReissue(String refreshToken);
}