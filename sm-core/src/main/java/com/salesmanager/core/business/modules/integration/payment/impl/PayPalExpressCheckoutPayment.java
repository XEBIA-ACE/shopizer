package com.salesmanager.core.business.modules.integration.payment.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.salesmanager.core.business.constants.Constants;
import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.business.services.catalog.pricing.PricingService;
import com.salesmanager.core.business.utils.CoreConfiguration;
import com.salesmanager.core.model.customer.Customer;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.order.Order;
import com.salesmanager.core.model.order.OrderTotal;
import com.salesmanager.core.model.order.OrderTotalSummary;
import com.salesmanager.core.model.payments.Payment;
import com.salesmanager.core.model.payments.PaymentType;
import com.salesmanager.core.model.payments.Transaction;
import com.salesmanager.core.model.payments.TransactionType;
import com.salesmanager.core.model.shoppingcart.ShoppingCartItem;
import com.salesmanager.core.model.system.IntegrationConfiguration;
import com.salesmanager.core.model.system.IntegrationModule;
import com.salesmanager.core.modules.integration.IntegrationException;
import com.salesmanager.core.modules.integration.payment.model.PaymentModule;

import urn.ebay.api.PayPalAPI.DoCaptureReq;
import urn.ebay.api.PayPalAPI.DoCaptureRequestType;
import urn.ebay.api.PayPalAPI.DoCaptureResponseType;
import urn.ebay.api.PayPalAPI.DoExpressCheckoutPaymentReq;
import urn.ebay.api.PayPalAPI.DoExpressCheckoutPaymentRequestType;
import urn.ebay.api.PayPalAPI.DoExpressCheckoutPaymentResponseType;
import urn.ebay.api.PayPalAPI.GetExpressCheckoutDetailsReq;
import urn.ebay.api.PayPalAPI.GetExpressCheckoutDetailsRequestType;
import urn.ebay.api.PayPalAPI.GetExpressCheckoutDetailsResponseType;
import urn.ebay.api.PayPalAPI.PayPalAPIInterfaceServiceService;
import urn.ebay.api.PayPalAPI.RefundTransactionReq;
import urn.ebay.api.PayPalAPI.RefundTransactionRequestType;
import urn.ebay.api.PayPalAPI.RefundTransactionResponseType;
import urn.ebay.api.PayPalAPI.SetExpressCheckoutReq;
import urn.ebay.api.PayPalAPI.SetExpressCheckoutRequestType;
import urn.ebay.api.PayPalAPI.SetExpressCheckoutResponseType;
import urn.ebay.apis.CoreComponentTypes.BasicAmountType;
import urn.ebay.apis.eBLBaseComponents.CompleteCodeType;
import urn.ebay.apis.eBLBaseComponents.DoExpressCheckoutPaymentRequestDetailsType;
import urn.ebay.apis.eBLBaseComponents.PaymentDetailsItemType;
import urn.ebay.apis.eBLBaseComponents.PaymentDetailsType;
import urn.ebay.apis.eBLBaseComponents.PaymentInfoType;
import urn.ebay.apis.eBLBaseComponents.RefundType;
import urn.ebay.apis.eBLBaseComponents.SetExpressCheckoutRequestDetailsType;

public class PayPalExpressCheckoutPayment implements PaymentModule {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PayPalExpressCheckoutPayment.class);
	
	
	@Inject
	private PricingService pricingService;
	
	@Inject
	private CoreConfiguration coreConfiguration;

	@Override
	public void validateModuleConfiguration(
			IntegrationConfiguration integrationConfiguration,
			MerchantStore store) throws IntegrationException {
		
		
		List<String> errorFields = null;
		
		//validate integrationKeys['account']
		Map<String,String> keys = integrationConfiguration.getIntegrationKeys();
		if(keys==null || StringUtils.isBlank(keys.get("api"))) {
			errorFields = new ArrayList<String>();
			errorFields.add("api");
		}
		
		if(keys==null || StringUtils.isBlank(keys.get("username"))) {
			if(errorFields==null) {
				errorFields = new ArrayList<String>();
			}
			errorFields.add("username");
		}
		
		if(keys==null || StringUtils.isBlank(keys.get("signature"))) {
			if(errorFields==null) {
				errorFields = new ArrayList<String>();
			}
			errorFields.add("signature");
		}
		

		if(errorFields!=null) {
			IntegrationException ex = new IntegrationException(IntegrationException.ERROR_VALIDATION_SAVE);
			ex.setErrorFields(errorFields);
			throw ex;
			
		}
		
		
	}

	@Override
	public Transaction initTransaction(MerchantStore store, Customer customer,
			BigDecimal amount, Payment payment,
			IntegrationConfiguration configuration, IntegrationModule module)
			throws IntegrationException {
		
			throw new IntegrationException("Not imlemented");
	}

	@Override
	public Transaction authorize(MerchantStore store, Customer customer,
			List<ShoppingCartItem> items, BigDecimal amount, Payment payment,
			IntegrationConfiguration configuration, IntegrationModule module)
			throws IntegrationException {

		
		com.salesmanager.core.model.payments.PaypalPayment paypalPayment = (com.salesmanager.core.model.payments.PaypalPayment)payment;
		Validate.notNull(paypalPayment.getPaymentToken(), "A paypal payment token is required to process this transaction");
		
		return processTransaction(store, customer, items, amount, paypalPayment, configuration, module);
		
		
	}

/*	@Override
	public Transaction capture(MerchantStore store, Customer customer,
			List<ShoppingCartItem> items, BigDecimal amount, Payment payment, Transaction transaction,
			IntegrationConfiguration configuration, IntegrationModule module)
			throws IntegrationException {
		
		com.salesmanager.core.business.payments.model.PaypalPayment paypalPayment = (com.salesmanager.core.business.payments.model.PaypalPayment)payment;
		Validate.notNull(paypalPayment.getPaymentToken(), "A paypal payment token is required to process this transaction");
		
		return processTransaction(store, customer, items, amount, paypalPayment, configuration, module);
		
	}*/
	
	public Transaction initPaypalTransaction(MerchantStore store,
			List<ShoppingCartItem> items, OrderTotalSummary summary, Payment payment,
			IntegrationConfiguration configuration, IntegrationModule module)
			throws IntegrationException {
		
			Validate.notNull(configuration, "Configuration must not be null");
			Validate.notNull(payment, "Payment must not be null");
			Validate.notNull(summary, "OrderTotalSummary must not be null");
		

		try {
			
			
			PaymentDetailsType paymentDetails = new PaymentDetailsType();
			if(configuration.getIntegrationKeys().get("transaction").equalsIgnoreCase(TransactionType.AUTHORIZECAPTURE.name())) {
				paymentDetails.setPaymentAction(urn.ebay.apis.eBLBaseComponents.PaymentActionCodeType.SALE);
			} else {
				paymentDetails.setPaymentAction(urn.ebay.apis.eBLBaseComponents.PaymentActionCodeType.AUTHORIZATION);
			}
			

			List<PaymentDetailsItemType> lineItems = new ArrayList<PaymentDetailsItemType>();
			
			for(ShoppingCartItem cartItem : items) {
			
				PaymentDetailsItemType item = new PaymentDetailsItemType();
				BasicAmountType amt = new BasicAmountType();
				amt.setCurrencyID(urn.ebay.apis.eBLBaseComponents.CurrencyCodeType.fromValue(payment.getCurrency().getCode()));
				amt.setValue(pricingService.getStringAmount(cartItem.getFinalPrice().getFinalPrice(), store));
				//itemsTotal = itemsTotal.add(cartItem.getSubTotal());
				int itemQuantity = cartItem.getQuantity();
				item.setQuantity(itemQuantity);
				item.setName(cartItem.getProduct().getProductDescription().getName());
				item.setAmount(amt);
				//System.out.println(pricingService.getStringAmount(cartItem.getSubTotal(), store));
				lineItems.add(item);
			
			}
			
			
			List<OrderTotal> orderTotals = summary.getTotals();
			BigDecimal tax = null;
			for(OrderTotal total : orderTotals) {
				
				if(total.getModule().equals(Constants.OT_SHIPPING_MODULE_CODE)) {
					BasicAmountType shipping = new BasicAmountType();
					shipping.setCurrencyID(urn.ebay.apis.eBLBaseComponents.CurrencyCodeType.fromValue(store.getCurrency().getCode()));
					shipping.setValue(pricingService.getStringAmount(total.getValue(), store));
					//System.out.println(pricingService.getStringAmount(total.getValue(), store));
					paymentDetails.setShippingTotal(shipping);
				}
				
				if(total.getModule().equals(Constants.OT_HANDLING_MODULE_CODE)) {
					BasicAmountType handling = new BasicAmountType();
					handling.setCurrencyID(urn.ebay.apis.eBLBaseComponents.CurrencyCodeType.fromValue(store.getCurrency().getCode()));
					handling.setValue(pricingService.getStringAmount(total.getValue(), store));
					//System.out.println(pricingService.getStringAmount(total.getValue(), store));
					paymentDetails.setHandlingTotal(handling);
				}
				
				if(total.getModule().equals(Constants.OT_TAX_MODULE_CODE)) {
					if(tax==null) {
						tax = new BigDecimal("0");
					}
					tax = tax.add(total.getValue());
				}
				
			}
			
			if(tax!=null) {
				BasicAmountType taxAmnt = new BasicAmountType();
				taxAmnt.setCurrencyID(urn.ebay.apis.eBLBaseComponents.CurrencyCodeType.fromValue(store.getCurrency().getCode()));
				taxAmnt.setValue(pricingService.getStringAmount(tax, store));
				//System.out.println(pricingService.getStringAmount(tax, store));
				paymentDetails.setTaxTotal(taxAmnt);
			}
			
			

			BasicAmountType itemTotal = new BasicAmountType();
			itemTotal.setCurrencyID(urn.ebay.apis.eBLBaseComponents.CurrencyCodeType.fromValue(store.getCurrency().getCode()));
			itemTotal.setValue(pricingService.getStringAmount(summary.getSubTotal(), store));
			paymentDetails.setItemTotal(itemTotal);
			
			paymentDetails.setPaymentDetailsItem(lineItems);
			BasicAmountType orderTotal = new BasicAmountType();
			orderTotal.setCurrencyID(urn.ebay.apis.eBLBaseComponents.CurrencyCodeType.fromValue(store.getCurrency().getCode()));
			orderTotal.setValue(pricingService.getStringAmount(summary.getTotal(), store));
			//System.out.println(pricingService.getStringAmount(itemsTotal, store));
			paymentDetails.setOrderTotal(orderTotal);
			List<PaymentDetailsType> paymentDetailsList = new ArrayList<PaymentDetailsType>();
			paymentDetails