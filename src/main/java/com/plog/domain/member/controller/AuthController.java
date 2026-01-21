package com.plog.domain.member.controller;

import com.plog.domain.member.entity.Member;
import com.plog.domain.member.service.AuthService;
import com.plog.global.response.CommonResponse;
import com.plog.global.rq.Rq;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

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
 * @author minhee
 * @see
 * @since 2026-01-20
 */

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final Rq rq;

    // TODO: recode 따로 빼서 Dto에 넣기 -> 사이즈 지정 필요
    // TODO: created일 때 URI 연결해서 내보내기 body 없이
    // TODO: api 당 주석달기

    public record MemberSignUpReq(
            @NotBlank @Email String email,
            @NotBlank String password,
            @NotBlank String nickname
    ) {}

    public record MemberSignInReq(
            @NotBlank @Email String email,
            @NotBlank String password
    ) {}

    /**
     * 로그인에 사용되는 DTO입니다.<br>
     * accessToken을 헤더로 설정했는데도 반환하는 이유는 프론트 작업을 원활하게 하기 위함입니다.
     *
     * @param nickname
     * @param accessToken
     */
    public record MemberSignInRes(
            @NotBlank String nickname,
            @NotBlank String accessToken
    ) {};

    /**
     * 새로운 회원을 등록(회원가입)합니다.
     * 인증/인가 도메인임을 고려하여 행위를 명시하는 경로를 예외적으로 사용합니다.
     *
     * @param req 회원가입에 필요한 이메일, 비밀번호, 닉네임 데이터
     * @return 생성된 회원의 조회 경로를 Location 헤더에 포함한 공통 응답 객체 (201 Created)
     */
    // TODO: sign-up 경로를 쓸지 말지 의논하기
    @PostMapping("/sign-up")
    public ResponseEntity<Void> signUp(
            @Valid @RequestBody MemberSignUpReq req
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
    public ResponseEntity<CommonResponse<MemberSignInRes>> signIn(
            @Valid @RequestBody MemberSignInReq req
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
        MemberSignInRes res = new MemberSignInRes(nickname, accessToken);

        return ResponseEntity.ok(
                CommonResponse.success(res, "%s님 환영합니다.".formatted(nickname))
        );
    }

    @GetMapping("/logout")
    public ResponseEntity<CommonResponse<Void>> logout() {
        rq.deleteCookie("apiKey");
        return ResponseEntity.ok(
                CommonResponse.success(null, "로그아웃 되었습니다.")
        );
    }

    @GetMapping("/reissue")
    public ResponseEntity<CommonResponse<MemberSignInRes>> accessTokenReissue() {
        String refreshToken = rq.getCookieValue("apiKey", null);
        String newAccessToken = authService.accessTokenReissue(refreshToken);
        rq.setHeader("Authorization", newAccessToken);

        // TODO: 멤버, 토큰 둘 다 반환 받은 후, nickname 부분 변경하기
        MemberSignInRes res = new MemberSignInRes("reissued", newAccessToken);
        return ResponseEntity.ok(
                CommonResponse.success(res, "토큰이 재발급되었습니다.")
        );
    }
}