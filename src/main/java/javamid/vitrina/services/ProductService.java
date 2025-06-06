package javamid.vitrina.services;

import javamid.vitrina.repositories.BasketRepository;
import javamid.vitrina.repositories.OrderRepository;
import javamid.vitrina.repositories.ProductRepository;
import javamid.vitrina.dao.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import jakarta.annotation.PostConstruct;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {
  private final ProductRepository productRepository;
  private final BasketRepository basketRepository;
  private final OrderRepository orderRepository;

  private byte[] cachedImage;

  ProductService( ProductRepository productRepository,
                  BasketRepository basketRepository,
                  OrderRepository orderRepository) {
    this.productRepository = productRepository;
    this.basketRepository = basketRepository;
    this.orderRepository = orderRepository;
  }

  @PostConstruct
  public void init() throws IOException {
    String filePath = "static/No_Image_Available.jpg";  // Указываем путь ОТНОСИТЕЛЬНО папки `resources` (без `classpath:`)
    ClassPathResource imgFile = new ClassPathResource(filePath);
    this.cachedImage = StreamUtils.copyToByteArray(imgFile.getInputStream());
  }


  public Page<Product> getProducts(String keyword, int page, int size) {
    PageRequest pageable = PageRequest.of(page, size, Sort.by("name"));  // Страница 0-based
    return productRepository.findByKeyword(keyword, pageable);
  }

  public Product getProductById( Long id ) {
    return productRepository.getById(id);
  }
  public void addProductToBasket(Product product, Basket basket) {
    BasketItem basketItem = new BasketItem();
    basketItem.setBasket( basket );
    basketItem.setProduct( product );
    List<BasketItem> basketItemList = basket.getBasketItems();
    basketItemList.add( basketItem );
    basketRepository.save(basket);
  }

  public void makeOrder(Basket basket) {
    User user = basket.getUser();
    Order order = new Order();
    order.setUser(user);
    List<OrderItem> orderItemList = new ArrayList<>();
    order.setOrderItems(orderItemList);
    List<BasketItem> basketItemList = basket.getBasketItems();
    for( BasketItem basketItem : basketItemList ) {
      OrderItem orderItem = new OrderItem();
      orderItem.setName(basketItem.getProduct().getName());
      orderItem.setImage(basketItem.getProduct().getImage());
      orderItem.setPrice(basketItem.getProduct().getPrice());
      orderItemList.add(orderItem);
    }
    orderRepository.save(order);
    basketRepository.deleteById(basket.getId());
  }

  public byte[] getImageByProductId( Long id ){
    if( id == 0L ) return cachedImage;
    return productRepository.findImageById( id );
  }

  public void saveAll( List<Product> products ){
    productRepository.saveAll( products );
  }
}
