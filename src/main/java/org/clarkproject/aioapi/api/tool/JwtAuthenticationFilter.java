//package org.clarkproject.aioapi.api.tool;
//
//import io.jsonwebtoken.Claims;
//import io.jsonwebtoken.JwtException;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.clarkproject.aioapi.api.obj.MemberRole;
//import org.clarkproject.aioapi.api.obj.MemberUserDetails;
//import org.clarkproject.aioapi.api.service.JWTService;
//import org.springframework.http.HttpHeaders;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import java.io.IOException;
//import java.util.Arrays;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Component
//public class JwtAuthenticationFilter extends OncePerRequestFilter {
//    private static final List<String> EXCLUDED_PATHS = Arrays.asList("/login", "/who-am-i");
//    private static final String BEARER_PREFIX = "Bearer ";
//    private JWTService jwtService;
//    public JwtAuthenticationFilter(JWTService jwtService) {
//        this.jwtService = jwtService;
//    }
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
//                                    FilterChain filterChain) throws ServletException, IOException {
//        // 取得 header
//        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
//        if (authHeader == null) {
//            filterChain.doFilter(request, response);
//        }
//
//        // 解析 JWT
//        String jwt = authHeader.substring(BEARER_PREFIX.length());
//        Claims claims;
//        try {
//            claims = jwtService.parseToken(jwt);
//        } catch (JwtException e) {
//            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//            return;
//        }
//
//        // 建立 UserDetails 物件
//        MemberUserDetails userDetails = new MemberUserDetails();
//        System.out.println("Subject = " + claims.getSubject());
//        userDetails.setUserName(claims.get("username", String.class));
//
//        List<MemberRole> memberAuthorities = ((List<String>) claims.get("authorities"))
//                .stream()
//                .map(MemberRole::valueOf)
//                .collect(Collectors.toList());();
//        userDetails.setMemberAuthorities(memberAuthorities);
//
//        filterChain.doFilter(request, response);
//    }
//
//    @Override
//    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
//        return EXCLUDED_PATHS.contains(request.getServletPath());
//    }
//}
