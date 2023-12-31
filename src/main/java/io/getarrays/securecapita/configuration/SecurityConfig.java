package io.getarrays.securecapita.configuration;

import io.getarrays.securecapita.filter.CustomAuthorizationFilter;
import io.getarrays.securecapita.handler.CustomAccesDeniedHandler;
import io.getarrays.securecapita.handler.CustomAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private static final String[] PUBLIC_URLS =
            {
            "/user/login/**",
            "/user/verify/code/**" ,
            "/user/register/**",
            "/user/resetPassword/**",
            "/user/verify/password/**",
            "/user/verify/account/**",
            "/user/refresh/token/**"
             };
    private final BCryptPasswordEncoder encoder;
    private final CustomAccesDeniedHandler customAccesDeniedHandler;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final UserDetailsService userDetailsService;
    private final CustomAuthorizationFilter customAuthorizationFilter;


    @Bean
    public SecurityFilterChain securityFilterChain (HttpSecurity http) throws Exception {
        http.csrf().disable().cors().disable();
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http.authorizeHttpRequests().requestMatchers(PUBLIC_URLS).permitAll();
        http.authorizeHttpRequests().requestMatchers(HttpMethod.DELETE, "/user/delete/**").hasAuthority("DELETE:USER");
        http.authorizeHttpRequests().requestMatchers(HttpMethod.DELETE, "/customer/delete/**").hasAuthority("DELETE:CUSTOMER");
        http.exceptionHandling().accessDeniedHandler(customAccesDeniedHandler).authenticationEntryPoint(customAuthenticationEntryPoint);
        http.authorizeHttpRequests().anyRequest().authenticated();
        http.addFilterBefore(customAuthorizationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager () {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(encoder);

        return new ProviderManager(authProvider);
    }
}
