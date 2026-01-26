package com.plog.domain.member.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * 로그인 요청을 위한 데이터 구조입니다.
 *
 * @param email    가입된 이메일 주소
 * @param password 계정 비밀번호
 */
public record AuthSignInReq(
        @NotBlank @Email String email,
        @NotBlank String password
) {
}