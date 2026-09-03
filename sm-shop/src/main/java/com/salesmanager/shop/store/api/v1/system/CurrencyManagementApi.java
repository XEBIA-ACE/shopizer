```java
package com.salesmanager.shop.store.api.v1.system;

import com.salesmanager.core.business.services.reference.currency.CurrencyService;
import com.salesmanager.core.model.reference.currency.Currency;
import com.salesmanager.shop.store.controller.store.facade.StoreFacade;
import com.salesmanager.shop.utils.SuccessResponse;
import com.salesmanager.shop.utils.StoreBeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/private/currencies")
public class CurrencyManagementApi {

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private StoreFacade storeFacade;

    @GetMapping
    public ResponseEntity<List<Currency>> listCurrencies() {
        List<Currency> currencies = currencyService.list();
        return new ResponseEntity<>(currencies, HttpStatus.OK);
    }

    @PostMapping("/add")
    public ResponseEntity<SuccessResponse> addCurrency(@RequestBody Currency currency) {
        currencyService.create(currency);
        return new ResponseEntity<>(new SuccessResponse("Currency added successfully"), HttpStatus.CREATED);
    }

    @DeleteMapping("/remove/{code}")
    public ResponseEntity<SuccessResponse> removeCurrency(@PathVariable String code) {
        Currency currency = currencyService.getByCode(code);
        if (currency != null) {
            currencyService.delete(currency);
            return new ResponseEntity<>(new SuccessResponse("Currency removed successfully"), HttpStatus.OK);
        }
        return new ResponseEntity<>(new SuccessResponse("Currency not found"), HttpStatus.NOT_FOUND);
    }
}
```