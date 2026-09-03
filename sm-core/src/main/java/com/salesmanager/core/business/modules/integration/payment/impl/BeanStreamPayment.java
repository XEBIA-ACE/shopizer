package com.salesmanager.core.business.modules.integration.payment.impl;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.UUID;

import jakarta.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.salesmanager.core.business.services.system.MerchantLogService;
import com.salesmanager.core.business.utils.CreditCardUtils;
import com.salesmanager.core.business.utils.ProductPriceUtils;
import com.salesmanager.core.model.customer.Customer;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.order.Order;
import com.salesmanager.core.model.payments.CreditCardPayment;
import com.salesmanager.core.model.payments.Payment;
import com.salesmanager.core.model.payments.PaymentType;
import com.salesmanager.core.model.payments.Transaction;
import com.salesmanager.core.model.payments.TransactionType;
import com.salesmanager.core.model.shoppingcart.ShoppingCartItem;
import com.salesmanager.core.model.system.IntegrationConfiguration;
import com.salesmanager.core.model.system.IntegrationModule;
import com.salesmanager.core.model.system.MerchantLog;
import com.salesmanager.core.model.system.ModuleConfig;
import com.salesmanager.core.modules.integration.IntegrationException;
import com.salesmanager.core.modules.integration.payment.model.PaymentModule;


public class BeanStreamPayment implements PaymentModule {
	
	@Inject
	private ProductPriceUtils productPriceUtils;
	
	@Inject
	private MerchantLogService merchantLogService;
	

	
	private static final Logger LOGGER = LoggerFactory.getLogger(BeanStreamPayment.class);

	@Override
	public Transaction initTransaction(MerchantStore store, Customer customer,
			BigDecimal amount, Payment payment,
			IntegrationConfiguration configuration, IntegrationModule module)
			throws IntegrationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Transaction authorize(MerchantStore store, Customer customer,
			List<ShoppingCartItem> items, BigDecimal amount, Payment payment,
			IntegrationConfiguration configuration, IntegrationModule module)
			throws IntegrationException {
		return processTransaction(store, customer, TransactionType.AUTHORIZE,
				amount,
				payment,
				configuration,
				module);
	}

	@Override
	public Transaction capture(MerchantStore store, Customer customer,
			Order order, Transaction capturableTransaction,
			IntegrationConfiguration configuration, IntegrationModule module)
			throws IntegrationException {


			try {
				

				
				//authorize a preauth 

		
				String trnID = capturableTransaction.getTransactionDetails().get("TRANSACTIONID");
				
				String amnt = productPriceUtils.getAdminFormatedAmount(store, order.getTotal());
				
				/**
				merchant_id=123456789&requestType=BACKEND
				&trnType=PAC&username=user1234&password=pass1234&trnID=1000
				2115 --> requires also adjId [not documented]
				**/
				
				StringBuilder messageString = new StringBuilder();
				messageString.append("requestType=BACKEND&");
				messageString.append("merchant_id=").append(configuration.getIntegrationKeys().get("merchantid")).append("&");
				messageString.append("trnType=").append("PAC").append("&");
				messageString.append("username=").append(configuration.getIntegrationKeys().get("username")).append("&");
				messageString.append("password=").append(configuration.getIntegrationKeys().get("password")).append("&");
				messageString.append("trnAmount=").append(amnt).append("&");
				messageString.append("adjId=").append(trnID).append("&");
				messageString.append("trnID=").append(trnID);
				
				LOGGER.debug("REQUEST SENT TO BEANSTREAM -> " + messageString.toString());



				return sendTransaction(null, store, messageString.toString(), "PAC", TransactionType.CAPTURE, PaymentType.CREDITCARD, order.getTotal(), configuration, module);
				
			} catch(Exception e) {
				
				if(e instanceof IntegrationException)
					throw (IntegrationException)e;
				throw new IntegrationException("Error while processing BeanStream transaction",e);
	
			} 

	}

	@Override
	public Transaction authorizeAndCapture(MerchantStore store, Customer customer,
			List<ShoppingCartItem> items, BigDecimal amount, Payment payment,
			IntegrationConfiguration configuration, IntegrationModule module)
			throws IntegrationException {
		return processTransaction(
				store,
				customer,
				TransactionType.AUTHORIZECAPTURE,
				amount,
				payment,
				configuration,
				module);
	}

	@Override
	public Transaction refund(boolean partial, MerchantStore store, Transaction transaction,
			Order order, BigDecimal amount,
			IntegrationConfiguration configuration, IntegrationModule module)
			throws IntegrationException {

		
		
		
		HttpURLConnection conn = null;
		
		try {
			
			
			boolean bSandbox = false;
			if (configuration.getEnvironment().equals("TEST")) {// sandbox
				bSandbox = true;
			}

			ModuleConfig configs = module.getModuleConfigs().get("PROD");

			if (bSandbox) {
				configs = module.getModuleConfigs().get("TEST");
			} 
			
			if(configs==null) {
				throw new IntegrationException("Module not configured for TEST or PROD");
			}


			String server = new StringBuffer().append(
					
					configs.getScheme()).append("://")
					.append(configs.getHost())
							.append(":")
							.append(configs.getPort())
							.append(configs.getUri()).toString();

			String trnID = transaction.getTransactionDetails().get("TRANSACTIONID");
			
			String amnt = productPriceUtils.getAdminFormatedAmount(store, amount);
			
			/**
			merchant_id=123456789&requestType=BACKEND
			&trnType=R&username=user1234&password=pass1234
			&trnOrderNumber=1234&trnAmount=1.00&adjId=1000
			2115
			**/
			StringBuilder messageString = new StringBuilder();



			messageString.append("requestType=BACKEND&");
			messageString.append("merchant_id=").append(configuration.getIntegrationKeys().get("merchantid")).append("&");
			messageString.append("trnType=").append("R").append("&");
			messageString.append("username=").append(configuration.getIntegrationKeys().get("username")).append("&");
			messageString.append("password=").append(configuration.getIntegrationKeys().get("password")).append("&");
			messageString.append("trnOrderNumber=").append(transaction.getTransactionDetails().get("TRNORDERNUMBER")).append("&");
			messageString.append("trnAmount=").append(amnt).append("&");
			messageString.append("adjId=").append(trnID);
			
			LOGGER.debug("REQUEST SENT TO BEANSTREAM -> " + messageString.toString());
	
			
		
			
			URL postURL = new URL(server);
			conn = (HttpURLConnection) postURL.openConnection();




			return sendTransaction(null, store, messageString.toString(), "R", TransactionType.REFUND, PaymentType.CREDITCARD, amount, configuration, module);
			
		} catch(Exception e) {
			
			if(e instanceof IntegrationException)
				throw (IntegrationException)e;
			throw new IntegrationException("Error while processing BeanStream transaction",e);

		} finally {
			
			
			if (conn != null) {
				try {
					conn.disconnect();
				} catch (Exception ignore) {
					// TODO: handle exception
				}
			}
		}
		
		
		
	}
	
	
	private Transaction sendTransaction(
			String orderNumber,
			MerchantStore store,
			String transaction, 
			String beanstreamType, 
			TransactionType transactionType,
			PaymentType paymentType,
			BigDecimal amount,
			IntegrationConfiguration configuration,
			IntegrationModule module
			) throws IntegrationException {
		
		String agent = "Mozilla/4.0";
		String respText = "";
		Map<String,String> nvp = null;
		DataOutputStream output = null;
		DataInputStream in = null;
		BufferedReader is = null;
		HttpURLConnection conn =null;
		try {
			
			//transaction = "requestType=BACKEND&merchant_id=300200260&trnType=P&username=carlito&password=shopizer001&orderNumber=caa71106-7e3f-4975-a657-a35904dc32a0&trnCardOwner=Carl Samson&trnCardNumber=5100000020002000&trnExpMonth=10&trnExpYear=14&trnCardCvd=123&trnAmount=77.01&ordName=Carl S&ordAddress1=358 Du Languedoc&ordCity=Victoria&ordProvince=BC&ordPostalCode=V8T2E7&ordCountry=CA&ordPhoneNumber=(444) 555-6666&ordEmailAddress=csamson777@yahoo.com";
			/**
			requestType=BACKEND&merchant_id=300200260
			&trnType=P
			&username=carlito&password=shopizer001
			&orderNumber=caa71106-7e3f-4975-a657-a35904dc32a0
			&trnCardOwner=Carl Samson
			&trnCardNumber=5100000020002000
			&trnExpMonth=10
			&trnExpYear=14
			&trnCardCvd=123
			&trnAmount=77.01
			&ordName=Carl S
			&ordAddress1=378 Du Languedoc
			&ordCity=Boucherville
			&ordProvince=QC
			&ordPostalCode=J3B8J1
			&ordCountry=CA
			&ordPhoneNumber=(444) 555-6666
			&ordEmailAddress=test@yahoo.com
			**/
			
			/**
			merchant_id=123456789&requestType=BACKEND
			&trnType=P&trnOrderNumber=1234TEST&trnAmount=5.00&trnCardOwner=Joe+Test
					&trnCardNumber=4030000010001234
					&trnExpMonth=10
					&trnExpYear=16
					&ordName=Joe+Test
					&ordAddress1=123+Test+Street
					&ordCity=Victoria
					&ordProvince=BC
					&ordCountry=CA
					&ordPostalCode=V8T2E7
					&ordPhoneNumber=5555555555
					&ordEmailAddress=joe%40testemail.com
			**/
			
			
			
			boolean bSandbox = false;
			if (configuration.getEnvironment().equals("TEST")) {// sandbox
				bSandbox = true;
			}

			String server = "";
			
			ModuleConfig configs = module.getModuleConfigs().get("PROD");

			if (bSandbox) {
				configs = module.getModuleConfigs().get("TEST");
			} 
			
			if(configs==null) {
				throw new IntegrationException("Module not configured for TEST or PROD");
			}
			

			server = new StringBuffer().append(
					
					configs.getSch