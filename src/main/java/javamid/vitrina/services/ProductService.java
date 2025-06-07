package javamid.vitrina.services;

import jakarta.transaction.Transactional;
import javamid.vitrina.repositories.BasketItemRepository;
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
import java.util.Iterator;
import java.util.List;

@Service
public class ProductService {
  private final ProductRepository productRepository;
  private final BasketRepository basketRepository;
  private final OrderRepository orderRepository;
  private final BasketItemRepository basketItemRepository;

  private byte[] cachedImage;

  ProductService( ProductRepository productRepository,
                  BasketRepository basketRepository,
                  BasketItemRepository basketItemRepository,
                  OrderRepository orderRepository) {
    this.productRepository = productRepository;
    this.basketRepository = basketRepository;
    this.basketItemRepository = basketItemRepository;
    this.orderRepository = orderRepository;
  }

  @PostConstruct
  public void init() throws IOException {
    String filePath = "static/No_Image_Available.jpg";  // Указываем путь ОТНОСИТЕЛЬНО папки `resources` (без `classpath:`)
    ClassPathResource imgFile = new ClassPathResource(filePath);
    this.cachedImage = StreamUtils.copyToByteArray(imgFile.getInputStream());
  }


  public Page<Product> getProducts(String keyword, String sort, int page, int size) {
    String sortBy = "id";
    if( sort.equals("NO") )    sortBy = "id";
    if( sort.equals("ALPHA") ) sortBy = "name";
    if( sort.equals("PRICE") ) sortBy = "price";
    System.out.println("___Sort by " + sortBy);
    PageRequest pageable = PageRequest.of(page, size, Sort.by(sortBy ));  // Страница 0-based
    return productRepository.findByKeyword(keyword, pageable);
  }

  public Product getProductById( Long id ) {
    Product product = productRepository.findById(id).get();
    return product;
  }

  public void addProductToBasket( Long productId, Long basketId ) {
    Basket basket = basketRepository.findById( basketId ).get();
    List<BasketItem> basketItemList = basket.getBasketItems();
    boolean alreadyInBasket = false;
    for( BasketItem basketItem: basketItemList ){
      if( basketItem.getProduct().getId() == productId ) {
        int quantity = basketItem.getQuantity();
        quantity++;
        basketItem.setQuantity( quantity );
        alreadyInBasket = true;
        basketItemRepository.save(basketItem);
        break;
      }
    }

    if( ! alreadyInBasket ) {
      BasketItem basketItem = new BasketItem();
      Product product = productRepository.findById(productId).get();
      basketItem.setBasket(basket);
      basketItem.setProduct(product);
      basketItem.setQuantity(1);
      basketItemList.add(basketItem);
      basketRepository.save(basket);
    }
  }

  public void removeProductFromBasket( Long productId, Long basketId ) {
    Basket basket = basketRepository.findById( basketId ).get();
    List<BasketItem> basketItemList = basket.getBasketItems();
    Iterator<BasketItem> iterator = basketItemList.iterator();
      while (iterator.hasNext()) {
        BasketItem basketItem = iterator.next();
        if (basketItem.getProduct().getId() == productId) {
          int quantity = basketItem.getQuantity();
          quantity--;
          if( quantity > 0 ) {
            basketItem.setQuantity(quantity);
            basketItemRepository.save(basketItem);
          } else {
            iterator.remove();
            basketRepository.save( basket );
          }
        }
      }
  }

  public void dropProductFromBasket( Long productId, Long basketId ) {
    Basket basket = basketRepository.findById( basketId ).get();
    List<BasketItem> basketItemList = basket.getBasketItems();
    Iterator<BasketItem> iterator = basketItemList.iterator();
    while (iterator.hasNext()) {
      BasketItem basketItem = iterator.next();
      if (basketItem.getProduct().getId() == productId) {
        iterator.remove();
      }
    }
    basketRepository.save( basket );
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

  public Basket getBasketById( Long id ){
    Basket basket = basketRepository.findById( id ).get();
    return basket;
  }
  public List<BasketItem> getBasketItemsByBasketId( Long basketId ){
    Basket basket = basketRepository.findById( basketId ).get();
    return basket.getBasketItems();
  }


}
