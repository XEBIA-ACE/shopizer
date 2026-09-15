package com.salesmanager.shop.application.config;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class DocumentationConfiguration {

	public static final Contact DEFAULT_CONTACT = new Contact().name("Shopizer").url("https://www.shopizer.com");

	private static final String JWT = "JWT";

	/**
	 * http://localhost:8080/swagger-ui.html http://localhost:8080/v3/api-docs
	 */
	@Bean
	public OpenAPI api() {
		return new OpenAPI()
				.info(new Info().title("Shopizer REST API")
						.description(
								"API for Shopizer e-commerce. Contains public end points as well as private end points requiring basic authentication and remote authentication based on jwt bearer token. URL patterns containing /private/** use bearer token; those are authorized customer and administrators administration actions.")
						.version("1.0").termsOfService("urn:tos").contact(DEFAULT_CONTACT)
						.license(new License().name("Apache 2.0").url("http://www.apache.org/licenses/LICENSE-2.0")))
				.components(new Components().addSecuritySchemes(JWT,
						new SecurityScheme().type(SecurityScheme.Type.APIKEY).in(SecurityScheme.In.HEADER)
								.name(AUTHORIZATION)))
				.addSecurityItem(new SecurityRequirement().addList(JWT));
	}

	@Bean
	public GroupedOpenApi shopizerApi() {
		return GroupedOpenApi.builder().group("shopizer")
				.packagesToScan("com.salesmanager.shop.store.api.v1", "com.salesmanager.shop.store.api.v2")
				.addOpenApiCustomizer(globalGetResponses()).build();
	}

	private OpenApiCustomizer globalGetResponses() {
		return openApi -> openApi.getPaths().values().stream().map(pathItem -> pathItem.getGet())
				.filter(get -> get != null).forEach(get -> {
					get.getResponses().addApiResponse("401", new ApiResponse().description("Unauthorized"));
					get.getResponses().addApiResponse("403", new ApiResponse().description("Forbidden"));
					get.getResponses().addApiResponse("500", new ApiResponse().description("500 message"));
				});
	}

}
