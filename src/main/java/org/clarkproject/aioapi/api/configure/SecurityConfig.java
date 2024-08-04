package org.clarkproject.aioapi.api.configure;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.clarkproject.aioapi.api.obj.Member;
import org.clarkproject.aioapi.api.service.JWTService;
import org.clarkproject.aioapi.api.service.MemberService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;
import java.util.stream.Collectors;


@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final MemberService memberService;
    public SecurityConfig(MemberService memberService) {
        this.memberService = memberService;
    }

    /**
     * 設定 可登入User與權限設定
     * @return
     */
    @Bean
    InMemoryUserDetailsManager inMemoryUserDetailsManager() {
        List<Member> memberList = memberService.findAllActiveMember();
        List<UserDetails> registeredUserList = memberList.stream()
                .map( member -> User
                    .withUsername(member.getAccount())
                    .password("{noop}" + member.getPassword()) // noop 代表不加密 另外可使用 BCrypt、SHA256等
                    .authorities(member.getStatus().name())
                    .build())
                .collect(Collectors.toList());

        return new InMemoryUserDetailsManager(registeredUserList); // 設定可以登入的User
    }

    /**
     * 可以為各個API設定訪問權限與CSRF
     * @param httpSecurity
     * @return
     * @throws Exception
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .formLogin(Customizer.withDefaults())
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers(HttpMethod.GET, "/api/1.0/member").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/1.0/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/1.0/wallet_record").hasAnyAuthority("USER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/1.0/wallet").hasAnyAuthority("USER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/1.0/member").hasAnyAuthority("USER","ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/1.0/member").hasAnyAuthority("USER","ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/1.0/wallet").hasAnyAuthority("USER","ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/1.0/withdraw").hasAnyAuthority("USER","ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/1.0/transfer").hasAnyAuthority("USER","ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/1.0/deposit").hasAnyAuthority("USER","ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/1.0/member").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/1.0/member").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/1.0/wallet").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/1.0/wallet").hasAuthority("ADMIN")
//                        .access(new WebExpressionAuthorizationManager("hasAuthority('ADMIN') AND hasAuthority('TEACHER')")) and 查詢
                        .anyRequest().authenticated()
                ).csrf(AbstractHttpConfigurer::disable)
                .build();
    }

    /**
     * 密碼加密工具
     * @return
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }

    @Bean
    public JWTService JWTService(
            @Value("${jwt.secret-key}") String secretKeyStr,
            @Value("${jwt.valid-seconds}") int validSeconds
    ) {
        return new JWTService(secretKeyStr, validSeconds);
    }
    @Bean
    @ConditionalOnMissingBean(AuthenticationEventPublisher.class)
    DefaultAuthenticationEventPublisher defaultAuthenticationEventPublisher(ApplicationEventPublisher delegate) {
        return new DefaultAuthenticationEventPublisher(delegate);
    }
}