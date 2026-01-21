package com.plog.domain.member.controller;

import com.plog.domain.member.dto.MemberInfoRes;
import com.plog.domain.member.service.MemberService;
import com.plog.global.response.CommonResponse;
import com.plog.global.response.Response;
import com.plog.testUtil.WebMvcTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;

import static com.plog.testUtil.JsonFieldMatcher.hasKey;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * {@link MemberController} 에 대한 슬라이스 테스트 입니다.
 *
 * @author jack8
 * @since 2026-01-19
 */
@WebMvcTest(MemberController.class)
class MemberControllerTest extends WebMvcTestSupport {

    @MockitoBean
    private MemberService memberService;

    @Test
    void findMemberWithId_success() throws Exception {
        //given
        Long userId = 3L;

        MemberInfoRes res = MemberInfoRes.builder()
                .id(userId)
                .email("example@email.com")
                .nickname("nickname")
                .createDate(LocalDateTime.of(2026, 1, 1, 0, 0))
                .build();
        Response<MemberInfoRes> expected = CommonResponse.success(res);
        given(memberService.findMemberWithId(userId)).willReturn(res);

        //when
        ResultActions result = mockMvc.perform(get("/api/members/id/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON));

        //then
        result.andExpect(status().isOk())
                .andExpect(hasKey(expected));
    }

    @Test
    void findMemberWithNickname_success() throws Exception {
        //given
        String nickname = "nickname";

        MemberInfoRes res = MemberInfoRes.builder()
                .id(1L)
                .email("example@email.com")
                .nickname(nickname)
                .createDate(LocalDateTime.of(2026, 1, 1, 0, 0))
                .build();
        Response<MemberInfoRes> expected = CommonResponse.success(res);
        given(memberService.findMemberWithNickname(nickname)).willReturn(res);

        //when
        ResultActions result = mockMvc.perform(get("/api/members/nickname/{nickname}", nickname)
                .contentType(MediaType.APPLICATION_JSON));

        //then
        result.andExpect(status().isOk())
                .andExpect(hasKey(expected));
    }

    //추후 spring security 이후 수정 예정
//    @Test
//    @WithCustomMockUser(userId = 3L)
//    void updateMember_success() throws Exception {
//        // given
//        Long memberId = 3L;
//
//        MemberUpdaterReq request = new MemberUpdaterReq("newNickname");
//
//        MemberInfoRes res = MemberInfoRes.builder()
//                .id(memberId)
//                .email("example@email.com")
//                .nickname("newNickname")
//                .createDate(LocalDateTime.of(2026, 1, 1, 0, 0))
//                .build();
//
//        Response<MemberInfoRes> expected = CommonResponse.success(res);
//
//        given(memberService.updateMemberInfo(memberId, request))
//                .willReturn(res);
//        // when
//        ResultActions result = mockMvc.perform(
//                put("/members/update")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request))
//        );
//
//        // then
//        result.andExpect(status().isOk())
//                .andExpect(hasKey(expected));
//    }

    @Test
    void checkDuplicateEmail_true() throws Exception {
        // given
        String email = "example@email.com";
        Boolean duplicated = true;

        Response<Boolean> expected = CommonResponse.success(duplicated);
        given(memberService.isDuplicateEmail(email)).willReturn(duplicated);

        // when
        ResultActions result = mockMvc.perform(
                get("/api/members/check/email")
                        .param("email", email)
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        result.andExpect(status().isOk())
                .andExpect(hasKey(expected));
    }

    @Test
    void checkDuplicateEmail_false() throws Exception {
        // given
        String email = "example@email.com";
        Boolean duplicated = false;

        Response<Boolean> expected = CommonResponse.success(duplicated);
        given(memberService.isDuplicateEmail(email)).willReturn(duplicated);

        // when
        ResultActions result = mockMvc.perform(
                get("/api/members/check/email")
                        .param("email", email)
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        result.andExpect(status().isOk())
                .andExpect(hasKey(expected));
    }

    @Test
    void checkDuplicateNickname_true() throws Exception {
        // given
        String nickname = "nickname";
        Boolean duplicated = true;

        Response<Boolean> expected = CommonResponse.success(duplicated);
        given(memberService.isDuplicateNickname(nickname)).willReturn(duplicated);

        // when
        ResultActions result = mockMvc.perform(
                get("/api/members/check/nickname")
                        .param("nickname", nickname)
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        result.andExpect(status().isOk())
                .andExpect(hasKey(expected));
    }

    @Test
    void checkDuplicateNickname_false() throws Exception {
        // given
        String nickname = "nickname";
        Boolean duplicated = false;

        Response<Boolean> expected = CommonResponse.success(duplicated);
        given(memberService.isDuplicateNickname(nickname)).willReturn(duplicated);

        // when
        ResultActions result = mockMvc.perform(
                get("/api/members/check/nickname")
                        .param("nickname", nickname)
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        result.andExpect(status().isOk())
                .andExpect(hasKey(expected));
    }
}