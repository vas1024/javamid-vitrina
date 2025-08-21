package javamid.vitrina.app.controllers;


import javamid.vitrina.app.dao.BasketItem;
import javamid.vitrina.app.dao.User;
import javamid.vitrina.app.model.Item;
import javamid.vitrina.app.model.Paging;
import javamid.vitrina.app.dao.Product;
import javamid.vitrina.app.services.PaymentService;
import javamid.vitrina.app.services.ProductService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.net.URI;


import org.springframework.web.bind.annotation.GetMapping;
import reactor.util.retry.Retry;

@Controller
public class ProductController {

  private final ProductService productService;
  private final PaymentService paymentService;
  public ProductController(ProductService productService,
                           PaymentService paymentService  ) {
    this.productService = productService;
    this.paymentService = paymentService;
  }


  record OrderModel(Long id, BigDecimal totalSum, List<Item> items) {}

//  public Long currentBasket() { return 1L; }
//  public Long currentUser() { return 1L; }


  private Mono<User> getCurrentUser() {
    return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .map(Authentication::getName) // получаем username
            .flatMap(username -> productService.findByLogin(username))
//            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED)));
            .defaultIfEmpty( new User() );
  }

//  public Mono<Long> currentUserId() { return getCurrentUser().map(User::getId); }
//  public Mono<Long> currentBasketId() { return getCurrentUser().map(User::getBasketId);  }
public Mono<Long> currentUserId() {
  return getCurrentUser()
          .map(user -> user.getId() != null ? user.getId() : -1L)
          .defaultIfEmpty(-1L);
}
public Mono<Long> currentBasketId() {
  return getCurrentUser()
          .map(user -> user.getBasketId() != null ? user.getBasketId() : -1L)
          .defaultIfEmpty(-1L);
}




  private Mono<Boolean> isAuthenticated() {
    return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .map(Authentication::isAuthenticated)
            .defaultIfEmpty(false);
  }


    private Mono<String> getCurrentUsername() {
    return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .map(Authentication::getName)
            .defaultIfEmpty("anonymous");
  }



  @GetMapping("/")
  public Mono<String> homePage() {
    return Mono.just("redirect:/main/items");
  }



  @GetMapping("/main/items")
  public Mono<String> getItems(
          @RequestParam(name = "search", required = false, defaultValue = "") String keyword,
          @RequestParam(name = "sort", defaultValue = "NO") String sort,
          @RequestParam(name = "pageSize", defaultValue = "10") int size,
          @RequestParam(name = "pageNumber", defaultValue = "1") int page,
          Model model) {

    Mono<Long> totalItemsMono = productService.countProducts(keyword, sort, page - 1, size);
    Flux<Product> productFlux = productService.getProducts(keyword, sort, page - 1, size);
//    Flux<BasketItem> basketItemFlux = productService.getBasketItems(currentBasket());




    // 1. Текущий пользователь (или null для гостей)
     Mono<User> currentUser = ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .map(Authentication::getName)
            .flatMap(productService::findByLogin)
            .defaultIfEmpty(new User())
            .doOnNext(user -> {
              if (user != null) {
                System.out.println("👤 Current user: " + user.getLogin() + ", ID: " + user.getId());
              } else {
                System.out.println("👤 Guest user");
              }
            });

// 2. ID пользователя (или null для гостей)
     Mono<Long> currentUserId = currentUser
            .map(user -> user != null ? user.getId() : null)
            .doOnNext(userId -> System.out.println("🆔 User ID: " + userId));

// 3. ID корзины (или null для гостей)
     Mono<Long> currentBasketId = currentUser
            .map(user -> user != null ? user.getBasketId() : null)
            .doOnNext(basketId -> System.out.println("🛒 Basket ID: " + basketId));





    Mono<Boolean> isAuthenticated = ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .doOnNext(auth -> System.out.println("🔐 Authentication object: " + auth))
            .map(Authentication::isAuthenticated)
            .doOnNext(authStatus -> System.out.println("✅ Authenticated status: " + authStatus))
            .defaultIfEmpty(false)
            .doOnNext(finalAuthStatus -> System.out.println("🎯 Final auth status: " + finalAuthStatus));

    Flux<BasketItem> basketItemFlux = isAuthenticated
            .doOnNext(authStatus -> System.out.println("🔄 Starting basket processing, authenticated: " + authStatus))
            .flatMapMany(authenticated -> {
              if (authenticated) {
                System.out.println("👤 User is authenticated, getting basket...");
                return currentBasketId()
                        .doOnNext(basketId -> System.out.println("🛒 Basket ID: " + basketId))
                        .flatMapMany(basketId -> {
                          System.out.println("📦 Fetching basket items for basket: " + basketId);
                          return productService.getBasketItems(basketId)
                                  .doOnNext(item -> System.out.println("📋 Basket item: " + item.getProductId() + " x" + item.getQuantity()))
                                  .doOnComplete(() -> System.out.println("✅ Basket items loading completed"))
                                  .doOnError(e -> System.out.println("❌ Error loading basket items: " + e.getMessage()));
                        });
              } else {
                System.out.println("👤 User is NOT authenticated, returning empty basket");
                return Flux.empty(); // Пустой Flux для гостей
              }
            })
            .doOnNext(item -> System.out.println("🎁 Processing basket item: " + item.getProductId()))
            .doOnComplete(() -> System.out.println("✅ All basket items processed"))
            .doOnError(e -> System.out.println("❌ Error in basket flow: " + e.getMessage()));

    Mono<Map<Long, Integer>> basketItemsMapMono = basketItemFlux
            .doOnNext(item -> System.out.println("🗺️ Collecting to map: " + item.getProductId() + " → " + item.getQuantity()))
            .collectMap(BasketItem::getProductId, BasketItem::getQuantity)
            .doOnNext(map -> System.out.println("🗺️ Final items map: " + map))
            .defaultIfEmpty(Map.of())
            .doOnNext(finalMap -> System.out.println("🎯 Final map (after default): " + finalMap))
            .doOnError(e -> System.out.println("❌ Error creating map: " + e.getMessage()));

    Mono<List<Item>> itemsMono = basketItemsMapMono
            .doOnNext(map -> System.out.println("🛍️ Starting items processing with map: " + map))
            .flatMap(basketItemsMap -> {
              System.out.println("🛒 Processing products with basket map");
              return productFlux
                      .doOnNext(product -> System.out.println("📦 Processing product: " + product.getId()))
                      .map(product -> {
                        Item item = new Item(product);
                        Integer count = basketItemsMap.get(product.getId());
                        item.setCount(count != null ? count : 0);
                        System.out.println("🎯 Product " + product.getId() + " → count: " + item.getCount());
                        return item;
                      })
                      .collectList()
                      .doOnNext(itemsList -> System.out.println("✅ Final items list: " + itemsList.size() + " items"));
            })
            .defaultIfEmpty(List.of())
            .doOnNext(finalList -> System.out.println("🎯 Final list (after default): " + finalList.size() + " items"))
            .doOnError(e -> System.out.println("❌ Error in items processing: " + e.getMessage()));





/*
    Mono<Boolean> isAuthenticated = ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .map(Authentication::isAuthenticated)
            .defaultIfEmpty(false);


    Flux<BasketItem> basketItemFlux = isAuthenticated
            .flatMapMany(authenticated -> {
              if (authenticated) {
                return currentBasketId()
                        .flatMapMany(productService::getBasketItems);
              } else {
                return Flux.empty(); // Пустой Flux для гостей
              }
            });

    Mono<Map<Long, Integer>> basketItemsMapMono = basketItemFlux
            .collectMap(BasketItem::getProductId, BasketItem::getQuantity)
            .defaultIfEmpty(Map.of()); // Пустая map для гостей

    Mono<List<Item>> itemsMono = basketItemsMapMono.flatMap(basketItemsMap ->
            productFlux
                    .map(product -> {
                      Item item = new Item(product);
                      item.setCount(basketItemsMap.getOrDefault(product.getId(), 0));
                      return item;
                    })
                    .collectList()
    ).defaultIfEmpty(List.of()); // Пустой список для гостей
*/





/*
    Flux<BasketItem> basketItemFlux = currentBasketId()
            .flatMapMany(productService::getBasketItems);

    Mono<Map<Long, Integer>> basketItemsMapMono = basketItemFlux
            .collectMap(BasketItem::getProductId, BasketItem::getQuantity);

    Mono<List<Item>> itemsMono = basketItemsMapMono.flatMap(basketItemsMap ->
            productFlux
                    .map(product -> {
                      Item item = new Item(product);
                      item.setCount(basketItemsMap.getOrDefault(product.getId(), 0));
                      return item;
                    })
                    .collectList()
    );
*/



    return Mono.zip(itemsMono, totalItemsMono)
            .doOnNext(tuple -> {
              List<Item> items = tuple.getT1();
              long totalItems = tuple.getT2();

              model.addAttribute("items", items);
              model.addAttribute("search", keyword);
              model.addAttribute("sort", sort);

              Paging paging = new Paging();
              paging.setPageNumber(page);
              paging.setPageSize(size);

              int totalPages = (int) Math.ceil((double) totalItems / size);
              paging.setNext(page < totalPages);
              paging.setPrevious(page > 1);

              model.addAttribute("paging", paging);
              model.addAttribute("totalItems", totalItems);
            })
            .thenReturn("main.html");
  }






  @GetMapping("/images/{id}")
  public Mono<ResponseEntity<byte[]>> getImage(@PathVariable Long id) {
    return productService.getImageByProductId(id)
            .map(imageData -> ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .header(HttpHeaders.CACHE_CONTROL, "no-transform")
                    .body(imageData));
  }

  @GetMapping("/orderimages/{id}")
  public Mono<ResponseEntity<byte[]>> getOrderImage(@PathVariable Long id) {
    return productService.getImageByOrderItemId(id)
            .map(imageData -> ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .header(HttpHeaders.CACHE_CONTROL, "no-transform")
                    .body(imageData));
  }


  @GetMapping("/items/{id}")
  public Mono<String> getItem(@PathVariable(name="id") long id,
                        Model model ) {


    return currentBasketId()
            .flatMap(basketId -> productService.getProductItem(id, basketId))
            .doOnNext(item -> model.addAttribute("item", item))
            .thenReturn("item.html")
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
            .doOnError(e -> System.err.println("Error: " + e));


  }




  @GetMapping("/cart/items")
  @PreAuthorize("isAuthenticated()")
  public Mono<String> getBasket( Model model ){
    Mono<Long> userIdMono = currentUserId();
    Mono<BigDecimal> balanceMono = userIdMono.flatMap( userId->paymentService.getUserBalance(userId));
    Flux<Item> basketItems = currentBasketId()
            .flatMapMany(productService::getItemsFromBasket);


    return basketItems
            .collectList()
            .flatMap(items -> {
              BigDecimal total = items.stream()
                      .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getCount())))
                      .reduce(BigDecimal.ZERO, BigDecimal::add);
              model.addAttribute("items", items);
              model.addAttribute("empty", items.isEmpty());
              model.addAttribute("total", total);


              return balanceMono.flatMap(balance -> {
                if (balance.compareTo(new BigDecimal("-1")) == 0) {
                  System.out.println("ProductController:getBasket: payment service -1");
                  model.addAttribute("error", "service_unavailable");
                }
                return Mono.just("cart");
              });

            });
  }





// три раза одинаковый код. костыль потому что не получается  @RequestParam(name="action")
  @PostMapping(value = "/items/{id}", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE )
  public Mono<String> addRemoveDeleteProductInProductItem( @PathVariable(name="id") long id,
                                                      ServerWebExchange exchange  ) {
    return exchange.getFormData()
            .flatMap(formData -> {
              String action = formData.getFirst("action");
              return currentBasketId()
                      .flatMap(currentBasket ->
                              productService.plusMinusDelete(id, currentBasket, action)
                      )
                      .thenReturn("redirect:/items/" + id);
            });
  }
  @PostMapping(value = "/cart/items/{id}", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE )
  public Mono<String> addRemoveDeleteProductInBasket( @PathVariable(name="id") long id,
                                                    ServerWebExchange exchange  ) {
    return exchange.getFormData()
            .flatMap(formData -> {
              String action = formData.getFirst("action");
              return currentBasketId()
                      .flatMap( currentBasket ->
                            productService.plusMinusDelete(id, currentBasket, action)
                      )
                      .thenReturn("redirect:/cart/items");
            });
  }


  @PostMapping(value = "/main/items/{id}", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE )
  public Mono<String> addRemoveDeleteProductInMain( @PathVariable(name="id") long id,
// does not work !!!                                @RequestParam(name="action") String action
                                                    ServerWebExchange exchange ) {
    return exchange.getFormData()
            .flatMap(formData -> {
              String action = formData.getFirst("action");
              return currentBasketId()
                      .flatMap( currentBasket ->
                             productService.plusMinusDelete(id, currentBasket, action)
                      )
                      .thenReturn("redirect:/main/items");
            });
  }






  @GetMapping("/orders")
  @PreAuthorize("isAuthenticated()")
  public Mono<String> getOrders(Model model) {
    return currentUserId()
            .flatMapMany(userId -> productService.getOrders(userId))
            .flatMap(order -> {
              return productService.getOrderItems(order.getId())
                      .map(Item::new)
                      .collectList()
                      .flatMap(items -> {
                        return productService.getOrderItems(order.getId())
                                .map(oi -> oi.getPrice().multiply(BigDecimal.valueOf(oi.getQuantity())))
                                .reduce(BigDecimal.ZERO, BigDecimal::add)
                                .map(totalSum -> new OrderModel(order.getId(), totalSum, items));
                      });
            })
            .collectList()
            .doOnNext(orderModelList -> {
              model.addAttribute("orders", orderModelList);
              orderModelList.forEach(orderModel -> {
                System.out.println(orderModel.id());
                orderModel.items().forEach(item ->
                        System.out.println("- " + item.getId() + " " + item.getTitle()));
              });
            })
            .thenReturn("orders.html");
  }








  @PostMapping("/buy")
  @PreAuthorize("isAuthenticated()")
  public Mono<Void> postBuy(ServerWebExchange exchange) {

    Mono<Long> userIdMono = currentUserId();
    Mono<BigDecimal> balanceMono = userIdMono.flatMap(userId -> paymentService.getUserBalance(userId))
            .doOnNext(balance -> System.out.println("ProductController:postBuy: balance before payment: " + balance));
    Flux<Item> basketItems = currentBasketId()
            .flatMapMany(productService::getItemsFromBasket);

    Mono<BigDecimal> totalMono = basketItems
            .collectList()  // собираем Flux в Mono<List<Item>>
            .map(items -> items.stream()
                    .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getCount())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
            );

//    Mono<String> orderSignatureMono = productService.getBasketSignature(currentBasket());
    Mono<String> orderSignatureMono = currentBasketId()
            .flatMap( productService::getBasketSignature);



    return Mono.zip(balanceMono, totalMono, userIdMono)
            .flatMap(tuple -> {
              BigDecimal balance = tuple.getT1();
              System.out.println("ProductController: balance = " + balance);
              BigDecimal total = tuple.getT2();
              Long userId = tuple.getT3();

              if (balance.compareTo(new BigDecimal("-1")) == 0) {
                System.out.println("ProductController: payment service unavailable");
                exchange.getResponse().setStatusCode(HttpStatus.SEE_OTHER);
                exchange.getResponse().getHeaders().setLocation(
                        URI.create("/cart/items?error=service_unavailable")
                );
                return Mono.empty();
              }

              if (balance.compareTo(total) < 0 && balance.compareTo(new BigDecimal("-1")) != 0) {
                // Редирект на /cart/items с флагом ошибки
                exchange.getResponse().setStatusCode(HttpStatus.SEE_OTHER);
                exchange.getResponse().getHeaders().setLocation(
                        URI.create("/cart/items?error=insufficient_funds")
                );
                exchange.getAttributes().put("error", "Недостаточно средств");
                return Mono.empty();
              }

              return Mono.zip(userIdMono, totalMono, orderSignatureMono)
                      .flatMap(tuple2 -> {
                        Long userId2 = tuple2.getT1();
                        BigDecimal amount = tuple2.getT2();
                        String signature = tuple2.getT3();
                        return paymentService.makePayment(userId2, amount, signature);
                      })
                      .flatMap(paymentSuccess -> {
                                if (paymentSuccess) {
                                  return currentBasketId()
                                          .flatMap(productService::makeOrder) // makeOrder принимает Long
                                          .flatMap(orderId -> {
                                            exchange.getResponse().setStatusCode(HttpStatus.SEE_OTHER);
                                            exchange.getResponse().getHeaders().setLocation(
                                                    URI.create("/orders/" + orderId)
                                            );
                                            return exchange.getResponse().setComplete();
                                          });
                                }
                                return Mono.empty();
                              }
                      );


            });


  }






  @GetMapping("/orders/{id}")
  @PreAuthorize("isAuthenticated()")
  public Mono<String> getOrder(
          @PathVariable("id") Long orderId,
          @RequestParam(name = "newOrder", required = false, defaultValue = "false") Boolean newOrder,
          Model model) {

    // Получаем ID текущего пользователя и проверяем права
    return currentUserId()
            .flatMap(currentUserId ->
                    productService.findOrderById(orderId)
                            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found")))
                            .flatMap(order -> {
                              if (currentUserId.longValue() != order.getUserId().longValue()) {
                                return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied"));
                              }

                              // Если проверка прошла, продолжаем оригинальную логику
                              return productService.getOrderItems(orderId)
                                      .collectList()
                                      .flatMap(orderItems -> {
                                        Mono<List<Item>> itemsMono = Flux.fromIterable(orderItems)
                                                .map(orderItem -> {
                                                  Item item = new Item(orderItem);
                                                  item.setId(orderItem.getProductId());
                                                  return item;
                                                })
                                                .collectList();

                                        Mono<BigDecimal> totalMono = Mono.just(
                                                orderItems.stream()
                                                        .map(oi -> oi.getPrice().multiply(BigDecimal.valueOf(oi.getQuantity())))
                                                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                                        );

                                        return Mono.zip(itemsMono, totalMono);
                                      })
                                      .map(tuple -> {
                                        List<Item> items = tuple.getT1();
                                        BigDecimal totalSum = tuple.getT2();

                                        record OrderModel(Long id, BigDecimal totalSum, List<Item> items) {}
                                        return new OrderModel(orderId, totalSum, items);
                                      })
                                      .doOnNext(orderModel -> {
                                        model.addAttribute("order", orderModel);
                                        if (newOrder) {
                                          model.addAttribute("newOrder", true);
                                        }
                                      })
                                      .thenReturn("order.html");
                            })
            );
  }








  @GetMapping("/check")
  public Mono<String> getCheck ( Model model ){
    return Mono.just("message.html");
  }

}
