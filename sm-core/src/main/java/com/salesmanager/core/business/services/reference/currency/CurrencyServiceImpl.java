package com.salesmanager.core.business.services.reference.currency;

import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.business.repositories.reference.currency.CurrencyRepository;
import com.salesmanager.core.business.services.common.generic.SalesManagerEntityServiceImpl;
import com.salesmanager.core.model.reference.currency.Currency;

@Service("currencyService")
public class CurrencyServiceImpl extends SalesManagerEntityServiceImpl<Long, Currency>
	implements CurrencyService {

	private static final Logger LOGGER = LoggerFactory.getLogger(CurrencyServiceImpl.class);
	
	private CurrencyRepository currencyRepository;
	
	@Inject
	public CurrencyServiceImpl(CurrencyRepository currencyRepository) {
		super(currencyRepository);
		this.currencyRepository = currencyRepository;
	}

	@Override
	public Currency getByCode(String code) {
		return currencyRepository.getByCode(code);
	}

	@Override
	public List<Currency> listSupported() {
		return currencyRepository.findBySupportedTrueOrderByCodeAsc();
	}

	@Override
	public Currency updateSupported(String code, boolean supported) throws ServiceException {
		Currency currency = currencyRepository.getByCode(code);
		if (currency == null) {
			throw new ServiceException("Currency [" + code + "] not found");
		}
		if (!supported && Boolean.TRUE.equals(currency.getSupported())
				&& currencyRepository.findBySupportedTrueOrderByCodeAsc().size() <= 1) {
			throw new ServiceException("Cannot remove the last supported currency [" + code + "]");
		}
		currency.setSupported(supported);
		Currency saved = currencyRepository.save(currency);
		LOGGER.info("Currency [{}] supported flag set to [{}]", code, supported);
		return saved;
	}

}
