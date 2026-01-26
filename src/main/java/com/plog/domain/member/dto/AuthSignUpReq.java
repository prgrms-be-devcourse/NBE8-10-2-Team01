package com.plog.domain.member.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * 회원가입 요청을 위한 데이터 구조입니다.
 *
 * @param email 가입하고자 하는 사용자의 이메일 주소 (ID 역할)
 * @param password 사용자의 비밀번호
 * @param nickname 서비스 내에서 사용할 사용자의 별명
 */
public record AuthSignUpReq(
        @NotBlank @Email String email,
        @NotBlank String password,
        @NotBlank String nickname
) {
}