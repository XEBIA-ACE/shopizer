package com.salesmanager.test.shop.security;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import com.salesmanager.shop.application.config.SsoSecurityConfig;
import com.salesmanager.shop.store.controller.sso.SsoLoginController;

@Configuration
@Import({ SsoSecurityConfig.class, SsoLoginController.class })
public class SsoTestConfiguration {

	@Bean
	public MessageSource messageSource() {
		ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
		messageSource.setBasenames("classpath:bundles/messages");
		messageSource.setDefaultEncoding("UTF-8");
		return messageSource;
	}
}
