package com.example.stajirovka.jwt;

import com.example.stajirovka.config.CustomUserDetails;
import com.example.stajirovka.exception.GenericRuntimeException;
import com.example.stajirovka.service.CustomUserDetailsService;
import com.example.stajirovka.service.JwtTokenService;
import com.example.stajirovka.util.ApiConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.LocaleResolver;

import java.io.IOException;
import java.util.Locale;
import java.util.Objects;

import static com.example.stajirovka.config.SecurityConfig.AUTH_WHITELIST;

@Component
@Slf4j
@RequiredArgsConstructor
public class AuthorizationFilter extends OncePerRequestFilter {

    private final JwtTokenService jwtTokenService;
    private final CustomUserDetailsService userDetailsService;
    private final LocaleResolver localeResolver;
    @Autowired
    @Qualifier("handlerExceptionResolver")
    private HandlerExceptionResolver resolver;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        long start = System.currentTimeMillis();
        String requestUri = request.getRequestURI();
        if (!isOpenPath(requestUri)) {
            try {
                String token = getTokenFromRequest(request);
                if (jwtTokenService.isValid(token)) {
                    String username = jwtTokenService.subject(token);
                    CustomUserDetails customUserDetails = userDetailsService.loadUserByUsername(username);
                    authenticate(request, customUserDetails);
                    log.info("User authenticated by id: {}", customUserDetails.getId());
                }
            } catch (GenericRuntimeException e) {
                log.error("Global filter error", e);
                resolver.resolveException(request, response, null, e);
                return;
            }
        }
        setLang(request, response);
        filterChain.doFilter(request, response);
        long finish = System.currentTimeMillis();
        log.info("->->Request = [ {}?{} ] Elapsed time to proceed this request = {}", request.getRequestURI(),
                request.getQueryString() == null ? "" : request.getQueryString(), finish - start);
    }

    private void authenticate(HttpServletRequest request, CustomUserDetails userDetails) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(ApiConstants.HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer")) {
            return bearerToken.trim().substring(7);
        } else {
            throw new RuntimeException("token.is.null");
        }
    }

    private boolean isOpenPath(String currentPath) {
        for (String path : AUTH_WHITELIST) {
            if (currentPath.contains(path)) {
                return true;
            }
        }
        return false;
    }

    private void setLang(HttpServletRequest request, HttpServletResponse response) {
        String header = request.getHeader(ApiConstants.LANG);
        localeResolver.setLocale(request, response, new Locale(Objects.requireNonNullElse(header, ApiConstants.DEFAULT_LANG)));
    }
}
