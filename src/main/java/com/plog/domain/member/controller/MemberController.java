package com.plog.domain.member.controller;

import com.plog.domain.member.dto.MemberInfoRes;
import com.plog.domain.member.dto.MemberUpdaterReq;
import com.plog.domain.member.service.MemberService;
import com.plog.global.response.CommonResponse;
import com.plog.global.response.Response;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Member 도메인에 대한 controller.
 *
 * @author jack8
 * @since 2026-01-19
 */
@RestController
@RequestMapping("/api/members")
@AllArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/id/{id}")
    public ResponseEntity<Response<MemberInfoRes>> findMemberWithId(@PathVariable("id") Long id) {
        MemberInfoRes response = memberService.findMemberWithId(id);

        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @GetMapping("/nickname/{nickname}")
    public ResponseEntity<Response<MemberInfoRes>> findMemberWithNickname(@PathVariable("nickname") String nickname) {
        MemberInfoRes response = memberService.findMemberWithNickname(nickname);

        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @PutMapping("/update")
    public ResponseEntity<Response<MemberInfoRes>> updateMember(@RequestBody MemberUpdaterReq request,
                                                                @AuthenticationPrincipal Long memberId) {
        MemberInfoRes response = memberService.updateMemberInfo(memberId, request);

        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @GetMapping("/check/email")
    public ResponseEntity<Response<Boolean>> checkDuplicateEmail(@RequestParam(name = "email") String email) {
        Boolean response = memberService.isDuplicateEmail(email);

        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @GetMapping("/check/nickname")
    public ResponseEntity<Response<Boolean>> checkDuplicateNickname(@RequestParam(name = "nickname") String nickname) {
        Boolean response = memberService.isDuplicateNickname(nickname);

        return ResponseEntity.ok(CommonResponse.success(response));
    }
}
