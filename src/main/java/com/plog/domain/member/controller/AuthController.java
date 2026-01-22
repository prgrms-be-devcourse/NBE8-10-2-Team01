package com.plog.domain.member.controller;

import com.plog.domain.member.dto.AuthSignInReq;
import com.plog.domain.member.dto.AuthSignInRes;
import com.plog.domain.member.dto.AuthSignUpReq;
import com.plog.domain.member.entity.Member;
import com.plog.domain.member.service.AuthService;
import com.plog.global.response.CommonResponse;
import com.plog.global.exception.exceptions.AuthException;
import com.plog.global.response.Response;
import com.plog.global.rq.Rq;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

/**
 * 사용자 인증 및 권한 관리를 담당하는 컨트롤러입니다.
 * <p>
 * 회원가입, 로그인, 로그아웃 및 토큰 재발급을 위한 엔드포인트를 제공하며,
 * {@link Rq} 객체를 통해 HTTP 요청/응답 메시지의 헤더와 쿠키를 제어합니다.
 *
 * <p><b>상속 정보:</b><br>
 * 상속 정보 없음.
 *
 * <p><b>주요 생성자:</b><br>
 * {@code AuthController(AuthService authService, Rq rq)} <br>
 * 생성자 주입을 통해 인증 서비스와 요청/응답 편의 객체를 주입받습니다. <br>
 *
 * <p><b>빈 관리:</b><br>
 * {@code @RestController}를 사용하여 스프링 컨테이너의 빈으로 관리되며,
 * 모든 응답은 {@link CommonResponse} 형태로 반환됩니다.
 *
 * <p><b>외부 모듈:</b><br>
 * Spring Web, Jakarta Validation, Jakarta Servlet API 등을 사용합니다.
 *
 * @author minhee
 * @since 2026-01-20
 * @see AuthService
 * @see Rq
 */

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final Rq rq;

    /**
     * 새로운 회원을 등록(회원가입)합니다.
     * 인증/인가 도메인임을 고려하여 행위를 명시하는 경로를 예외적으로 사용합니다.
     *
     * @param req 회원가입에 필요한 이메일, 비밀번호, 닉네임 데이터
     * @return 생성된 회원의 조회 경로를 Location 헤더에 포함한 공통 응답 객체 (201 Created)
     */

    @PostMapping("/sign-up")
    public ResponseEntity<Void> signUp(
            @Valid @RequestBody AuthSignUpReq req
    ) {
        Long memberId = authService.signUp(
                req.email(),
                req.password(),
                req.nickname()
        );

        return ResponseEntity.created(URI.create(("/api/members/"+memberId))).build();
    }

    /**
     * 인증/인가 도메인임을 고려하여 행위를 명시하는 경로를 예외적으로 사용합니다.
     * <p>
     * 입력받은 이메일과 비밀번호를 검증하여 일치할 경우,
     * 서비스 이용을 위한 Access Token과 보안 유지를 위한 Refresh Token을 생성합니다.
     * Access Token은 응답 헤더와 바디에, Refresh Token은 보안 쿠키(apiKey)에 설정됩니다.
     *
     * @param req 로그인 요청 데이터 (email, password)
     * @return 로그인 성공 메시지와 사용자 닉네임, Access Token을 포함한 공통 응답 객체 (200 OK)
     */
    @PostMapping("/sign-in")
    public ResponseEntity<Response<AuthSignInRes>> signIn(
            @Valid @RequestBody AuthSignInReq req
    ) {
        Member member = authService.signIn(
                req.email(),
                req.password()
        );

        String accessToken = authService.genAccessToken(member);
        String refreshToken = authService.genRefreshToken(member);
        rq.setHeader("Authorization", accessToken);
        rq.setCookie("apiKey", refreshToken);

        String nickname = member.getNickname();
        AuthSignInRes res = new AuthSignInRes(nickname, accessToken);

        return ResponseEntity.ok(
                CommonResponse.success(res, "%s님 환영합니다.".formatted(nickname))
        );
    }

    /**
     * 로그아웃을 수행합니다.
     * <p>
     * 브라우저에 저장된 인증용 쿠키(apiKey)를 삭제하며, 세션 상태를 무효화합니다.
     * 클라이언트 측에서도 보관 중인 Access Token을 삭제해야 완벽한 로그아웃이 이루어집니다.
     *
     * @return 로그아웃 완료 메시지를 포함한 공통 응답 객체 (200 OK)
     */
    @GetMapping("/logout")
    public ResponseEntity<Response<Void>> logout() {
        rq.deleteCookie("apiKey");
        return ResponseEntity.ok(
                CommonResponse.success(null, "로그아웃 되었습니다.")
        );
    }

    /**
     * 만료된 Access Token을 재발급합니다.
     * <p>
     * 쿠키에 담긴 Refresh Token(apiKey)의 유효성을 검증하고,
     * 새로운 Access Token을 생성하여 헤더와 응답 바디를 통해 반환합니다.
     *
     * @return 갱신된 Access Token을 포함한 공통 응답 객체 (200 OK)
     * @throws AuthException Refresh Token이 유효하지 않거나 만료된 경우 발생
     */
    @GetMapping("/reissue")
    public ResponseEntity<Response<AuthSignInRes>> accessTokenReissue() {
        String refreshToken = rq.getCookieValue("apiKey", null);
        AuthSignInRes reissuedRes = authService.accessTokenReissue(refreshToken);

        rq.setHeader("Authorization", reissuedRes.accessToken());
        AuthSignInRes res = new AuthSignInRes(reissuedRes.nickname(), reissuedRes.accessToken());

        return ResponseEntity.ok(
                CommonResponse.success(res, "토큰이 재발급되었습니다.")
        );
    }
}