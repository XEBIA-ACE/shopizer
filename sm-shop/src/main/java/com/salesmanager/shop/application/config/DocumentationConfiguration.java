package com.salesmanager.shop.application.config;

import static java.util.Collections.singletonList;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

import springfox.documentation.RequestHandler;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.builders.ResponseBuilder;
import springfox.documentation.oas.annotations.EnableOpenApi;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.Contact;
import springfox.documentation.service.Response;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.service.VendorExtension;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;

@Configuration
@EnableOpenApi
public class DocumentationConfiguration {

	public static final Contact DEFAULT_CONTACT = new Contact("Shopizer", "https://www.shopizer.com", "");

	private static final String HOST = "localhost:8080";

	private static final String HEADER = "header";

	/**
	 * http://localhost:8080/swagger-ui/index.html http://localhost:8080/v3/api-docs
	 * (legacy: http://localhost:8080/v2/api-docs)
	 */

	@Bean
	public Docket api() {

		final List<Response> getMessages = new ArrayList<Response>();
		getMessages.add(new ResponseBuilder().code("500").description("500 message").build());
		getMessages.add(new ResponseBuilder().code("403").description("Forbidden").build());
		getMessages.add(new ResponseBuilder().code("401").description("Unauthorized").build());

		Set<String> produces = new HashSet<>();
		produces.add("application/json");

		Set<String> consumes = new HashSet<>();
		consumes.add("application/json");

		return new Docket(DocumentationType.OAS_30)
				.host(HOST)
				.apiInfo(apiInfo())
				.select()
				.apis(requestHandlers()).build()
				.securitySchemes(Collections.singletonList(new ApiKey("JWT", AUTHORIZATION, HEADER)))
				.securityContexts(singletonList(
					SecurityContext.builder()
						.securityReferences(
							singletonList(SecurityReference.builder()
								.reference("JWT")
								.scopes(new AuthorizationScope[0])
								.build()
							)
						)
						.build())
				)
				.produces(produces).consumes(consumes)
				.globalResponses(HttpMethod.GET, getMessages);

	}

	final Predicate<RequestHandler> requestHandlers() {
		return RequestHandlerSelectors.basePackage("com.salesmanager.shop.store.api.v1")
				.or(RequestHandlerSelectors.basePackage("com.salesmanager.shop.store.api.v2"));
	}

	private ApiInfo apiInfo() {
		return new ApiInfo("Shopizer REST API",
				"API for Shopizer e-commerce. Contains public end points as well as private end points requiring basic authentication and remote authentication based on jwt bearer token. URL patterns containing /private/** use bearer token; those are authorized customer and administrators administration actions.",
				"1.0", "urn:tos", DEFAULT_CONTACT, "Apache 2.0", "http://www.apache.org/licenses/LICENSE-2.0",
				new ArrayList<VendorExtension>());

	}

}
