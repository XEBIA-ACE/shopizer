package com.salesmanager.shop.store.api.v1.store;

import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.shop.store.controller.store.facade.StoreCurrencyFacade;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link StoreCurrencyApi}.
 *
 * Verifies HTTP status codes and delegation to {@link StoreCurrencyFacade}.
 */
@RunWith(MockitoJUnitRunner.class)
public class StoreCurrencyApiTest {

    private static final String STORE_CODE = "DEFAULT";
    private static final String EUR = "EUR";

    @Mock
    private StoreCurrencyFacade storeCurrencyFacade;

    @InjectMocks
    private StoreCurrencyApi api;

    private MockHttpServletRequest request;

    @Before
    public void setUp() {
        request = new MockHttpServletRequest();
    }

    // -----------------------------------------------------------------------
    // GET /store/{storeCode}/currencies
    // -----------------------------------------------------------------------

    @Test
    public void getSupportedCurrencies_shouldReturn200WithList() throws Exception {
        List<String> codes = Arrays.asList("EUR", "GBP");
        when(storeCurrencyFacade.getSupportedCurrencies(STORE_CODE)).thenReturn(codes);

        ResponseEntity<List<String>> response = api.getSupportedCurrencies(STORE_CODE, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(codes, response.getBody());
    }

    @Test
    public void getSupportedCurrencies_shouldReturn200WithEmptyList() throws Exception {
        when(storeCurrencyFacade.getSupportedCurrencies(STORE_CODE))
                .thenReturn(Collections.emptyList());

        ResponseEntity<List<String>> response = api.getSupportedCurrencies(STORE_CODE, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody().size());
    }

    @Test
    public void getSupportedCurrencies_shouldReturn500OnUnexpectedError() throws Exception {
        when(storeCurrencyFacade.getSupportedCurrencies(STORE_CODE))
                .thenThrow(new RuntimeException("DB error"));

        ResponseEntity<List<String>> response = api.getSupportedCurrencies(STORE_CODE, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    // -----------------------------------------------------------------------
    // POST /store/{storeCode}/currencies/{currencyCode}
    // -----------------------------------------------------------------------

    @Test
    public void addCurrency_shouldReturn201OnSuccess() throws Exception {
        doNothing().when(storeCurrencyFacade).addCurrency(STORE_CODE, EUR);

        ResponseEntity<Void> response = api.addCurrency(STORE_CODE, EUR, request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(storeCurrencyFacade).addCurrency(STORE_CODE, EUR);
    }

    @Test
    public void addCurrency_shouldReturn400OnServiceException() throws Exception {
        doThrow(new ServiceException("Unknown currency"))
                .when(storeCurrencyFacade).addCurrency(STORE_CODE, "XYZ");

        ResponseEntity<Void> response = api.addCurrency(STORE_CODE, "XYZ", request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void addCurrency_shouldReturn500OnUnexpectedError() throws Exception {
        doThrow(new RuntimeException("DB error"))
                .when(storeCurrencyFacade).addCurrency(STORE_CODE, EUR);

        ResponseEntity<Void> response = api.addCurrency(STORE_CODE, EUR, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void addCurrency_shouldUppercaseCurrencyCode() throws Exception {
        doNothing().when(storeCurrencyFacade).addCurrency(STORE_CODE, "EUR");

        api.addCurrency(STORE_CODE, "eur", request);

        verify(storeCurrencyFacade).addCurrency(STORE_CODE, "EUR");
    }

    // -----------------------------------------------------------------------
    // DELETE /store/{storeCode}/currencies/{currencyCode}
    // -----------------------------------------------------------------------

    @Test
    public void removeCurrency_shouldReturn204OnSuccess() throws Exception {
        doNothing().when(storeCurrencyFacade).removeCurrency(STORE_CODE, EUR);

        ResponseEntity<Void> response = api.removeCurrency(STORE_CODE, EUR, request);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(storeCurrencyFacade).removeCurrency(STORE_CODE, EUR);
    }

    @Test
    public void removeCurrency_shouldReturn400OnServiceException() throws Exception {
        doThrow(new ServiceException("Cannot remove primary currency"))
                .when(storeCurrencyFacade).removeCurrency(STORE_CODE, "USD");

        ResponseEntity<Void> response = api.removeCurrency(STORE_CODE, "USD", request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void removeCurrency_shouldReturn500OnUnexpectedError() throws Exception {
        doThrow(new RuntimeException("DB error"))
                .when(storeCurrencyFacade).removeCurrency(STORE_CODE, EUR);

        ResponseEntity<Void> response = api.removeCurrency(STORE_CODE, EUR, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void removeCurrency_shouldUppercaseCurrencyCode() throws Exception {
        doNothing().when(storeCurrencyFacade).removeCurrency(STORE_CODE, "EUR");

        api.removeCurrency(STORE_CODE, "eur", request);

        verify(storeCurrencyFacade).removeCurrency(STORE_CODE, "EUR");
    }
}
