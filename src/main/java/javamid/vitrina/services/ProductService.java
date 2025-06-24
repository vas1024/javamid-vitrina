package javamid.vitrina.services;


import jakarta.annotation.PostConstruct;
import javamid.vitrina.dao.Product;
import javamid.vitrina.dao.Basket;
import javamid.vitrina.dao.BasketItem;
import javamid.vitrina.dao.User;
import javamid.vitrina.model.Item;
import javamid.vitrina.repositories.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

@Service
public class ProductService {

  private final ProductRepository productRepository;
  private final BasketRepository basketRepository;
  private final BasketItemRepository basketItemRepository;
  private final UserRepository userRepository;
  private final ProductImageRepository productImageRepository;

  private byte[] cachedImage;

  public ProductService(ProductRepository productRepository,
                        BasketRepository basketRepository,
                        BasketItemRepository basketItemRepository,
                        UserRepository userRepository,
                        ProductImageRepository productImageRepository  ) {
    this.productRepository = productRepository;
    this.basketRepository = basketRepository;
    this.basketItemRepository = basketItemRepository;
    this.userRepository = userRepository;
    this.productImageRepository = productImageRepository;
  }


  @PostConstruct
  public void init() throws IOException {
    String filePath = "static/No_Image_Available.jpg";  // Указываем путь ОТНОСИТЕЛЬНО папки `resources` (без `classpath:`)
    ClassPathResource imgFile = new ClassPathResource(filePath);
    this.cachedImage = StreamUtils.copyToByteArray(imgFile.getInputStream());
  }


  /**
   * Сохраняет список продуктов (реактивная версия)
   */
  public Flux<Product> saveProducts(List<Product> products) {
    System.out.println("hello from productService.saveAll");
          return Flux.fromIterable(products)
              .flatMap(product -> productRepository.save(product));
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

  public Flux<Product> getProducts(String keyword, String sort, int page, int size) {
    String validSort = switch(sort) {
      case "NO", "ALPHA", "PRICE" -> sort;
      default -> "NO";
    };
    int validPage = Math.max(0, page); // Страница не может быть < 0
    int validSize = Math.max(1, size);  // Размер не может быть < 1
    long offset = (long) validPage  * validSize; // Автоматически будет 0 для page=1

    return productRepository.getProducts(
            keyword.isEmpty() ? null : keyword,
            validSort,
            validSize,
            offset
    );
  }

  public Mono<Long> countProducts(String keyword, String sort, int page, int size){
    return productRepository.countProducts( keyword );
  }


  public Mono<byte[]> getImageByProductId(Long id) {
    return productImageRepository.findImageById(id)
            .flatMap(image -> {
              if (image == null || image.length == 0) {
                return Mono.justOrEmpty(cachedImage);
              }
              return Mono.just(image);
            })
            .defaultIfEmpty(cachedImage);
  }



  public Mono<Item> getProductItem(Long productId, Long basketId) {
    return Mono.zip(
                    productRepository.findById(productId)
                            .switchIfEmpty(Mono.defer(() -> {
                              System.out.println("Product not found, id: " + productId);
                              return Mono.error(new RuntimeException("Product not found"));
                            })),

                    basketItemRepository.getQuantity(basketId, productId)
                            .defaultIfEmpty(0)
                            .doOnNext(q -> System.out.println("Quantity: " + q))
            )
            .map(tuple -> {
              Product product = tuple.getT1();
              Integer quantity = tuple.getT2();

              Item item = new Item(product);
              item.setCount(quantity);
              System.out.println("Created item: " + item.getDescription());

              return item;
            })
            .doOnError(e -> System.err.println("Error in getProductItem: " + e));
  }



  public Flux<Item> getItemsFromBasket(Long basketId) {
    return basketItemRepository.findBasketItemsAndProducts(basketId)
            .doOnNext(item -> {
                    item.setImgPath(item.getId());
                    System.out.println(
                    "[DEBUG] Item: id=" + item.getId() +
                            ", name=" + item.getTitle() +
                            ", pathToImg=" + item.getImgPath() +
                            ", quantity=" + item.getCount()
                    );
            });
  }

  public Flux<BasketItem> getBasketItems(Long basketId){
    return basketItemRepository.findByBasketId( basketId );
  }

  public Mono<Void> plusMinusDelete(Long productId, Long basketId, String action) {
    return Mono.just(action)
            .flatMap(act -> {
              switch (act) {
                case "plus":

                  return basketItemRepository.findByBasketIdAndProductId(basketId, productId)
                          .doOnSubscribe(sub -> System.out.println("[DEBUG] Starting search for basketId=" + basketId +
                                  ", productId=" + productId))
                          .doOnNext(item -> System.out.println("[DEBUG] Found existing item: " + item))
                          .flatMap(basketItem -> {
                            int newQuantity = basketItem.getQuantity() + 1;
                            System.out.println("[DEBUG] Updating quantity from " + basketItem.getQuantity() +
                                    " to " + newQuantity);

                            return basketItemRepository.updateQuantity(basketId, productId, newQuantity)
                                    .doOnSuccess(v -> System.out.println("[DEBUG] Update query executed successfully"))
                                    .thenReturn(basketItem)
                                    .doOnNext(updated -> System.out.println("[DEBUG] Returning updated item: " + updated));
                          })
                          .switchIfEmpty(
                                  Mono.defer(() -> {
                                    System.out.println("[DEBUG] No existing item found, creating new one");
                                    BasketItem newItem = new BasketItem();
                                    newItem.setBasketId(basketId);
                                    newItem.setProductId(productId);
                                    newItem.setQuantity(1);
                                    System.out.println("[DEBUG] New item to save: " + newItem);

                                    return basketItemRepository.save(newItem)
                                            .doOnSubscribe(sub -> System.out.println("[DEBUG] Starting save operation"))
                                            .doOnSuccess(saved -> System.out.println("[DEBUG] Item saved successfully: " + saved))
                                            .doOnError(e -> System.err.println("[ERROR] Failed to save item: " + e.getMessage()));
                                  })
                          )
                          .doOnError(e -> System.err.println("[ERROR] Operation failed: " + e.getMessage()))
                          .doOnTerminate(() -> System.out.println("[DEBUG] Operation completed"));

                case "minus":
                  return basketItemRepository.findByBasketIdAndProductId(basketId, productId)
                          .flatMap(basketItem -> {
                            if (basketItem.getQuantity() > 1) {
                              return basketItemRepository.updateQuantity(basketId, productId, basketItem.getQuantity() - 1);
                            } else {
                              return basketItemRepository.deleteByBasketIdAndProductId(basketId, productId);
                            }
                          });

                case "delete":
                  return basketItemRepository.deleteByBasketIdAndProductId(basketId, productId);

                default:
                  return Mono.error(new IllegalArgumentException("Invalid action: " + action));
              }
            })
            .then();
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
