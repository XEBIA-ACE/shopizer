package com.salesmanager.test.shop.integration.documentation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import com.salesmanager.shop.application.ShopApplication;
import com.salesmanager.test.shop.common.ServicesTestSupport;

@SpringBootTest(classes = ShopApplication.class, webEnvironment = WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
public class SwaggerDocumentationIntegrationTest extends ServicesTestSupport {

	@Test
	public void openApiV3DocsAreGenerated() {
		final ResponseEntity<String> response = testRestTemplate.getForEntity("/v3/api-docs", String.class);
		assertThat(response.getStatusCode(), is(HttpStatus.OK));
		assertThat(response.getBody(), containsString("\"openapi\":\"3.0"));
		assertThat(response.getBody(), containsString("Shopizer REST API"));
		assertThat(response.getBody(), containsString("/api/v1/store/{store}"));
		assertThat(response.getBody(), containsString("\"JWT\""));
	}

	@Test
	public void swaggerV2DocsAreStillGenerated() {
		final ResponseEntity<String> response = testRestTemplate.getForEntity("/v2/api-docs", String.class);
		assertThat(response.getStatusCode(), is(HttpStatus.OK));
		assertThat(response.getBody(), containsString("\"swagger\":\"2.0\""));
		assertThat(response.getBody(), containsString("/api/v1/store/{store}"));
	}

	@Test
	public void swaggerUiIsServed() {
		final ResponseEntity<String> response = testRestTemplate.getForEntity("/swagger-ui/index.html", String.class);
		assertThat(response.getStatusCode(), is(HttpStatus.OK));
		assertThat(response.getBody(), containsString("swagger-ui"));
	}
}
