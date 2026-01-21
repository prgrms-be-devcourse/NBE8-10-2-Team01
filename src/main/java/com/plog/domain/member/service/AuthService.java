package com.plog.domain.member.service;


import com.plog.domain.member.dto.AuthSignInRes;
import com.plog.domain.member.entity.Member;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

// TODO: 주석 더 자세히 적기

@Service
@Transactional
public interface AuthService {
    /** accessToken 생성 */
    String genAccessToken(Member member);

    /** refreshToken 생성 */
    String genRefreshToken(Member member);

    /** 회원가입 로직 */
    Long signUp(String email, String password, String nickname);

    /** 로그인 로직 */
    Member signIn(String email, String password);

    AuthSignInRes accessTokenReissue(String refreshToken);
}