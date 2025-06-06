package javamid.vitrina;

import javamid.vitrina.controllers.ProductController;
import javamid.vitrina.dao.Product;
import javamid.vitrina.services.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@WebMvcTest(ProductController.class)
public class ProductControllerTest {
  @Autowired
  MockMvc mockMvc;

  @MockitoBean
  ProductService productService;

  @Test
  void testFindByIdFound() throws Exception {

    Product product = new Product();
    product.setId(1L);
    product.setName("cap");
    product.setDescription("for controller shor test");
    product.setPrice(BigDecimal.valueOf(1000));

    doReturn(product).when(productService).getProductById(1L);

    mockMvc.perform(get("/items/1"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("text/html;charset=UTF-8"));
  }

}
