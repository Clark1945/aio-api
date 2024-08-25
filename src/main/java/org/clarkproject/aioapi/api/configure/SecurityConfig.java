package org.clarkproject.aioapi.api.configure;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import lombok.extern.slf4j.Slf4j;
import org.clarkproject.aioapi.api.obj.enums.MemberStatus;
import org.clarkproject.aioapi.api.obj.po.MemberPO;
import org.clarkproject.aioapi.api.repository.MemberRepository;
import org.clarkproject.aioapi.api.service.JWTService;
import org.clarkproject.aioapi.api.tool.JWTAuthenticationFilter;
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
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import java.util.List;


@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
@Configuration
@EnableWebSecurity
@Slf4j
public class SecurityConfig {

    private final MemberRepository memberRepository;

    public SecurityConfig(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    /**
     * 查詢所有已啟用的帳號，並將資訊存到UserDetailsServiceImpl中
     * @return
     */
    @Bean
    public UserDetailsService userDetailsService() {
        List<MemberPO> memberPOList = memberRepository.findAllByStatus(MemberStatus.ACTIVE.name());
        log.info("Loaded {} members!", memberPOList.size());
        return new UserDetailsServiceImpl(memberPOList); // 設定可以登入的User
    }

    /**
     * 密碼加密工具 ( 選擇暫時不加密 )
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
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity, JWTAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        return httpSecurity
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers(HttpMethod.POST, "/api/1.0/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/1.0/loginWithBasicToken").hasAuthority("USER")
                        .requestMatchers(HttpMethod.POST, "/api/1.0/member").permitAll()
                        .requestMatchers(HttpMethod.GET,"/swagger-ui/*").permitAll()
                        .anyRequest().permitAll()
                )
                .csrf(AbstractHttpConfigurer::disable) // disable CSRF
//                .formLogin(Customizer.withDefaults()) // 用Spring Security提供的表單登入
                .httpBasic(Customizer.withDefaults()) // HTTP BASIC 登入
                .addFilterBefore(jwtAuthenticationFilter, BasicAuthenticationFilter.class) // FWT認證filter
                .build();
    }


    @Bean
    public JWTService JWTService(
            @Value("${jwt.secret-key}") String secretKeyStr,
            @Value("${jwt.valid-seconds}") int validSeconds
    ) {
        return new JWTService(secretKeyStr, validSeconds);
    }

    /**
     * Spring Security認證元件
     */
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity.getSharedObject(AuthenticationManagerBuilder.class).build();
    }

    /**
     * 在記憶體中管理帳號
     * Spring Security的User類別實作了UserDetails，收集完後放到InMemoryUserDetailsManager交由記憶體管理
     * @return
     */
//    @Bean
//    public UserDetailsService inMemoryUserDetailsManager() { // 使用原生User物件的做法
//        List<MemberPO> memberPOList = memberRepository.findAllByStatus(MemberStatus.ACTIVE.name());
//        // 以自定義類別取代
//        List<UserDetails> registeredUserList = memberList.stream()
//                .map(member -> User
//                        .withUsername(member.getAccount())
//                        .password(member.getPassword()) // {noop} 代表不加密 {bcrypt} BCrypt演算法、{sha256}SHA256演算法
//                        .authorities("ROLE_" + member.getRole().name())
//                        .build())
//                .collect(Collectors.toList());
//        List<UserDetails> registeredUserList = memberPOList.stream()
//                .map(MemberUserDetails::new)
//                .collect(Collectors.toList());
//        return new InMemoryUserDetailsManager(registeredUserList); // 設定可以登入的User
//    }
}