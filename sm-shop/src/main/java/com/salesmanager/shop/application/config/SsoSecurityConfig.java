package com.salesmanager.shop.application.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.savedrequest.NullRequestCache;

import com.salesmanager.shop.store.controller.sso.SsoLoginController;
import com.salesmanager.shop.store.security.SsoLoginEntryPoint;

/**
 * Storefront routes that require an SSO-authenticated user. Unauthenticated
 * requests are redirected to the SSO login entry point; no credential form is
 * ever rendered by this application for these routes.
 */
@Configuration
@Order(0)
public class SsoSecurityConfig extends WebSecurityConfigurerAdapter {

	public static final String[] PROTECTED_ROUTES = {
			"/cart/**",
			"/checkout/**",
			"/wishlist/**",
			"/address/**",
			"/orders/**"
	};

	@Bean
	public SsoLoginEntryPoint ssoLoginEntryPoint() {
		return new SsoLoginEntryPoint(SsoLoginController.PATH);
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
			.requestMatchers().antMatchers(PROTECTED_ROUTES)
			.and()
			.authorizeRequests().anyRequest().authenticated()
			.and()
			.exceptionHandling().authenticationEntryPoint(ssoLoginEntryPoint())
			.and()
			.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
			.and()
			.requestCache().requestCache(new NullRequestCache())
			.and()
			.formLogin().disable()
			.httpBasic().disable()
			.logout().disable()
			.csrf().disable();
	}
}
