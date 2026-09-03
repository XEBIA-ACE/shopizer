package com.salesmanager.shop.store.controller.currency.facade;

import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.business.services.reference.currency.CurrencyService;
import com.salesmanager.core.model.reference.currency.Currency;
import com.salesmanager.shop.model.references.ReadableCurrency;
import com.salesmanager.shop.store.api.exception.ResourceNotFoundException;
import com.salesmanager.shop.store.api.exception.ServiceRuntimeException;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.springframework.stereotype.Service;

@Service
public class CurrencyFacadeImpl implements CurrencyFacade {

  private static final Comparator<Currency> BY_CODE = Comparator.comparing(Currency::getCode);

  @Inject
  private CurrencyService currencyService;

  @Override
  public List<Currency> getList() {
    List<Currency> currencyList = currencyService.listSupported();
    if (currencyList.isEmpty()){
      throw new ResourceNotFoundException("No supported currencies found");
    }
    currencyList.sort(BY_CODE);
    return currencyList;
  }

  @Override
  public List<ReadableCurrency> getAll() {
    List<Currency> currencyList = currencyService.list();
    if (currencyList.isEmpty()){
      throw new ResourceNotFoundException("No currencies found");
    }
    return currencyList.stream().sorted(BY_CODE).map(this::toReadable).collect(Collectors.toList());
  }

  @Override
  public ReadableCurrency setSupported(String code, boolean supported) {
    if (currencyService.getByCode(code) == null) {
      throw new ResourceNotFoundException("Currency [" + code + "] not found");
    }
    try {
      return toReadable(currencyService.updateSupported(code, supported));
    } catch (ServiceException e) {
      throw new ServiceRuntimeException(e.getMessage(), e);
    }
  }

  private ReadableCurrency toReadable(Currency currency) {
    ReadableCurrency readable = new ReadableCurrency();
    readable.setId(currency.getId());
    readable.setCode(currency.getCode());
    readable.setName(currency.getName());
    readable.setSymbol(currency.getSymbol());
    readable.setSupported(Boolean.TRUE.equals(currency.getSupported()));
    return readable;
  }
}
