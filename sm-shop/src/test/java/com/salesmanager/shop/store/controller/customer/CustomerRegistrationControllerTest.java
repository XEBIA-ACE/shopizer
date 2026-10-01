package com.salesmanager.shop.store.controller.customer;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.country.Country;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.shop.model.customer.CustomerRegistrationRequest;
import com.salesmanager.shop.model.customer.PersistableCustomer;
import com.salesmanager.shop.model.customer.UserAlreadyExistException;
import com.salesmanager.shop.store.controller.customer.facade.CustomerFacade;
import com.salesmanager.shop.store.security.validation.PasswordComplexityValidator;

@WebMvcTest(controllers = CustomerRegistrationController.class)
@ContextConfiguration(classes = { CustomerRegistrationController.class, PasswordComplexityValidator.class,
		CustomerRegistrationControllerTest.StoreAndLanguageResolvers.class })
@AutoConfigureMockMvc(addFilters = false)
class CustomerRegistrationControllerTest {

	private static final String VALID_PASSWORD = "Str0ng!Passw0rd";
	private static final MerchantStore STORE = new MerchantStore();
	private static final Language LANGUAGE = new Language("en");

	static {
		Country country = new Country("CA");
		STORE.setCode("DEFAULT");
		STORE.setCountry(country);
	}

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private CustomerFacade customerFacade;

	@BeforeEach
	void noExistingCustomer() throws Exception {
		when(customerFacade.checkIfUserExists(any(), any())).thenReturn(false);
	}

	@Test
	void registrationPageRendersFormWithSharedRules() throws Exception {
		mockMvc.perform(get("/register"))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
				.andExpect(content().string(containsString("id=\"registration-form\"")))
				.andExpect(content().string(containsString("type=\"email\" id=\"emailAddress\"")))
				.andExpect(content().string(containsString("type=\"password\" id=\"password\"")))
				.andExpect(content().string(containsString("id=\"email-error\"")))
				.andExpect(content().string(containsString("id=\"password-error\"")))
				.andExpect(content().string(containsString("id=\"form-confirmation\"")))
				.andExpect(content().string(containsString("id=\"form-error\"")))
				.andExpect(content().string(containsString("type=\"submit\"")))
				.andExpect(content().string(containsString("\"minLength\":12")))
				.andExpect(content().string(containsString(PasswordComplexityValidator.UPPERCASE_MESSAGE)))
				.andExpect(content().string(not(containsString(CustomerRegistrationController.RULES_PLACEHOLDER))))
				.andExpect(content().string(not(containsString("<>"))));
	}

	@Test
	void validRegistrationCreatesCustomerAndConfirms() throws Exception {
		MvcResult result = mockMvc.perform(post("/register").contentType(MediaType.APPLICATION_JSON)
				.content(body("new.customer@example.com", VALID_PASSWORD)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value(CustomerRegistrationController.SUCCESS_MESSAGE))
				.andReturn();

		assertPasswordNotExposed(result);
		ArgumentCaptor<PersistableCustomer> captor = ArgumentCaptor.forClass(PersistableCustomer.class);
		verify(customerFacade).registerCustomer(captor.capture(), eq(STORE), eq(LANGUAGE));
		PersistableCustomer customer = captor.getValue();
		assertEquals("new.customer@example.com", customer.getEmailAddress());
		assertEquals("new.customer@example.com", customer.getUserName());
		assertEquals(VALID_PASSWORD, customer.getPassword());
		assertEquals("CA", customer.getBilling().getCountry());
		assertEquals("new.customer", customer.getBilling().getFirstName());
	}

	@Test
	void invalidEmailReturnsFieldError() throws Exception {
		MvcResult result = mockMvc.perform(post("/register").contentType(MediaType.APPLICATION_JSON)
				.content(body("notanemail", VALID_PASSWORD)))
				.andExpect(status().isUnprocessableEntity())
				.andExpect(jsonPath("$.fieldErrors.emailAddress[0]").value(CustomerRegistrationRequest.INVALID_EMAIL_MESSAGE))
				.andExpect(jsonPath("$.fieldErrors.emailAddress.length()").value(1))
				.andExpect(jsonPath("$.fieldErrors.password").doesNotExist())
				.andReturn();

		assertPasswordNotExposed(result);
		verify(customerFacade, never()).registerCustomer(any(), any(), any());
	}

	@Test
	void emailWithoutDomainSuffixIsRejected() throws Exception {
		mockMvc.perform(post("/register").contentType(MediaType.APPLICATION_JSON)
				.content(body("user@localhost", VALID_PASSWORD)))
				.andExpect(status().isUnprocessableEntity())
				.andExpect(jsonPath("$.fieldErrors.emailAddress[0]").value(CustomerRegistrationRequest.INVALID_EMAIL_MESSAGE));
	}

	@Test
	void emailLongerThanColumnIsRejected() throws Exception {
		String local = new String(new char[CustomerRegistrationRequest.EMAIL_MAX_LENGTH]).replace('\0', 'a');
		mockMvc.perform(post("/register").contentType(MediaType.APPLICATION_JSON)
				.content(body(local + "@example.com", VALID_PASSWORD)))
				.andExpect(status().isUnprocessableEntity())
				.andExpect(jsonPath("$.fieldErrors.emailAddress[0]").value(CustomerRegistrationRequest.INVALID_EMAIL_MESSAGE));
	}

	@Test
	void blankEmailReportsSingleMessage() throws Exception {
		mockMvc.perform(post("/register").contentType(MediaType.APPLICATION_JSON).content(body("", VALID_PASSWORD)))
				.andExpect(status().isUnprocessableEntity())
				.andExpect(jsonPath("$.fieldErrors.emailAddress.length()").value(1));
	}

	@Test
	void weakPasswordListsEveryViolatedRule() throws Exception {
		String weak = "short";
		MvcResult result = mockMvc.perform(post("/register").contentType(MediaType.APPLICATION_JSON)
				.content(body("new.customer@example.com", weak)))
				.andExpect(status().isUnprocessableEntity())
				.andExpect(jsonPath("$.fieldErrors.password[0]").value(PasswordComplexityValidator.MIN_LENGTH_MESSAGE))
				.andExpect(jsonPath("$.fieldErrors.password[1]").value(PasswordComplexityValidator.UPPERCASE_MESSAGE))
				.andExpect(jsonPath("$.fieldErrors.password[2]").value(PasswordComplexityValidator.DIGIT_MESSAGE))
				.andExpect(jsonPath("$.fieldErrors.password[3]").value(PasswordComplexityValidator.SPECIAL_CHARACTER_MESSAGE))
				.andExpect(jsonPath("$.fieldErrors.password.length()").value(4))
				.andReturn();

		assertEquals(-1, result.getResponse().getContentAsString().indexOf("\"" + weak + "\""));
		verify(customerFacade, never()).registerCustomer(any(), any(), any());
	}

	@Test
	void duplicateEmailReturnsGenericError() throws Exception {
		when(customerFacade.checkIfUserExists(eq("taken@example.com"), any())).thenReturn(true);

		MvcResult result = mockMvc.perform(post("/register").contentType(MediaType.APPLICATION_JSON)
				.content(body("taken@example.com", VALID_PASSWORD)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value(CustomerRegistrationController.GENERIC_ERROR_MESSAGE))
				.andReturn();

		assertPasswordNotExposed(result);
		assertEquals(-1, result.getResponse().getContentAsString().indexOf("taken@example.com"));
		verify(customerFacade, never()).registerCustomer(any(), any(), any());
	}

	@Test
	void registrationFailureReturnsSameGenericError() throws Exception {
		doThrow(new UserAlreadyExistException("User already exist")).when(customerFacade).registerCustomer(any(), any(), any());

		MvcResult result = mockMvc.perform(post("/register").contentType(MediaType.APPLICATION_JSON)
				.content(body("race@example.com", VALID_PASSWORD)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value(CustomerRegistrationController.GENERIC_ERROR_MESSAGE))
				.andReturn();

		assertPasswordNotExposed(result);
	}

	private static void assertPasswordNotExposed(MvcResult result) throws Exception {
		assertEquals(-1, result.getResponse().getContentAsString().indexOf(VALID_PASSWORD));
		for (String header : result.getResponse().getHeaderNames()) {
			for (String value : result.getResponse().getHeaders(header)) {
				assertEquals(-1, value.indexOf(VALID_PASSWORD));
			}
		}
	}

	private static String body(String email, String password) {
		return "{\"emailAddress\":\"" + email + "\",\"password\":\"" + password + "\"}";
	}

	@TestConfiguration
	static class StoreAndLanguageResolvers implements WebMvcConfigurer {
		@Override
		public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
			resolvers.add(new FixedValueResolver(MerchantStore.class, STORE));
			resolvers.add(new FixedValueResolver(Language.class, LANGUAGE));
		}
	}

	private static final class FixedValueResolver implements HandlerMethodArgumentResolver {
		private final Class<?> type;
		private final Object value;

		FixedValueResolver(Class<?> type, Object value) {
			this.type = type;
			this.value = value;
		}

		@Override
		public boolean supportsParameter(MethodParameter parameter) {
			return parameter.getParameterType().equals(type);
		}

		@Override
		public Object resolveArgument(MethodParameter parameter,
				ModelAndViewContainer mavContainer,
				NativeWebRequest webRequest,
				WebDataBinderFactory binderFactory) {
			return value;
		}
	}
}
