package com.example.nineuniverse.security;

import com.example.nineuniverse.dev.DevTestUserLoginBaselineService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
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
	private final UserDetailsService userDetailsService;

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
			DevTestUserAwareAuthenticationSuccessHandler devTestUserAwareAuthenticationSuccessHandler,
			@Value("${app.security.remember-me.key:nine-universe-remember-me-key}") String rememberMeKey,
			@Value("${app.security.remember-me.token-validity-seconds:604800}") int rememberMeTokenValiditySeconds)
			throws Exception {
		int validitySeconds = Math.clamp(rememberMeTokenValiditySeconds, 3_600, 60 * 60 * 24 * 30);
		http
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/login", "/register", "/how-to-play", "/css/**", "/js/**", "/images/**", "/pack-art/**", "/downloads/**", "/error", "/api/client-asset-version", "/api/desktop-client-update").permitAll()
						.anyRequest().authenticated())
				.formLogin(form -> form
						.loginPage("/login")
						.successHandler(devTestUserAwareAuthenticationSuccessHandler)
						.permitAll())
				.rememberMe(remember -> remember
						.key(rememberMeKey)
						.tokenValiditySeconds(validitySeconds)
						.userDetailsService(userDetailsService)
						.alwaysRemember(true))
				.logout(logout -> logout
						.logoutSuccessUrl("/login?logout")
						.permitAll())
				.addFilterAfter(lastAccessUpdateFilter, UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}
}
