package com.plog.testUtil;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

/**
 * 테스트 환경에서 {@link WithCustomMockUser} 어노테이션을 통해
 * Spring Security {@link SecurityContext}를 구성하는 Factory 클래스입니다.
 *
 * <p>
 * {@link WithCustomMockUser#userId()}로 전달받은 사용자 ID를
 * {@link UsernamePasswordAuthenticationToken}의 principal로 설정하여,
 * 컨트롤러에서 {@code @AuthenticationPrincipal Long} 형태로
 * 사용자 식별자를 직접 주입받을 수 있도록 합니다.
 *
 * <p>
 * 해당 Factory는 {@link org.springframework.security.test.context.support.WithSecurityContext}
 * 메커니즘을 사용하며, 각 테스트 실행 시마다 새로운 {@link SecurityContext}를 생성하여
 * 테스트 간 {@link org.springframework.security.core.context.SecurityContextHolder} 오염을 방지합니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@link WithSecurityContextFactory}를 구현하여
 * {@link WithCustomMockUser} 어노테이션이 선언된 테스트에 대해
 * SecurityContext 생성을 담당합니다.
 *
 * <p><b>주요 생성자:</b><br>
 * 기본 생성자를 사용하며, 별도의 상태를 가지지 않습니다.
 *
 * <p><b>빈 관리:</b><br>
 * 본 클래스는 Spring Bean으로 관리되지 않으며,
 * Spring Security Test 프레임워크에 의해 리플렉션 기반으로 사용됩니다.
 *
 * <p><b>외부 모듈:</b><br>
 * spring-security-test
 *
 * @author jack8
 * @see WithCustomMockUser
 * @see WithSecurityContextFactory
 * @see UsernamePasswordAuthenticationToken
 * @since 2026-01-20
 */
public class WithSecurityMockUserContextFactory implements WithSecurityContextFactory<WithCustomMockUser> {
    @Override
    public SecurityContext createSecurityContext(WithCustomMockUser annotation) {
        Long userId = annotation.userId();

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                userId,
                null,
                AuthorityUtils.NO_AUTHORITIES
        );

        SecurityContext context = new SecurityContextImpl();
        context.setAuthentication(auth);
        return context;
    }
}
