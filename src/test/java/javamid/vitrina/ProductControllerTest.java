package javamid.vitrina;

import jakarta.transaction.Transactional;
import javamid.vitrina.controllers.ProductController;
import javamid.vitrina.dao.Basket;
import javamid.vitrina.dao.BasketItem;
import javamid.vitrina.dao.Product;
import javamid.vitrina.repositories.BasketRepository;
import javamid.vitrina.repositories.ProductRepository;
import javamid.vitrina.repositories.UserRepository;
import javamid.vitrina.services.ProductService;
import javamid.vitrina.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;


@SpringBootTest
@AutoConfigureMockMvc
public class ProductControllerTest {
  @Autowired
  MockMvc mockMvc;

  @Autowired
  private ProductRepository productRepository;

  @Autowired
  private BasketRepository basketRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  ProductService productService;

  @Test
  @Transactional
  void testFindByIdFound() throws Exception {

    mockMvc.perform(get("/items/1"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("text/html;charset=UTF-8"));

  }

}

