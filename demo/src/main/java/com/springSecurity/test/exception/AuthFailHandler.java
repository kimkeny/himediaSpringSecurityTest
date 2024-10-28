package com.springSecurity.test.exception;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import java.io.IOException;
import java.net.URLEncoder;

@Configuration
public class AuthFailHandler extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest req, HttpServletResponse res, AuthenticationException exception)
            throws IOException, ServletException {

        String errorMessage = null;

        if (exception instanceof BadCredentialsException) {

            errorMessage = "[Auth-Failed] 아이디가 존재하지 않거나 비밀번호가 일치하지 않습니다.";
        } else if (exception instanceof InternalAuthenticationServiceException) {

            errorMessage = "[Auth-Failed] 서버에서 오류가 발생되었습니다.";
        } else if (exception instanceof UsernameNotFoundException) {

            errorMessage = "[Auth-Failed] 존재하지 않는 이메일 입니다.";
        } else if (exception instanceof AuthenticationCredentialsNotFoundException) {

            errorMessage = "[Auth-Failed] 인증 요청이 거부되었습니다.";
        } else {

            errorMessage = "[Auth-Failed] 알 수 없는 오류로 로그인 요청을 처리할 수 없습니다.";
        }

        errorMessage = URLEncoder.encode(errorMessage, "UTF-8");

        setDefaultFailureUrl("/auth/fail?message=" + errorMessage);;

        super.onAuthenticationFailure(req, res, exception);
    }
}
