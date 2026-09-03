```java
package com.shopizer.api.v1.products;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.shopizer.api.v1.dto.ProductDTO;
import com.shopizer.services.localization.LocaleService;
import com.shopizer.store.ProductService;

@RestController
public class ProductAPI {

    @Autowired
    private ProductService productService;

    @Autowired
    private LocaleService localeService;

    @GetMapping("/products")
    public List<ProductDTO> getProducts(@RequestHeader("user-id") Long userId) {
        Locale userLocale = localeService.getUserLocale(userId);

        return productService.getAllProducts().stream()
                .map(product -> translateProduct(product, userLocale))
                .collect(Collectors.toList());
    }

    private ProductDTO translateProduct(ProductDTO product, Locale locale) {
        // Translate product based on the locale - this is simplistic for illustration
        // In reality, translations should come from a persistent source or service
        if (locale.getLanguage().equals("fr")) {
            product.setName("Produit: " + product.getName());
            product.setDescription("Description en français: " + product.getDescription());
        }
        return product;
    }
}
```