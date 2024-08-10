package org.clarkproject.aioapi.api.configure;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.clarkproject.aioapi.api.obj.Member;
import org.clarkproject.aioapi.api.obj.MemberStatus;
import org.clarkproject.aioapi.api.obj.MemberUserDetails;
import org.clarkproject.aioapi.api.orm.MemberPO;
import org.clarkproject.aioapi.api.repository.MemberRepository;
import org.clarkproject.aioapi.api.service.JWTService;
import org.clarkproject.aioapi.api.service.MemberService;
import org.clarkproject.aioapi.api.tool.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;
import java.util.Properties;
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

    private final MemberRepository memberRepository;

    public SecurityConfig(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    /**
     * 在記憶體中管理帳號
     * Spring Security的User類別實作了UserDetails，收集完後放到InMemoryUserDetailsManager交由記憶體管理
     * @return
     */
    @Bean
    public UserDetailsService inMemoryUserDetailsManager() {
        List<MemberPO> memberPOList = memberRepository.findAllByStatus(MemberStatus.ACTIVE.name());
//        以自定義類別取代
//        List<UserDetails> registeredUserList = memberList.stream()
//                .map(member -> User
//                        .withUsername(member.getAccount())
//                        .password(member.getPassword()) // {noop} 代表不加密 {bcrypt} BCrypt演算法、{sha256}SHA256演算法
//                        .authorities("ROLE_" + member.getRole().name())
//                        .build())
//                .collect(Collectors.toList());
        List<UserDetails> registeredUserList = memberPOList.stream()
                .map(MemberUserDetails::new)
                .collect(Collectors.toList());
        return new InMemoryUserDetailsManager(registeredUserList); // 設定可以登入的User
    }

    /**
     * 密碼加密工具 ( 選擇暫時不加密 )
     *
     * @return
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }

    /**
     * 可以為各個API設定訪問權限與CSRF
     *
     * @param httpSecurity
     * @return
     * @throws Exception
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers(HttpMethod.POST, "/api/1.0/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/1.0/loginWithBasicToken").hasAuthority("USER")
                        .requestMatchers(HttpMethod.POST, "/api/1.0/member").permitAll()
                        .anyRequest().permitAll()
                )
                .csrf(AbstractHttpConfigurer::disable) // disable CSRF
//                .formLogin(Customizer.withDefaults()) // 用Spring Security提供的表單登入
//                .httpBasic(Customizer.withDefaults()) // HTTP BASIC 登入
                .build();
    }
//        return httpSecurity
//                .formLogin(Customizer.withDefaults())
//                .authorizeHttpRequests(requests -> requests
//                                .requestMatchers(HttpMethod.GET, "/api/1.0/member").permitAll()
//                                .requestMatchers(HttpMethod.POST, "/api/1.0/login").permitAll()
//                                .requestMatchers(HttpMethod.GET, "/api/1.0/who-am-i").permitAll()
//                                .requestMatchers(HttpMethod.GET, "/").permitAll()
//                                .requestMatchers(HttpMethod.GET, "/api/1.0/wallet_record").hasAnyAuthority("USER", "ADMIN")
//                                .requestMatchers(HttpMethod.GET, "/api/1.0/wallet").hasAnyAuthority("USER", "ADMIN")
//                                .requestMatchers(HttpMethod.POST, "/api/1.0/member").hasAnyAuthority("USER", "ADMIN")
//                                .requestMatchers(HttpMethod.PUT, "/api/1.0/member").hasAnyAuthority("USER", "ADMIN")
//                                .requestMatchers(HttpMethod.POST, "/api/1.0/wallet").hasAnyAuthority("USER", "ADMIN")
//                                .requestMatchers(HttpMethod.POST, "/api/1.0/withdraw").hasAnyAuthority("USER", "ADMIN")
//                                .requestMatchers(HttpMethod.POST, "/api/1.0/transfer").hasAnyAuthority("USER", "ADMIN")
//                                .requestMatchers(HttpMethod.POST, "/api/1.0/deposit").hasAnyAuthority("USER", "ADMIN")
//                                .requestMatchers(HttpMethod.DELETE, "/api/1.0/member").hasAuthority("ADMIN")
//                                .requestMatchers(HttpMethod.PATCH, "/api/1.0/member").hasAuthority("ADMIN")
//                                .requestMatchers(HttpMethod.DELETE, "/api/1.0/wallet").hasAuthority("ADMIN")
//                                .requestMatchers(HttpMethod.PATCH, "/api/1.0/wallet").hasAuthority("ADMIN")
//                                .requestMatchers(HttpMethod.GET, "/swagger-ui/index").authenticated()
////                        .access(new WebExpressionAuthorizationManager("hasAuthority('ADMIN') AND hasAuthority('TEACHER')")) and 查詢
//                                .anyRequest().authenticated()
//                ).csrf(AbstractHttpConfigurer::disable)
//                .build();

    @Bean
    public JWTService JWTService(
            @Value("${jwt.secret-key}") String secretKeyStr,
            @Value("${jwt.valid-seconds}") int validSeconds
    ) {
        return new JWTService(secretKeyStr, validSeconds);
    }
//
//    @Bean
//    @ConditionalOnMissingBean(AuthenticationEventPublisher.class)
//    DefaultAuthenticationEventPublisher defaultAuthenticationEventPublisher(ApplicationEventPublisher delegate) {
//        return new DefaultAuthenticationEventPublisher(delegate);
//    }
//
    /**
     * Spring Security認證元件
     */
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity.getSharedObject(AuthenticationManagerBuilder.class).build();
    }

//    ?????
//    @Bean
//    public UserDetailsService userDetailsService() {
//        List<MemberPO> memberPOList = memberRepository.findAllByStatus(MemberStatus.ACTIVE.name());
//        return new UserDetailsServiceImpl(memberRepository,memberPOList);
//    }
}