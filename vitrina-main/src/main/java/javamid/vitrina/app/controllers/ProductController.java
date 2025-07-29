package javamid.vitrina.app.controllers;


import javamid.vitrina.app.dao.BasketItem;
import javamid.vitrina.app.model.Item;
import javamid.vitrina.app.model.Paging;
import javamid.vitrina.app.dao.Product;
import javamid.vitrina.app.services.PaymentService;
import javamid.vitrina.app.services.ProductService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

  public Long currentBasket() { return 1L; }
  public Long currentUser() { return 1L; }
  record OrderModel(Long id, BigDecimal totalSum, List<Item> items) {}

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
    Flux<BasketItem> basketItemFlux = productService.getBasketItems(currentBasket());

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
//    Mono<Item> itemMono = productService.getProductItem(id, currentBasket())
    return productService.getProductItem(id, currentBasket())
            .doOnNext(item -> {
              model.addAttribute("item", item);
//              System.out.println("Item = " + item.getDescription());
            })
            .doOnError(e -> System.err.println("Error: " + e)) // Добавлено
            .thenReturn("item.html")
            .switchIfEmpty(Mono.defer(() -> {
              System.out.println("Item not found for id: " + id); // Добавлено
              return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND));
            }));

  }

  @GetMapping("/cart/items")
  public Mono<String> getBasket( Model model ){


    Mono<Long> userIdMono = productService.findUserIdByBasketId( currentBasket() );
/*
    userIdMono.flatMap(userId -> paymentService.getUserBalance(userId))
            .doOnNext(balance -> System.out.println("Текущий баланс: " + balance))
            .subscribe();
 */
    
    Mono<BigDecimal> balanceMono = userIdMono.flatMap( userId->paymentService.getUserBalance(userId));

    Flux<Item> basketItems = productService.getItemsFromBasket( currentBasket() ) ;

    return basketItems
            .collectList()  // собираем Flux в Mono<List<Item>>
            .doOnNext(items -> {
              BigDecimal total = items.stream()
                      .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getCount())))
                      .reduce(BigDecimal.ZERO, BigDecimal::add);
              model.addAttribute("items", items);
              model.addAttribute("empty", items.isEmpty());
              model.addAttribute("total", total);
            })
            .thenReturn("cart.html");
  }


// три раза одинаковый код. костыль потому что не получается  @RequestParam(name="action")
  @PostMapping(value = "/items/{id}", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE )
  public Mono<String> addRemoveDeleteProductInProductItem( @PathVariable(name="id") long id,
                                                      ServerWebExchange exchange  ) {
    return exchange.getFormData()
            .flatMap(formData -> {
              String action = formData.getFirst("action");
              return productService.plusMinusDelete(id, currentBasket(), action)
                      .thenReturn("redirect:/items/" + id );
            });
  }
  @PostMapping(value = "/cart/items/{id}", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE )
  public Mono<String> addRemoveDeleteProductInBasket( @PathVariable(name="id") long id,
                                                    ServerWebExchange exchange  ) {
    return exchange.getFormData()
            .flatMap(formData -> {
              String action = formData.getFirst("action");
              return productService.plusMinusDelete(id, currentBasket(), action)
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
              return productService.plusMinusDelete(id, currentBasket(), action)
                      .thenReturn("redirect:/main/items");
            });
  }




  @GetMapping("/orders")
  public Mono<String> getOrders(Model model) {
    return productService.getOrders(currentUser())
            .flatMap(order -> {
              return productService.getOrderItems(order.getId())
                      .map(orderItem -> new Item(orderItem))
                      .collectList()
                      .flatMap(items -> {
                        return productService.getOrderItems(order.getId()) // Повторный запрос, но теперь для суммы
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

/*
  @PostMapping("/buy")
  public Mono<Void> postBuy(ServerWebExchange exchange) {

    return productService.makeOrder( currentBasket() )
            .flatMap(orderId -> {
              System.out.println("Buy id: " + orderId);

              // 1. Добавляем ID в путь (аналог addAttribute)
              String redirectUrl = "/orders/" + orderId;

              // 2. Добавляем flash-атрибут (аналог addFlashAttribute)
              exchange.getAttributes().put("newOrder", true);

              // 3. Делаем редирект
              return Mono.fromRunnable(() -> {
                exchange.getResponse().setStatusCode(HttpStatus.SEE_OTHER);
                exchange.getResponse().getHeaders().setLocation(URI.create(redirectUrl));
              });
            });
  }

*/


  @PostMapping("/buy")
  public Mono<Void> postBuy(ServerWebExchange exchange) {

    Mono<Long> userIdMono = productService.findUserIdByBasketId( currentBasket() );

    Mono<BigDecimal> balanceMono =  userIdMono
            .flatMap(userId->paymentService.getUserBalance(userId));

    Flux<Item> basketItems = productService.getItemsFromBasket( currentBasket() ) ;

    Mono<BigDecimal> totalMono = basketItems
            .collectList()  // собираем Flux в Mono<List<Item>>
            .map(items -> items.stream()
                    .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getCount())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
            );


    return Mono.zip(balanceMono, totalMono, userIdMono)
            .flatMap(tuple -> {
              BigDecimal balance = tuple.getT1();
              System.out.println("ProductController: balance = " + balance );
              BigDecimal total = tuple.getT2();
              Long userId = tuple.getT3();

              if (balance.compareTo(new BigDecimal("-1")) == 0) {
                System.out.println("ProductController: payment service unavailable" );
                exchange.getResponse().setStatusCode(HttpStatus.SEE_OTHER);
                exchange.getResponse().getHeaders().setLocation(
                        URI.create("/cart/items?error=service_unavailable")
                );
                return Mono.empty();
              }

              if (balance.compareTo(total) < 0 && balance.compareTo(new BigDecimal("-1")) != 0 ) {
                // Редирект на /cart/items с флагом ошибки
                exchange.getResponse().setStatusCode(HttpStatus.SEE_OTHER);
                exchange.getResponse().getHeaders().setLocation(
                        URI.create("/cart/items?error=insufficient_funds")
                );
                exchange.getAttributes().put("error", "Недостаточно средств");
                return Mono.empty();
              }

              // Если баланс OK - создаем заказ
              return productService.makeOrder(currentBasket())
                      .flatMap(orderId -> {
                        exchange.getResponse().setStatusCode(HttpStatus.SEE_OTHER);
                        exchange.getResponse().getHeaders().setLocation(
                                URI.create("/orders/" + orderId)
                        );
                        return Mono.empty();
                      });
            });



  }









  @GetMapping("/orders/{id}")
  public Mono<String> getOrder(
          @PathVariable("id") Long orderId,
          @RequestParam(name = "newOrder", required = false, defaultValue = "false") Boolean newOrder,
          Model model) {

    return productService.getOrderItems(orderId)
            .collectList()
            .flatMap(orderItems -> {
              // Параллельно преобразуем в Items и считаем сумму
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
  }





}
