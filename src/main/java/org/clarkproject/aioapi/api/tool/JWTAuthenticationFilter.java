package org.clarkproject.aioapi.api.tool;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.clarkproject.aioapi.api.obj.MemberUserDetails;
import org.clarkproject.aioapi.api.repository.MemberRepository;
import org.clarkproject.aioapi.api.service.JWTService;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;


@Component
public class JWTAuthenticationFilter extends OncePerRequestFilter {
    private static final List<String> INCLUDE_PATHS = Arrays.asList("/api/1.0/loginWithJWTToken");
    private static final String BEARER_PREFIX = "Bearer ";
    private final JWTService jwtService;
    private final MemberRepository memberRepository;
    public JWTAuthenticationFilter(JWTService jwtService, MemberRepository memberRepository) {
        this.jwtService = jwtService;
        this.memberRepository = memberRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 取得 header
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
        }
        // 解析 JWT
        String jwt = authHeader.substring(BEARER_PREFIX.length());
        Claims claims;
        try {
            claims = jwtService.parseToken(jwt);
        } catch (JwtException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // 建立 UserDetails 物件
        MemberUserDetails userDetails = new MemberUserDetails(memberRepository.findByAccount(claims.get("username", String.class)));
        userDetails.setUsername(claims.get("username", String.class));

        List<String> memberAuthorities = ((ArrayList<LinkedHashMap<String,String>>) claims.get("authorities"))
                .stream()
                .map( a -> a.get("authority"))
                .collect(Collectors.toList());

        userDetails.setMemberAuthorities(memberAuthorities);

        // 放入 Security Context
        Authentication token = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(token);

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return !INCLUDE_PATHS.contains(request.getServletPath());
    }
}