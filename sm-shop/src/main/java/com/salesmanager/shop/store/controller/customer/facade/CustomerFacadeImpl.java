/**
 *
 */
package com.salesmanager.shop.store.controller.customer.facade;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import jakarta.inject.Inject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.jgroups.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.salesmanager.core.business.exception.ConversionException;
import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.business.modules.email.Email;
import com.salesmanager.core.business.services.customer.CustomerService;
import com.salesmanager.core.business.services.customer.optin.CustomerOptinService;
import com.salesmanager.core.business.services.customer.review.CustomerReviewService;
import com.salesmanager.core.business.services.reference.country.CountryService;
import com.salesmanager.core.business.services.reference.language.LanguageService;
import com.salesmanager.core.business.services.reference.zone.ZoneService;
import com.salesmanager.core.business.services.shoppingcart.ShoppingCartService;
import com.salesmanager.core.business.services.system.EmailService;
import com.salesmanager.core.business.services.system.optin.OptinService;
import com.salesmanager.core.business.services.user.GroupService;
import com.salesmanager.core.business.services.user.PermissionService;
import com.salesmanager.core.business.utils.CoreConfiguration;
import com.salesmanager.core.model.customer.Customer;
import com.salesmanager.core.model.customer.CustomerCriteria;
import com.salesmanager.core.model.customer.CustomerList;
import com.salesmanager.core.model.customer.review.CustomerReview;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.country.Country;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.core.model.reference.zone.Zone;
import com.salesmanager.core.model.shoppingcart.ShoppingCart;
import com.salesmanager.core.model.system.optin.CustomerOptin;
import com.salesmanager.core.model.system.optin.Optin;
import com.salesmanager.core.model.system.optin.OptinType;
import com.salesmanager.core.model.user.Group;
import com.salesmanager.core.model.user.GroupType;
import com.salesmanager.core.model.user.Permission;
import com.salesmanager.shop.constants.Constants;
import com.salesmanager.shop.constants.EmailConstants;
import com.salesmanager.shop.model.customer.CustomerEntity;
import com.salesmanager.shop.model.customer.PersistableCustomer;
import com.salesmanager.shop.model.customer.PersistableCustomerReview;
import com.salesmanager.shop.model.customer.ReadableCustomer;
import com.salesmanager.shop.model.customer.ReadableCustomerReview;
import com.salesmanager.shop.model.customer.UserAlreadyExistException;
import com.salesmanager.shop.model.customer.address.Address;
import com.salesmanager.shop.model.customer.optin.PersistableCustomerOptin;
import com.salesmanager.shop.populator.customer.CustomerBillingAddressPopulator;
import com.salesmanager.shop.populator.customer.CustomerDeliveryAddressPopulator;
import com.salesmanager.shop.populator.customer.CustomerEntityPopulator;
import com.salesmanager.shop.populator.customer.CustomerPopulator;
import com.salesmanager.shop.populator.customer.PersistableCustomerBillingAddressPopulator;
import com.salesmanager.shop.populator.customer.PersistableCustomerReviewPopulator;
import com.salesmanager.shop.populator.customer.PersistableCustomerShippingAddressPopulator;
import com.salesmanager.shop.populator.customer.ReadableCustomerList;
import com.salesmanager.shop.populator.customer.ReadableCustomerPopulator;
import com.salesmanager.shop.populator.customer.ReadableCustomerReviewPopulator;
import com.salesmanager.shop.store.api.exception.ConversionRuntimeException;
import com.salesmanager.shop.store.api.exception.ResourceNotFoundException;
import com.salesmanager.shop.store.api.exception.ServiceRuntimeException;
import com.salesmanager.shop.utils.EmailTemplatesUtils;
import com.salesmanager.shop.utils.EmailUtils;
import com.salesmanager.shop.utils.ImageFilePath;
import com.salesmanager.shop.utils.LabelUtils;
import com.salesmanager.shop.utils.LocaleUtils;


/**
 * Customer Facade work as an abstraction layer between Controller and Service layer. It work as an
 * entry point to service layer.
 * 
 * @author Umesh Awasthi
 * @version 2.2.1, 2.8.0
 * @modified Carl Samson
 *
 */

@Service("customerFacade")
public class CustomerFacadeImpl implements CustomerFacade {

  private static final Logger LOG = LoggerFactory.getLogger(CustomerFacadeImpl.class);
  public final static int USERNAME_LENGTH = 6;

  private final static String RESET_PASSWORD_TPL = "email_template_password_reset_customer.ftl";

  public final static String ROLE_PREFIX = "ROLE_";// Spring Security 4


  @Inject
  private CustomerService customerService;

  @Inject
  private OptinService optinService;

  @Inject
  private CustomerOptinService customerOptinService;

  @Inject
  private ShoppingCartService shoppingCartService;

  @Inject
  private LanguageService languageService;

  @Inject
  private LabelUtils messages;

  @Inject
  private CountryService countryService;

  @Inject
  private GroupService groupService;

  @Inject
  private PermissionService permissionService;

  @Inject
  private ZoneService zoneService;

  @Inject
  private PasswordEncoder passwordEncoder;

  @Inject
  private EmailService emailService;

  @Inject
  private EmailTemplatesUtils emailTemplatesUtils;

  @Inject
  private AuthenticationManager customerAuthenticationManager;

  @Inject
  private CustomerReviewService customerReviewService;

  @Inject
  private CoreConfiguration coreConfiguration;
  
  @Autowired
  private CustomerPopulator customerPopulator;

  @Inject
  private EmailUtils emailUtils;

  @Inject
  @Qualifier("img")
  private ImageFilePath imageUtils;

  /**
   * Method used to fetch customer based on the username and storecode. Customer username is unique
   * to each store.
   *
   * @param userName
   * @param store
   * @throws ConversionException
   */
  @Override
  public CustomerEntity getCustomerDataByUserName(final String userName, final MerchantStore store,
      final Language language) throws Exception {
    LOG.info("Fetching customer with userName" + userName);
    Customer customer = customerService.getByNick(userName);

    if (customer != null) {
      LOG.info("Found customer, converting to CustomerEntity");
      try {
        CustomerEntityPopulator customerEntityPopulator = new CustomerEntityPopulator();
        return customerEntityPopulator.populate(customer, store, language); // store, language

      } catch (ConversionException ex) {
        LOG.error("Error while converting Customer to CustomerEntity", ex);
        throw new Exception(ex);
      }
    }

    return null;

  }


  /*
   * (non-Javadoc)
   * 
   * @see com.salesmanager.web.shop.controller.customer.facade#mergeCart(final Customer
   * customerModel, final String sessionShoppingCartId ,final MerchantStore store,final Language
   * language)
   */
  @Override
  public ShoppingCart mergeCart(final Customer customerModel, final String sessionShoppingCartId,
      final MerchantStore store, final Language language) throws Exception {

    LOG.debug("Starting merge cart process");
    if (customerModel != null) {
      ShoppingCart customerCart = shoppingCartService.getShoppingCart(customerModel, store);
      if (StringUtils.isNotBlank(sessionShoppingCartId)) {
        ShoppingCart sessionShoppingCart =
            shoppingCartService.getByCode(sessionShoppingCartId, store);
        if (sessionShoppingCart != null) {
          if (customerCart == null) {
            if (sessionShoppingCart.getCustomerId() == null) {// saved shopping cart does not belong
                                                              // to a customer
              LOG.debug("Not able to find any shoppingCart with current customer");
              // give it to the customer
              sessionShoppingCart.setCustomerId(customerModel.getId());
              shoppingCartService.saveOrUpdate(sessionShoppingCart);
              customerCart = shoppingCartService.getById(sessionShoppingCart.getId(), store);
              return customerCart;
              // return populateShoppingCartData(customerCart,store,language);
            } else {
              return null;
            }
          } else {
            if (sessionShoppingCart.getCustomerId() == null) {// saved shopping cart does not belong
                                                              // to a customer
              // assign it to logged in user
              LOG.debug("Customer shopping cart as well session cart is available, merging carts");
              customerCart =
                  shoppingCartService.mergeShoppingCarts(customerCart, sessionShoppingCart, store);
              customerCart = shoppingCartService.getById(customerCart.getId(), store);
              return customerCart;
              // return popul