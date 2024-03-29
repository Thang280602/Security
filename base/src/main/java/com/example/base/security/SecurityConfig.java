package com.example.base.security;

import com.example.base.constant.SecurityConstant;
import com.example.base.enumeration.RoleEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * @author HungDV
 * <p>
 * Class cung cấp cấu hình chung cho Spring Security.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends Exception{

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserDetailsService userDetailsService;
    /**
     * Tạo 1 Bean cho SecurityFilterChain.
     * Sử dụng SecurityFilterChain caung cấp cấu hình cho Spring Security.
     * Bao gồm về cors, crsf, j session, phân quyển endpoint, thêm filter...
     *
     * @param http là object HttpSecurity để cung cấp các cấu hình cho Spring Security.
     * @return object SecurityFilterChain.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        user chỉ xem users/** (get)

        return http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(req -> {
//                    req.requestMatchers(HttpMethod.GET,SecurityConstant.PRIVATE_URIS_ROLE_USER).hasAnyAuthority(RoleEnum.ROLE_USER.toString());
//                    req.requestMatchers(SecurityConstant.PRIVATE_URIS_ROLE_ADMIN).hasAuthority(RoleEnum.ROLE_ADMIN.toString());
                    req.requestMatchers(HttpMethod.GET,"/users/*")
                            .hasAnyRole(RoleEnum.USER.name(),RoleEnum.ADMIN.name());
                    req.requestMatchers("/users","/users/**")
                            .hasAnyRole(RoleEnum.ADMIN.name());
                    req.anyRequest()
                            .permitAll();

                })
                .sessionManagement(
                        httpSecuritySessionManagement -> httpSecuritySessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

                .build();
    }




    /**
     * Bean để dùng quản lý các người dùng đã đăng nhập
     * @param config AuthenticationManager.class
     * @return AuthenticationManager object bean
     * @throws Exception
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }


    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder);
        return authenticationProvider;
    }
    // User Creation

    /**
     * Tạo 1 Bean cho CorsConfigurationSource cung cấp cấu hình cho bảo mật Cors.
     * Đặt tạm thời cho phép truy cập endpoint từ mọi nguồn gốc, mọi method, mọi header.
     *
     * @return object CorsConfigurationSource.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("*"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}