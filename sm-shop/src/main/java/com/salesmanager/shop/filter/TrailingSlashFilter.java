package com.salesmanager.shop.filter;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Normalizes request paths ending with a trailing slash so that
 * "/api/v1/category/" is handled by the "/api/v1/category" mapping.
 */
public class TrailingSlashFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    String uri = request.getRequestURI();
    if (uri.length() > 1 && uri.endsWith("/")) {
      String trimmed = uri.substring(0, uri.length() - 1);
      request = new HttpServletRequestWrapper(request) {
        @Override
        public String getRequestURI() {
          return trimmed;
        }

        @Override
        public StringBuffer getRequestURL() {
          StringBuffer url = super.getRequestURL();
          url.setLength(url.length() - 1);
          return url;
        }

        @Override
        public String getServletPath() {
          String path = super.getServletPath();
          return path.length() > 1 && path.endsWith("/") ? path.substring(0, path.length() - 1)
              : path;
        }
      };
    }
    filterChain.doFilter(request, response);
  }
}
