package com.plog.domain.member.api;

import com.plog.domain.member.dto.MemberInfoRes;
import com.plog.domain.member.dto.UpdateMemberReq;
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
@RequestMapping("/members")
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
    public ResponseEntity<Response<MemberInfoRes>> updateMember(@RequestBody UpdateMemberReq request,
                                                                @AuthenticationPrincipal Long memberId) {
        MemberInfoRes response = memberService.updateMemberInfo(memberId, request);

        return ResponseEntity.ok(CommonResponse.success(response));
    }
}
