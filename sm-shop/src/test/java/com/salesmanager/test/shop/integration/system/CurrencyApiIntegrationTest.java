package com.salesmanager.test.shop.integration.system;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.salesmanager.shop.application.ShopApplication;
import com.salesmanager.test.shop.common.ServicesTestSupport;

@SpringBootTest(classes = ShopApplication.class, webEnvironment = WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
public class CurrencyApiIntegrationTest extends ServicesTestSupport {

  private static final String PUBLIC_CURRENCIES = "/api/v1/currency";
  private static final String ADMIN_CURRENCIES = "/api/v1/private/currency";

  @Autowired
  private TestRestTemplate testRestTemplate;

  private final ObjectMapper mapper = new ObjectMapper();

  @Test
  public void adminTogglesCurrencyAndStorefrontReflectsIt() throws Exception {
    List<String> before = publicCodes();
    assertTrue(before.size() > 1);
    String code = before.get(before.size() - 1);

    ResponseEntity<String> disabled = toggle(code, false, getHeader());
    assertEquals(HttpStatus.OK, disabled.getStatusCode());
    assertFalse(mapper.readTree(disabled.getBody()).get("supported").asBoolean());
    assertFalse(publicCodes().contains(code));

    ResponseEntity<String> enabled = toggle(code, true, getHeader());
    assertEquals(HttpStatus.OK, enabled.getStatusCode());
    assertTrue(publicCodes().contains(code));
  }

  @Test
  public void adminListsAllCurrencies() throws Exception {
    ResponseEntity<String> response =
        testRestTemplate.exchange(ADMIN_CURRENCIES, HttpMethod.GET, new HttpEntity<>(getHeader()), String.class);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    JsonNode body = mapper.readTree(response.getBody());
    assertTrue(body.isArray());
    assertNotNull(body.get(0).get("supported"));
  }

  @Test
  public void unauthenticatedUserCannotToggleCurrency() throws Exception {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    ResponseEntity<String> response = toggle("USD", false, headers);
    assertTrue(response.getStatusCode() == HttpStatus.UNAUTHORIZED
        || response.getStatusCode() == HttpStatus.FORBIDDEN);
  }

  @Test
  public void unknownCurrencyReturnsNotFound() throws Exception {
    ResponseEntity<String> response = toggle("ZZZ", false, getHeader());
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  private ResponseEntity<String> toggle(String code, boolean supported, HttpHeaders headers) {
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<String> entity = new HttpEntity<>("{\"supported\":" + supported + "}", headers);
    return testRestTemplate.exchange(ADMIN_CURRENCIES + "/" + code + "/supported", HttpMethod.PUT, entity, String.class);
  }

  private List<String> publicCodes() throws Exception {
    ResponseEntity<String> response = testRestTemplate.getForEntity(PUBLIC_CURRENCIES, String.class);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    JsonNode body = mapper.readTree(response.getBody());
    return Arrays.stream(mapper.convertValue(body, JsonNode[].class))
        .map(n -> n.get("code").asText()).collect(Collectors.toList());
  }
}
