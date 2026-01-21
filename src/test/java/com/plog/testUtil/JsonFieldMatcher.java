package com.plog.testUtil;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.plog.global.exception.errorCode.ErrorCode;
import com.plog.global.response.Response;
import org.hamcrest.core.IsNull;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MockMvc 테스트에서 JSON 응답의 필드 구조와 값을 검증하기 위한
 * 커스텀 {@link ResultMatcher} 구현체입니다.
 *
 * <p>
 * 단일 jsonPath 검증이 아닌, 여러 {@link ResultMatcher}를 조합하여
 * 하나의 응답(Response 구조 전체 등)을 한 번에 검증할 수 있도록 설계되었습니다.
 *
 * <p>
 * 특히 {@link Response} 래퍼 객체를 기준으로
 * <ul>
 *     <li>status / message / data 필드 검증</li>
 *     <li>중첩 객체 및 리스트 구조 재귀 검증</li>
 *     <li>{@link LocalDate}, {@link LocalTime}, {@link LocalDateTime} 직렬화 형식 대응</li>
 * </ul>
 * 을 지원합니다.
 *
 * <p>
 * 테스트 코드에서는 다음과 같은 형태로 사용됩니다.
 * <pre>
 * mockMvc.perform(...)
 *        .andExpect(JsonFieldMatcher.hasKey(expectedResponse));
 * </pre>
 *
 * <p><b>상속 정보:</b><br>
 * {@link ResultMatcher} 인터페이스를 구현하여
 * MockMvc의 {@code andExpect} 체인에서 사용됩니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@code JsonFieldMatcher(List<ResultMatcher> matchers)} <br>
 * 내부적으로 실행할 ResultMatcher 목록을 전달받습니다.
 *
 *
 * @author jack8
 * @since 2026-01-19
 */
public class JsonFieldMatcher implements ResultMatcher {

    private final List<ResultMatcher> matchers;

    /**
     * 여러 ResultMatcher를 묶어 하나의 Matcher로 구성합니다.
     */
    public JsonFieldMatcher(List<ResultMatcher> matchers) {
        this.matchers = matchers;
    }

    /**
     * 내부에 포함된 모든 {@link ResultMatcher}를 순차적으로 실행합니다.
     */
    @Override
    public void match(MvcResult result) throws Exception {
        for (ResultMatcher matcher : matchers) {
            matcher.match(result);
        }
    }

    /**
     * ErrorCode에 정의된 HttpStatus와 실제 응답 status를 비교 검증합니다.
     */
    public static ResultMatcher hasStatus(ErrorCode errorCode) {
        return result -> {
            HttpStatus status = errorCode.getHttpStatus();
            int actualStatus = result.getResponse().getStatus();
            assertThat(status.value())
                    .withFailMessage(
                            "기대한 Http status 는 %d 였지만, 실제는 %d 였습니다.",
                            status.value(), actualStatus
                    )
                    .isEqualTo(actualStatus);
        };
    }

    /**
     * 단일 JSON 필드(key=value)를 검증합니다.
     */
    public static ResultMatcher hasKey(String key, String value) {
        List<ResultMatcher> matchers = new ArrayList<>();
        matchers.add(MockMvcResultMatchers.jsonPath("$." + key).value(value));
        return new JsonFieldMatcher(matchers);
    }

    /**
     * 실패 응답(ErrorCode 기준)의 status/message 필드를 검증합니다.
     */
    public static ResultMatcher hasKey(ErrorCode errorCode) {
        List<ResultMatcher> matchers = new ArrayList<>();
        matchers.add(MockMvcResultMatchers.jsonPath("$.status").value("fail"));
        matchers.add(MockMvcResultMatchers.jsonPath("$.message").value(errorCode.getMessage()));
        return new JsonFieldMatcher(matchers);
    }

    /**
     * 실패 응답의 message 필드를 문자열 기준으로 검증합니다.
     */
    public static ResultMatcher hasKey(String message) {
        List<ResultMatcher> matchers = new ArrayList<>();
        matchers.add(MockMvcResultMatchers.jsonPath("$.status").value("fail"));
        matchers.add(MockMvcResultMatchers.jsonPath("$.message").value(message));
        return new JsonFieldMatcher(matchers);
    }

    /**
     * {@link Response} 객체를 기준으로 JSON 응답 전체를 검증합니다.
     *
     * <p>
     * - status / message 검증<br>
     * - data가 null인 경우 null 여부 검증<br>
     * - data가 객체/리스트인 경우 재귀적으로 필드 검증
     */
    public static <T> ResultMatcher hasKey(Response<T> response) {
        List<ResultMatcher> matchers = new ArrayList<>();

        matchers.add(MockMvcResultMatchers.jsonPath("$.status").value(response.getStatus()));

        if (response.getMessage() != null) {
            matchers.add(MockMvcResultMatchers.jsonPath("$.message").value(response.getMessage()));
        }

        T data = response.getData();

        if (data == null) {
            matchers.add(MockMvcResultMatchers.jsonPath("$.data").value(IsNull.nullValue()));
        } else {
            matchers.addAll(buildMatchersForValue("data", data));
        }

        return new JsonFieldMatcher(matchers);
    }

    /**
     * List 타입 데이터에 대한 검증 Matcher를 생성합니다.
     *
     * <p>
     * - 리스트 길이 검증<br>
     * - 요소가 1개인 경우 내부 값까지 재귀 검증
     */
    private static List<ResultMatcher> matcherForList(String root, List<?> values) {
        List<ResultMatcher> matchers = new ArrayList<>();

        matchers.add(
                MockMvcResultMatchers.jsonPath("$." + root + ".length()")
                        .value(values.size())
        );

        if (values.size() == 1) {
            Object value = values.get(0);
            String elementPath = String.format("%s[0]", root);
            matchers.addAll(buildMatchersForValue("$." + elementPath, value));
        }

        return matchers;
    }

    /**
     * 주어진 객체를 기준으로 JSON 경로별 검증 Matcher를 재귀적으로 생성합니다.
     *
     * <p>
     * 단순 타입, Java Time 타입, List, 복합 객체를 모두 처리합니다.
     */
    @SuppressWarnings("unchecked")
    private static List<ResultMatcher> buildMatchersForValue(String path, Object value) {
        List<ResultMatcher> matchers = new ArrayList<>();

        if (value == null) {
            matchers.add(MockMvcResultMatchers.jsonPath(path).value((Object) null));
        } else if (isSimpleValue(value)) {
            matchers.add(MockMvcResultMatchers.jsonPath(path).value(value));
        } else if (value instanceof LocalDate) {
            matchers.add(
                    MockMvcResultMatchers.jsonPath(path)
                            .value(((LocalDate) value).format(DateTimeFormatter.ISO_LOCAL_DATE))
            );
        } else if (value instanceof LocalTime) {
            matchers.add(
                    MockMvcResultMatchers.jsonPath(path)
                            .value(((LocalTime) value).format(DateTimeFormatter.ISO_LOCAL_TIME))
            );
        } else if (value instanceof LocalDateTime) {
            String formatted = ((LocalDateTime) value)
                    .truncatedTo(ChronoUnit.SECONDS)
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
            matchers.add(MockMvcResultMatchers.jsonPath(path).value(formatted));
        } else if (value instanceof List<?> list) {
            matchers.addAll(matcherForList(path, list));
        } else {
            ObjectMapper mapper = createMapper();
            Map<String, Object> nestedMap = mapper.convertValue(value, Map.class);
            for (Map.Entry<String, Object> entry : nestedMap.entrySet()) {
                matchers.addAll(
                        buildMatchersForValue(path + "." + entry.getKey(), entry.getValue())
                );
            }
        }

        return matchers;
    }

    /**
     * JSON Path에서 직접 비교 가능한 단순 타입 여부를 판단합니다.
     */
    private static boolean isSimpleValue(Object value) {
        return value instanceof String
               || value instanceof Number
               || value instanceof Boolean;
    }

    /**
     * Java Time 타입 직렬화를 위한 테스트 전용 {@link ObjectMapper}를 생성합니다.
     */
    private static ObjectMapper createMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }


}
