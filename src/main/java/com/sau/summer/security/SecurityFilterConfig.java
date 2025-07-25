package com.sau.summer.security;

import com.sau.summer.enums.Role;
import com.sau.summer.repository.CommitteeRepo;
import com.sau.summer.repository.StudentRepo;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
public class SecurityFilterConfig {

    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    @Autowired
    private StudentRepo studentRepo;
    @Autowired
    private CommitteeRepo committeeRepo;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/error").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/initialize/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/initialize/**").permitAll()
                        .requestMatchers("/dashboard", "/applications/**", "/settings/**", "/external/**", "/university/**",
                                "/profile", "/change-password").hasAnyRole(Role.STUDENT.name(), Role.COMMITTEE.name(), Role.ADMIN.name())
                        .anyRequest().authenticated()
                )
                .userDetailsService(userDetailsService)
                .sessionManagement(session -> session.maximumSessions(1))
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .successHandler(authenticationSuccessHandler())
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return (request, response, authentication) -> {
            HttpSession session = request.getSession(true);

            var userDetails = (UserDetailsImpl) authentication.getPrincipal();
            String role = userDetails.getRole();
            Long id = userDetails.getId();

            session.setAttribute("role", role);

            if ("STUDENT".equals(role)) {
                studentRepo.findById(id).ifPresent(student -> session.setAttribute("student", student));
            } else if ("COMMITTEE".equals(role)) {
                committeeRepo.findById(id).ifPresent(committee -> session.setAttribute("committee", committee));
            } else if ("ADMIN".equals(role)) {
                committeeRepo.findById(id).ifPresent(committee -> session.setAttribute("committee", committee));
            }

            response.sendRedirect("/dashboard");
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Şifreler bcrypt ile encode edilecek
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
