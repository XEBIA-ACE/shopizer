package com.salesmanager.shop.store.security;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.util.Assert;

/**
 * Redirects unauthenticated requests on protected storefront routes to the SSO
 * login entry point page. Stateless and thread-safe.
 */
public class SsoLoginEntryPoint implements AuthenticationEntryPoint {

	private final String loginPath;

	public SsoLoginEntryPoint(String loginPath) {
		Assert.hasText(loginPath, "loginPath must not be empty");
		Assert.isTrue(loginPath.startsWith("/"), "loginPath must start with '/'");
		this.loginPath = loginPath;
	}

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException {
		response.sendRedirect(request.getContextPath() + loginPath);
	}

	public String getLoginPath() {
		return loginPath;
	}
}
