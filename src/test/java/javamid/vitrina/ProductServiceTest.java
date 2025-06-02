package javamid.vitrina;

import jakarta.transaction.Transactional;
import javamid.vitrina.model.Basket;
import javamid.vitrina.model.BasketItem;
import javamid.vitrina.model.Product;
import javamid.vitrina.repositories.BasketRepository;
import javamid.vitrina.repositories.ProductRepository;
import javamid.vitrina.services.ProductService;
import static javamid.vitrina.testUtils.printPage;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

@SpringBootTest
public class ProductServiceTest {

  @Autowired
  ProductService productService;
  @Autowired
  ProductRepository productRepository;
  @Autowired
  BasketRepository basketRepository;

  @Test
  @Transactional
  public void getProductTest() {
    Pageable pageable = PageRequest.of(0, 5);
    Page<Product> foundProductPage = productService.getProducts("", 0, 10);
    printPage(foundProductPage);
  }

  @Test
  @Transactional
  public void saveProductToBasket(){
    Product product = productRepository.getById(1l);
    Basket basket = basketRepository.getById( 1L );
    productService.addProductToBasket(product,basket);
    List<BasketItem> basketItemList = basket.getBasketItems();
    System.out.println( "Basket:");
    for( BasketItem e : basketItemList ){
      System.out.println( e.getProduct().getName() );
    }
  }
}
