package org.gnori.chatwebsockets.config.web.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static org.gnori.chatwebsockets.api.constant.Endpoint.*;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig implements WebMvcConfigurer {

    private final UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(
                        request -> request
                                .requestMatchers(HttpMethod.POST, (USERS + SIGN_UP_PATH), (USERS + SIGN_IN_PATH)).permitAll()
                                .requestMatchers(START_PAGE_PATH, "/js/**", "/css/**").permitAll()
                                .anyRequest().authenticated()
                )
                .formLogin(
                        login -> login
                                .loginPage(START_PAGE_PATH)
                                .loginProcessingUrl((USERS + SIGN_IN_PATH))
                                .defaultSuccessUrl(MAIN_PAGE_PATH)
                )
                .logout(logout ->
                        logout
                                .logoutRequestMatcher(new AntPathRequestMatcher(LOGOUT_PATH))
                                .deleteCookies("SESSION")
                                .logoutSuccessUrl(START_PAGE_PATH)
                )
                .userDetailsService(userDetailsService);

        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController(START_PAGE_PATH).setViewName("index.html");
        registry.addViewController(MAIN_PAGE_PATH).setViewName("onlyAuth.html");
    }

}
