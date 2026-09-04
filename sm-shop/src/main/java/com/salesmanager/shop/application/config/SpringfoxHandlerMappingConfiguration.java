package com.salesmanager.shop.application.config;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping;

import springfox.documentation.spring.web.plugins.WebMvcRequestHandlerProvider;

/**
 * Springfox 2.x only understands handler mappings using the ant path matcher. Handler mappings
 * relying on the pattern parser, such as the actuator ones, are filtered out of the springfox
 * request handler providers.
 */
@Configuration
public class SpringfoxHandlerMappingConfiguration {

	@Bean
	public static BeanPostProcessor springfoxHandlerProviderBeanPostProcessor() {
		return new BeanPostProcessor() {

			@Override
			public Object postProcessAfterInitialization(Object bean, String beanName) {
				if (bean instanceof WebMvcRequestHandlerProvider) {
					List<RequestMappingInfoHandlerMapping> handlerMappings = handlerMappings(bean);
					List<RequestMappingInfoHandlerMapping> antPathMatcherMappings = handlerMappings.stream()
							.filter(mapping -> mapping.getPatternParser() == null)
							.collect(Collectors.toList());
					handlerMappings.clear();
					handlerMappings.addAll(antPathMatcherMappings);
				}
				return bean;
			}

			@SuppressWarnings("unchecked")
			private List<RequestMappingInfoHandlerMapping> handlerMappings(Object bean) {
				Field field = ReflectionUtils.findField(bean.getClass(), "handlerMappings");
				ReflectionUtils.makeAccessible(field);
				return (List<RequestMappingInfoHandlerMapping>) ReflectionUtils.getField(field, bean);
			}
		};
	}
}
