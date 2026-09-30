package com.salesmanager.core.business.services.customer;


import java.util.List;

import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.business.services.common.generic.SalesManagerEntityService;
import com.salesmanager.core.model.common.Address;
import com.salesmanager.core.model.customer.Customer;
import com.salesmanager.core.model.customer.CustomerCriteria;
import com.salesmanager.core.model.customer.CustomerList;
import com.salesmanager.core.model.merchant.MerchantStore;



public interface CustomerService  extends SalesManagerEntityService<Long, Customer> {

	List<Customer> getByName(String firstName);

	List<Customer> getListByStore(MerchantStore store);

	Customer getByNick(String nick);

	void saveOrUpdate(Customer customer) throws ServiceException ;

	/**
	 * Registers a new customer. The raw password is hashed with the application
	 * {@link org.springframework.security.crypto.password.PasswordEncoder} before
	 * the customer is persisted; nothing is persisted if hashing fails.
	 * @param customer new customer (without id)
	 * @param rawPassword plain-text password, never stored
	 * @throws com.salesmanager.core.business.exception.PasswordHashingException when the password cannot be hashed
	 * @throws ServiceException
	 */
	void registerCustomer(Customer customer, String rawPassword) throws ServiceException;

	CustomerList getListByStore(MerchantStore store, CustomerCriteria criteria);

	Customer getByNick(String nick, int storeId);
	Customer getByNick(String nick, String code);
	
	/**
	 * Password reset token
	 * @param storeCode
	 * @param token
	 * @return
	 */
	Customer getByPasswordResetToken(String storeCode, String token);

	/**
	 * Return an {@link com.salesmanager.core.business.common.model.Address} object from the client IP address. Uses underlying GeoLocation module
	 * @param store
	 * @param ipAddress
	 * @return
	 * @throws ServiceException
	 */
	Address getCustomerAddress(MerchantStore store, String ipAddress)
			throws ServiceException;


}
