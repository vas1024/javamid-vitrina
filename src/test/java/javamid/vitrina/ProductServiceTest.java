package javamid.vitrina;

import jakarta.transaction.Transactional;
import javamid.vitrina.dao.*;
import javamid.vitrina.repositories.BasketRepository;
import javamid.vitrina.repositories.OrderRepository;
import javamid.vitrina.repositories.ProductRepository;
import javamid.vitrina.repositories.UserRepository;
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
  @Autowired
  UserRepository userRepository;
  @Autowired
  OrderRepository orderRepository;

  @Test
  @Transactional
  public void getProductTest() {
    Pageable pageable = PageRequest.of(0, 5);
    Page<Product> foundProductPage = productService.getProducts("", "NO",0, 10);
    printPage(foundProductPage);
  }

  @Test
  @Transactional
  public void saveProductToBasketTest(){
    Product product = productRepository.getById(1l);
    Basket basket = basketRepository.getById( 1L );
    productService.addProductToBasket(1L, 1L);
    List<BasketItem> basketItemList = basket.getBasketItems();
    System.out.println( "Basket:");
    for( BasketItem e : basketItemList ){
      System.out.println( e.getProduct().getName() );
    }
  }

  @Test
  //@Transactional
  public void makeOrderTest(){

    Product product = productRepository.findById(1l).get();
    Basket basket = basketRepository.findById( 1L ).get();

//    productService.addProductToBasket( 1L, 1L );


    List<BasketItem> basketItemList = basket.getBasketItems();
    System.out.println( "Basket:");
    for( BasketItem e : basketItemList ){
      System.out.println( e.getProduct().getName() );
    }


    productService.makeOrder(basket);

    User user = userRepository.findById( basket.getUser().getId() ).get();
    List<Order> orderList = orderRepository.findByUserId( user.getId() );
    for( Order o : orderList){
      for( OrderItem oi: o.getOrderItems() ){
        System.out.println( "Product name: " + oi.getName() + "  price: " +  oi.getPrice() );
      }
    }



  }
}
