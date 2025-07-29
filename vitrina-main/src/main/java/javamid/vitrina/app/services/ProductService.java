package javamid.vitrina.app.services;


import jakarta.annotation.PostConstruct;
import javamid.vitrina.app.dao.*;
import javamid.vitrina.app.model.Item;
import javamid.vitrina.app.repositories.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Collections;
import java.util.List;


@Service
public class ProductService {

  private final ProductRepository productRepository;
  private final BasketRepository basketRepository;
  private final BasketItemRepository basketItemRepository;
  private final UserRepository userRepository;
  private final ProductImageRepository productImageRepository;
  private final OrderRepository orderRepository;
  private final OrderItemRepository orderItemRepository;

  private byte[] cachedImage;


  public ProductService(ProductRepository productRepository,
                        BasketRepository basketRepository,
                        BasketItemRepository basketItemRepository,
                        UserRepository userRepository,
                        ProductImageRepository productImageRepository,
                        OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository  ) {
    this.productRepository = productRepository;
    this.basketRepository = basketRepository;
    this.basketItemRepository = basketItemRepository;
    this.userRepository = userRepository;
    this.productImageRepository = productImageRepository;
    this.orderRepository = orderRepository;
    this.orderItemRepository = orderItemRepository;
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


  public Mono<byte[]> getImageByOrderItemId(Long id) {
    return productImageRepository.findOrderItemImageById(id)
            .flatMap(image -> {
              if (image == null || image.length == 0) {
                return Mono.justOrEmpty(cachedImage);
              }
              return Mono.just( image );
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


  public Flux<Order> getOrders(Long userId ){
    return orderRepository.findAllByUserId( userId );
  }

  public Flux<OrderItem> getOrderItems( Long orderId ){
    return orderItemRepository.findByOrderId( orderId );
  }


  public Mono<Long> findUserIdByBasketId(Long basketId) {
    return basketRepository.findUserIdByBasketId(basketId);
  }


  public Mono<Long> makeOrder(Long basketId) {
    return basketRepository.findUserIdByBasketId(basketId)
            .flatMap(userId -> basketRepository.findByUserId(userId)
                    .flatMap(basket -> {
                      // 1. Создаем и сохраняем заказ
                      Order order = new Order();
                      order.setUserId(userId);

                      return orderRepository.save(order)
                              .flatMap(savedOrder -> {
                                // 2. Обрабатываем элементы корзины
                                return basketItemRepository.findByBasketId(basketId)
                                        .flatMap(basketItem -> productRepository.findById(basketItem.getProductId())
                                                .flatMap(product -> {
                                                  // 3. Создаем и сохраняем OrderItem
                                                  OrderItem orderItem = new OrderItem();
                                                  orderItem.setOrderId(savedOrder.getId());
                                                  orderItem.setQuantity(basketItem.getQuantity());
                                                  orderItem.setName(product.getName());
                                                  orderItem.setImage(product.getImage());
                                                  orderItem.setPrice(product.getPrice());
                                                  orderItem.setProductId(product.getId());

                                                  return orderItemRepository.save(orderItem)


                                                          .thenReturn( basketItem.getProductId()
                                                                  + ":"
                                                                  + basketItem.getQuantity()
                                                          );


                                                })
                                        )
                                        .collectList()

                                        .doOnNext(list -> {
                                          System.out.println("Debug: Собранные productId:quantity -> " + list);
                                        })

                                        .flatMap(orderItemHashes -> {
                                            // 1. Сортируем, чтобы порядок не влиял на хеш
                                            Collections.sort(orderItemHashes);
                                            // 2. Объединяем в одну строку через запятую (или другой разделитель)
                                            String fullOrderHashInput = String.join(",", orderItemHashes);
                                            // 4. Логируем для отладки
                                            System.out.println("Debug: Полная строка для хеша заказа: " + fullOrderHashInput);


                                          // 4. Удаляем элементы корзины и возвращаем ID заказа
                                          return basketItemRepository.deleteByBasketId(basketId)
                                                  .thenReturn(savedOrder.getId());
                                        });
                              });
                    })
            );
  }



}

