package com.springSecurity.test.config;

import com.springSecurity.test.auth.model.UserRole;
import com.springSecurity.test.exception.AuthFailHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private AuthFailHandler authFailHandler;

    @Autowired
    public SecurityConfig(AuthFailHandler authFailHandler) {
        this.authFailHandler = authFailHandler;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring()
                         .requestMatchers(PathRequest.toStaticResources().atCommonLocations());
    }

    @Bean
    public SecurityFilterChain filterChainConfigure(HttpSecurity http) throws Exception {

        // #1. 접근 제어 : 서버의 리소스에 접근 가능한 권한을 URL 별로 매칭하여 설정.
        http.authorizeHttpRequests(auth -> {
            // 로그인,회원가입, 실패 페이지와 Root Context는 모두에게 허용
            auth.requestMatchers("/auth/login", "/mapper/user/signup", "/auth/fail", "/").permitAll();
            // "/admin/*" 엔드포이트는 "ADMIN" 권한을 가진 사용자만 접근 허용
            auth.requestMatchers("/admin/*").hasAnyAuthority(UserRole.ADMIN.getRole());
            // "/user/*" 엔드포이트는 "USER" 권한을 가진 사용자만 접근 허용
            auth.requestMatchers("/mapper/user/*").hasAnyAuthority(UserRole.USER.getRole(),UserRole.ADMIN.getRole());
            // 나머지 요청은 모두 인증된(로그인한) 사용자만 접근 가능
            auth.anyRequest().authenticated();
            // #2. 로그인 관리 : <form> 태그를 사용한 로그인(form-login) 관련 설정
        }).formLogin(login -> {
            // 로그인 페이지 경로 설정(로그인 페이지에 해당되는 핸들러 패밍이 존재해야 함)
            login.loginPage("/auth/login");
            // 사용자 ID 입력 필드(form 데이터 input의 name 속성과 일치)
            login.usernameParameter("username");
            // 사용자 PW 입력 필드(form 데이터 input의 name 속성과 일치)
            login.passwordParameter("password");
            // 로그인 성공시 이동할 기본 페이지(로그인 성공 페이지에 해당되는 핸들러 매핑이 존재해야함)
            login.defaultSuccessUrl("/", true);
            // 로그인 실패시, 해당 예외를 처리할 핸들러 지정(직접 제작한 핸들러 사용)
            login.failureHandler(authFailHandler);
            // #3. 로그아웃 관리 : 로그아웃 요청시 관련 처리 설정
        }).logout(logout -> {
            // 로그아웃 경로 설정
            logout.logoutRequestMatcher(new AntPathRequestMatcher("/auth/logout"));
            // 로그아웃 시, 클라이언트의 JSESSIONID 쿠키 삭제
            logout.deleteCookies("JSESSIONID");
            // 세션을 무효화(소멸)하는 설정
            logout.invalidateHttpSession(true);
            // 로그아웃 성공 시 이동할 페이지의 URL 경로
            logout.logoutSuccessUrl("/");
            // #4. 세션관리 : 애플리케이션 내 세션 관리 설
        }).sessionManagement(session -> {
            // 동시 세션 수(허용 개수)를 1개로 제한
            session.maximumSessions(1);
            // 세션 만료시 이동할 페이지 URL 경로
            session.invalidSessionUrl("/");
            // #5. CSRF 설정
        }).csrf(csrf ->
            // CSRF 보호 비활성화
            csrf.disable()
        );

        return http.build();
    }
}
