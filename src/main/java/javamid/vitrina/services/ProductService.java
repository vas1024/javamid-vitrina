package javamid.vitrina.services;


import javamid.vitrina.dao.Product;
import javamid.vitrina.dao.Basket;
import javamid.vitrina.dao.BasketItem;
import javamid.vitrina.dao.User;
import javamid.vitrina.repositories.BasketItemRepository;
import javamid.vitrina.repositories.BasketRepository;
import javamid.vitrina.repositories.ProductRepository;
import javamid.vitrina.repositories.UserRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class ProductService {

  private final ProductRepository productRepository;
  private final BasketRepository basketRepository;
  private final BasketItemRepository basketItemRepository;
  private final UserRepository userRepository;

  public ProductService(ProductRepository productRepository,
                        BasketRepository basketRepository,
                        BasketItemRepository basketItemRepository,
                        UserRepository userRepository  ) {
    this.productRepository = productRepository;
    this.basketRepository = basketRepository;
    this.basketItemRepository = basketItemRepository;
    this.userRepository = userRepository;
  }

  /**
   * Сохраняет список продуктов (реактивная версия)
   */
  public Mono<Void> saveAll(List<Product> products) {
    return Flux.fromIterable(products)
            .flatMap(productRepository::save)
            .then();
  }

  public Mono<Basket> getBasketById( Long id ){
    return basketRepository.findById( id );
  }

  public Flux<BasketItem> getBasketItemsByBasketId( Long basketId ){
    return basketItemRepository.findByBasketId( basketId );
  }

  public Mono<Product> getProductById( Long id ) {
    return productRepository.findById(id);
  }

  public Mono<User> getUserById( Long id ) {
    return userRepository.findById( id );
  }

  public Flux<Product> getProducts( String keyword, String sort, int page, int size ) {
    return productRepository.findAll();
  }

/*
  public Page<Product> getProducts(String keyword, String sort, int page, int size) {
    String sortBy = "id";
    if( sort.equals("NO") )    sortBy = "id";
    if( sort.equals("ALPHA") ) sortBy = "name";
    if( sort.equals("PRICE") ) sortBy = "price";
    System.out.println("___Sort by " + sortBy);
    PageRequest pageable = PageRequest.of(page, size, Sort.by(sortBy ));  // Страница 0-based
    return productRepository.findByKeyword(keyword, pageable);
  }


    public Flux<Product> searchProducts(String keyword, int page, int size, String sort) {
    long offset = (long) page * size;
    return productRepository.findByKeyword(keyword, size, offset, sort);
  }

*/







/*
  public Mono<Long> saveAllAndReturnCount(List<Product> products) {
    return Flux.fromIterable(products)
            .flatMap(productRepository::save)
            .count();
  }


  public Flux<Product> searchProducts(String keyword, int page, int size, String sort) {
    long offset = (long) page * size;
    return productRepository.findByKeyword(keyword, size, offset, sort);
  }


  public Mono<byte[]> getProductImage(Long productId) {
    return productRepository.findImageById(productId);
  }


  public Mono<Long> countProductsByKeyword(String keyword) {
    return productRepository.countByKeyword(keyword);
  }


  public Mono<Void> saveInBatches(List<Product> products, int batchSize) {
    return Flux.fromIterable(products)
            .buffer(batchSize)
            .flatMap(batch -> productRepository.saveAll(batch))
            .then();
  }


  public Mono<Void> deleteAllProducts() {
    return productRepository.deleteAll();
  }

  */
}


/*
@Service
public class ProductService {
  private final ProductRepository productRepository;
  private final BasketRepository basketRepository;
  private final OrderRepository orderRepository;
  private final OrderItemRepository orderItemRepository;
  private final BasketItemRepository basketItemRepository;


  private byte[] cachedImage;

  ProductService( ProductRepository productRepository,
                  BasketRepository basketRepository,
                  BasketItemRepository basketItemRepository,
                  OrderRepository orderRepository,
                  OrderItemRepository orderItemRepository) {
    this.productRepository = productRepository;
    this.basketRepository = basketRepository;
    this.basketItemRepository = basketItemRepository;
    this.orderRepository = orderRepository;
    this.orderItemRepository = orderItemRepository;
  }

  @PostConstruct
  public void init() throws IOException {
    String filePath = "static/No_Image_Available.jpg";  // Указываем путь ОТНОСИТЕЛЬНО папки `resources` (без `classpath:`)
    ClassPathResource imgFile = new ClassPathResource(filePath);
    this.cachedImage = StreamUtils.copyToByteArray(imgFile.getInputStream());
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

  @Transactional // чтобы сделался коммит при удалении корзины
  public Long makeOrder(Basket basket) {

    User user = basket.getUser();
    Order order = new Order();
    order.setUser(user);
    List<OrderItem> orderItemList = new ArrayList<>();
    order.setOrderItems(orderItemList);
    List<BasketItem> basketItemList = basket.getBasketItems();
    for( BasketItem basketItem : basketItemList ) {
      OrderItem orderItem = new OrderItem();
      orderItem.setOrder( order );
      orderItem.setName(basketItem.getProduct().getName());
      orderItem.setImage(basketItem.getProduct().getImage());
      orderItem.setPrice(basketItem.getProduct().getPrice());
      orderItem.setQuantity(basketItem.getQuantity());
      orderItem.setProductId(basketItem.getProduct().getId());
      orderItemList.add(orderItem);
    }
    orderRepository.save(order);
    basket.setBasketItems(new ArrayList<BasketItem>() );
    basketRepository.save(basket);
    return order.getId();
  }


  public byte[] getImageByProductId( Long id ){
    if( id == 0L ) return cachedImage;
    return productRepository.findImageById( id );
  }

  public byte[] getImageByOrderItemId( Long id ){
    if( id == 0L ) return cachedImage;
    return orderItemRepository.findImageById( id );
  }

  public void saveAll( List<Product> products ){
    productRepository.saveAll( products );
  }

  public Basket getBasketById( Long id ){
    Optional<Basket> basketOptional= basketRepository.findById( id );
    if( basketOptional.isEmpty() ) return null;
    else return basketOptional.get();
  }



  public List<Order> findAllOrders( Long basketId ){
    List<Order> orders = orderRepository.findAll();
    orders.forEach(order-> Hibernate.initialize(order.getOrderItems()));
    return orders;
  }

  public Order findOrderById( Long orderId ){
    Order order = orderRepository.findById( orderId ).get();
    return order;
  }

}

 */
