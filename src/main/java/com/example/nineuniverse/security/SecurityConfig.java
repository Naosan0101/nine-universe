package com.example.nineuniverse.security;

import com.example.nineuniverse.dev.DevTestUserLoginBaselineService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final LastAccessUpdateFilter lastAccessUpdateFilter;
	private final DevTestUserLoginBaselineService devTestUserLoginBaselineService;

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	DevTestUserAwareAuthenticationSuccessHandler devTestUserAwareAuthenticationSuccessHandler() {
		return new DevTestUserAwareAuthenticationSuccessHandler(devTestUserLoginBaselineService);
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http,
			DevTestUserAwareAuthenticationSuccessHandler devTestUserAwareAuthenticationSuccessHandler) throws Exception {
		http
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/login", "/register", "/how-to-play", "/css/**", "/js/**", "/images/**", "/error").permitAll()
						.anyRequest().authenticated())
				.formLogin(form -> form
						.loginPage("/login")
						.successHandler(devTestUserAwareAuthenticationSuccessHandler)
						.permitAll())
				.logout(logout -> logout
						.logoutSuccessUrl("/login?logout")
						.permitAll())
				.addFilterAfter(lastAccessUpdateFilter, UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}
}
