package com.earn.earnmoney.security;

import com.earn.earnmoney.cors.CorsConfig;
import com.earn.earnmoney.security.jwt.AuthEntryPointJwt;
import com.earn.earnmoney.security.jwt.AuthTokenFilter;
import com.earn.earnmoney.security.services.UserDetailsServiceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableWebSecurity
public class WebSecurityConfig {

    @Autowired
    UserDetailsServiceImpl userDetailsService;

    @Autowired
    AuthEntryPointJwt unauthorizedHandler;

    @Autowired
    private CorsConfig corsConfig;

    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());

        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfiguration) throws Exception {
        return authConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors().and().csrf().disable()
                .exceptionHandling().authenticationEntryPoint(unauthorizedHandler).and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .authorizeRequests()
                .antMatchers("/api/auth/**").permitAll()
                .antMatchers("/api/ads/getimage/**").permitAll()
                .antMatchers("/api/payment/getimage/**").permitAll()
                .antMatchers("/api/withdraw/getimage/**").permitAll()
                .antMatchers("/api/mediashare/getimage/**").permitAll()
                .antMatchers("/api/userimage/getimage/**").permitAll()
                .antMatchers("/api/store/getimage/**").permitAll()
                .antMatchers("/api/marketplace/getimage/**").permitAll()
                .antMatchers("/api/agents/getimage/**").permitAll()
                .antMatchers("/api/profile/image/**").permitAll()
                .antMatchers("/api/users/register").permitAll()
                .antMatchers("/api/users/activecode/**").permitAll()
                .antMatchers("/api/users/checkactivecode/**").permitAll()
                .antMatchers("/api/users/forgot_password/**").permitAll()
                .antMatchers("/api/users/reset_password/**").permitAll()
                .antMatchers("/api/payment/v1/add").permitAll() //
                // Swagger UI endpoints
                .antMatchers("/swagger-ui/**", "/swagger-ui.html").permitAll()
                .antMatchers("/v3/api-docs/**", "/v3/api-docs.yaml").permitAll()
                .antMatchers("/swagger-resources/**").permitAll()
                .antMatchers("/webjars/**").permitAll()
                .antMatchers("/api/games/**").authenticated() // Games require auth
                .anyRequest().authenticated();

        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}
