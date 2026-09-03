/**
 *
 */
package com.salesmanager.shop.store.controller.shoppingCart.facade;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import jakarta.inject.Inject;
import jakarta.persistence.NoResultException;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.business.services.catalog.pricing.PricingService;
import com.salesmanager.core.business.services.catalog.product.ProductService;
import com.salesmanager.core.business.services.catalog.product.attribute.ProductAttributeService;
import com.salesmanager.core.business.services.shoppingcart.ShoppingCartCalculationService;
import com.salesmanager.core.business.services.shoppingcart.ShoppingCartService;
//import com.salesmanager.core.business.utils.ProductPriceUtils;
import com.salesmanager.core.model.catalog.product.Product;
import com.salesmanager.core.model.catalog.product.attribute.ProductAttribute;
import com.salesmanager.core.model.catalog.product.availability.ProductAvailability;
import com.salesmanager.core.model.catalog.product.price.FinalPrice;
import com.salesmanager.core.model.catalog.product.variant.ProductVariant;
import com.salesmanager.core.model.customer.Customer;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.language.Language;
import com.salesmanager.core.model.shoppingcart.ShoppingCart;
import com.salesmanager.shop.constants.Constants;
import com.salesmanager.shop.mapper.cart.ReadableShoppingCartMapper;
import com.salesmanager.shop.model.shoppingcart.CartModificationException;
import com.salesmanager.shop.model.shoppingcart.PersistableShoppingCartItem;
import com.salesmanager.shop.model.shoppingcart.ReadableShoppingCart;
import com.salesmanager.shop.model.shoppingcart.ShoppingCartAttribute;
import com.salesmanager.shop.model.shoppingcart.ShoppingCartData;
import com.salesmanager.shop.model.shoppingcart.ShoppingCartItem;
import com.salesmanager.shop.populator.shoppingCart.ShoppingCartDataPopulator;
import com.salesmanager.shop.store.api.exception.ResourceNotFoundException;
import com.salesmanager.shop.store.api.exception.ServiceRuntimeException;
import com.salesmanager.shop.utils.DateUtil;
import com.salesmanager.shop.utils.ImageFilePath;

/**
 * @author Umesh Awasthi
 * @author Carl Samson
 * @version 3.2.0
 * @since 1.0
 */
@Service(value = "shoppingCartFacade")
public class ShoppingCartFacadeImpl implements ShoppingCartFacade {

	private static final Logger LOG = LoggerFactory.getLogger(ShoppingCartFacadeImpl.class);

	@Inject
	private ShoppingCartService shoppingCartService;

	@Inject
	private ShoppingCartCalculationService shoppingCartCalculationService;


	@Inject
	private ProductService productService;

	@Inject
	private PricingService pricingService;

	@Inject
	private ProductAttributeService productAttributeService;

	@Inject
	@Qualifier("img")
	private ImageFilePath imageUtils;

	@Autowired
	private ReadableShoppingCartMapper readableShoppingCartMapper;

	public void deleteShoppingCart(final Long id, final MerchantStore store) throws Exception {
		ShoppingCart cart = shoppingCartService.getById(id, store);
		if (cart != null) {
			shoppingCartService.deleteCart(cart);
		}
	}

	@Override
	public void deleteShoppingCart(final String code, final MerchantStore store) throws Exception {
		ShoppingCart cart = shoppingCartService.getByCode(code, store);
		if (cart != null) {
			shoppingCartService.deleteCart(cart);
		}
	}

	// @Override
	// REMOVE
	public ShoppingCartData addItemsToShoppingCart(final ShoppingCartData shoppingCartData, final ShoppingCartItem item,
			final MerchantStore store, final Language language, final Customer customer) throws Exception {

		ShoppingCart cartModel = null;

		/**
		 * Sometimes a user logs in and a shopping cart is present in db
		 * (shoppingCartData but ui has no cookie with shopping cart code so the cart
		 * code will have to be added to the item in order to process add to cart
		 * normally
		 */
		if (shoppingCartData != null && StringUtils.isBlank(item.getCode())) {
			item.setCode(shoppingCartData.getCode());
		}

		if (!StringUtils.isBlank(item.getCode())) {
			// get it from the db
			cartModel = getShoppingCartModel(item.getCode(), store);
			if (cartModel == null) {
				cartModel = createCartModel(shoppingCartData.getCode(), store, customer);
			}

		}

		if (cartModel == null) {

			final String shoppingCartCode = StringUtils.isNotBlank(shoppingCartData.getCode())
					? shoppingCartData.getCode()
					: null;
			cartModel = createCartModel(shoppingCartCode, store, customer);

		}
		com.salesmanager.core.model.shoppingcart.ShoppingCartItem shoppingCartItem = createCartItem(cartModel, item,
				store);

		boolean duplicateFound = false;
		if (CollectionUtils.isEmpty(item.getShoppingCartAttributes())) {// increment quantity
			// get duplicate item from the cart
			Set<com.salesmanager.core.model.shoppingcart.ShoppingCartItem> cartModelItems = cartModel.getLineItems();
			for (com.salesmanager.core.model.shoppingcart.ShoppingCartItem cartItem : cartModelItems) {
				if (cartItem.getProduct().getId().longValue() == shoppingCartItem.getProduct().getId().longValue()) {
					if (CollectionUtils.isEmpty(cartItem.getAttributes())) {
						if (!duplicateFound) {
							if (!shoppingCartItem.isProductVirtual()) {
								cartItem.setQuantity(cartItem.getQuantity() + shoppingCartItem.getQuantity());
							}
							duplicateFound = true;
							break;
						}
					}
				}
			}
		}

		if (!duplicateFound) {
			cartModel.getLineItems().add(shoppingCartItem);
		}

		/** Update cart in database with line items **/
		shoppingCartService.saveOrUpdate(cartModel);

		// refresh cart
		cartModel = shoppingCartService.getById(cartModel.getId(), store);

		shoppingCartCalculationService.calculate(cartModel, store, language);

		ShoppingCartDataPopulator shoppingCartDataPopulator = new ShoppingCartDataPopulator();
		shoppingCartDataPopulator.setShoppingCartCalculationService(shoppingCartCalculationService);
		shoppingCartDataPopulator.setPricingService(pricingService);
		shoppingCartDataPopulator.setimageUtils(imageUtils);

		return shoppingCartDataPopulator.populate(cartModel, store, language);
	}

	private com.salesmanager.core.model.shoppingcart.ShoppingCartItem createCartItem(final ShoppingCart cartModel,
			final ShoppingCartItem shoppingCartItem, final MerchantStore store) throws Exception {

		Product product = productService.getBySku(shoppingCartItem.getSku(), store, store.getDefaultLanguage());

		if (product == null) {
			throw new Exception("Item with sku " + shoppingCartItem.getSku() + " does not exist");
		}

		if (product.getMerchantStore().getId().intValue() != store.getId().intValue()) {
			throw new Exception(
					"Item with sku " + shoppingCartItem.getSku() + " does not belong to merchant " + store.getId());
		}

		/**
		 * Check if product quantity is 0 Check if product is available Check if date
		 * available <= now
		 */

		Set<ProductAvailability> availabilities = product.getAvailabilities();
		if (availabilities == null) {

			throw new Exception("Item with id " + product.getId() + " is not properly configured");

		}

		for (ProductAvailability availability : availabilities) {
			if (availability.getProductQuantity() == null || availability.getProductQuantity().intValue() == 0) {
				throw new Exception("Item with id " + product.getId() + " is not available");
			}
		}

		if (!product.isAvailable()) {
			throw new Exception("Item with id " + product.getId() + " is not available");
		}

		if (!DateUtil.dateBeforeEqualsDate(product.getDateAvailable(), new Date())) {
			throw new Exception("Item with id " + product.getId() + " is not available");
		}

		com.salesmanager.core.model.shoppingcart.ShoppingCartItem item = shoppingCartService
				.populateShoppingCartItem(product, store);

		item.setQuantity(shoppingCartItem.getQuantity());
		item.setShoppingCart(cartModel);

		// attributes
		List<ShoppingCartAttribute> cartAttributes = shoppingCartItem.getShoppingCartAttributes();
		if (!CollectionUtils.isEmpty(cartAttributes)) {
			for (ShoppingCartAttribute attribute : cartAttributes) {
				ProductAttribute productAttribute = productAttributeService.getById(attribute.getAttributeId());
				if (productAttribute != null
						&& productAttribute.getProduct().getId().longValue() == product.getId().longValue()) {
					com.salesmanager.core.model.shoppingcart.ShoppingCartAttributeItem attributeItem = new com.salesmanager.core.model.shoppingcart.ShoppingCartAttributeItem(
							item, productAttribute);

					item.addAttributes(attributeItem);
				}
			}
		}
		return item;

	}

	// KEEP -- ENTRY
	private com.salesmanager.core.model.shoppingcart.ShoppingCartItem createCartItem(ShoppingCart cartModel,
			PersistableShoppingCartItem shoppingCartItem, MerchantStore store) throws Exception {

		// USE Product sku
		Product product = null;

		product = productService.getBySku(shoppingCartItem.getProduct(), store, store.getDefaultLanguage());// todo use
																											// language
																											// from api
																											// request
		if (product == null) {
			thro