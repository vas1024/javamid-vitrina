package javamid.vitrina;

import javamid.vitrina.controllers.ProductController;
import javamid.vitrina.services.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProductController.class)
public class ProductControllerTest {
  @Autowired
  MockMvc mockMvc;

  @MockitoBean
  ProductService productService;

  @Test
  void testFindByIdFound() throws Exception {
    Order order = new Order();
    order.setId(3L);
    order.setNumber("#123456");
    order.setDate(LocalDate.now());
    doReturn(Optional.of(order)).when(orderService).findById(3L);

    mockMvc.perform(get("/orders/3"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("text/html;charset=UTF-8"));
  }

}
