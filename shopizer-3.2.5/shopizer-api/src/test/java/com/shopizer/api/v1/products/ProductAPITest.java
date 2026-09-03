```java
package com.shopizer.api.v1.products;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.shopizer.api.v1.dto.ProductDTO;
import com.shopizer.services.localization.LocaleService;
import com.shopizer.store.ProductService;

@SpringJUnitConfig
public class ProductAPITest {

    @Mock
    private ProductService productService;

    @Mock
    private LocaleService localeService;

    @InjectMocks
    private ProductAPI productAPI;

    public ProductAPITest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetProductsInFrench() {
        Long userId = 1L;
        when(localeService.getUserLocale(userId)).thenReturn(Locale.FRENCH);
        
        ProductDTO product1 = new ProductDTO("Product 1", "Description 1");
        List<ProductDTO> products = Arrays.asList(product1);
        
        when(productService.getAllProducts()).thenReturn(products);
        
        List<ProductDTO> result = productAPI.getProducts(userId);
        
        assertEquals("Produit: Product 1", result.get(0).getName());
    }
}
```