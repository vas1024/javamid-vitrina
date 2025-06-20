package javamid.vitrina.controllers;


import javamid.vitrina.dao.Basket;
import javamid.vitrina.dao.BasketItem;
import javamid.vitrina.model.Item;
import javamid.vitrina.model.Paging;
import javamid.vitrina.dao.Product;
import javamid.vitrina.dao.Order;
import javamid.vitrina.dao.OrderItem;
import javamid.vitrina.services.ProductService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.util.Pair;



import javamid.vitrina.services.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Mono;

@Controller
public class ProductController {

  private final ProductService productService;

  public ProductController(ProductService productService) {
    this.productService = productService;

  }


}




  /*

  @GetMapping("/{id}")
  public Mono<String> getUserById(@PathVariable Long id, Model model) {
    return orderService.findById(id)
            .doOnNext(order -> model.addAttribute("order", order)) // Передаём готовый объект в модель
            .map(order -> "order") // Возвращаем order.html
            .defaultIfEmpty("not-found"); // Если Mono<Order> пустой, то отдаём not-found.html
  }
}

   */





/*
@Controller
@RequestMapping("/products")
public class ProductController {

  private final ProductService productService;
  private final UserService userService;

  public ProductController(ProductService productService,
                                   UserService userService) {
    this.productService = productService;
    this.userService = userService;
  }

  // Получение корзины с актуальными данными
  private Mono<Basket> getBasketData() {
    return productService.getBasketById(1L)
            .flatMap(basket -> {
              // Получаем элементы корзины отдельным запросом
              return productService.getBasketItemsByBasketId(basket.getId())
                      .collectList()
                      .flatMap(items -> {
                        // Получаем полные данные о продуктах
                        return Flux.fromIterable(items)
                                .flatMap(basketItem ->
                                        productService.getProductById(basketItem.getProductId())
                                                .map(product -> new Pair<>(product, basketItem.getQuantity()))
                                )
                                .collectList()
                                .map(productQuantityPairs -> {
                                  Map<Long, Integer> productsMap = new HashMap<>();
                                  BigDecimal total = BigDecimal.ZERO;

                                  for (Pair<Product, Integer> pair : productQuantityPairs) {
                                    Product product = pair.getFirst();
                                    Integer quantity = pair.getSecond();
                                    productsMap.put(product.getId(), quantity);
                                    total = total.add(product.getPrice()
                                            .multiply(BigDecimal.valueOf(quantity)));
                                  }

                                  return new Basket(basket, productsMap, total);
                                });
                      });
            });
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

    return productService.getProductsReactive(keyword, sort, page - 1, size)
            .flatMap(productPage -> {
              return Flux.fromIterable(productPage.getContent())
                      .flatMap(product -> {
                        return getCurrentBasketData()
                                .map(basketData -> {
                                  Map<Long, Integer> productsInBasket = (Map<Long, Integer>) basketData.get("productsInBasket");
                                  Item item = new Item(product);
                                  item.setCount(productsInBasket.getOrDefault(product.getId(), 0));
                                  return item;
                                });
                      })
                      .collectList()
                      .map(itemList -> {
                        model.addAttribute("items", itemList);

                        Paging paging = new Paging();
                        paging.setPageNumber(productPage.getNumber() + 1);
                        paging.setPageSize(productPage.getSize());
                        paging.setNext(productPage.hasNext());
                        paging.setPrevious(productPage.hasPrevious());

                        model.addAttribute("paging", paging);
                        model.addAttribute("search", keyword);
                        model.addAttribute("sort", sort);
                        return "main";
                      });
            });
  }

  @GetMapping("/images/{id}")
  public Mono<ResponseEntity<byte[]>> getImage(@PathVariable Long id) {
    return productService.getImageByProductIdReactive(id)
            .map(imageData -> ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .contentLength(imageData.length)
                    .header(HttpHeaders.CACHE_CONTROL, "no-transform")
                    .body(imageData));
  }

  @GetMapping("/orderimages/{id}")
  public Mono<ResponseEntity<byte[]>> getOrderImage(@PathVariable Long id) {
    return productService.getImageByOrderItemIdReactive(id)
            .map(imageData -> ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .contentLength(imageData.length)
                    .header(HttpHeaders.CACHE_CONTROL, "no-transform")
                    .body(imageData));
  }

  @GetMapping("/items/{id}")
  public Mono<String> getItem(@PathVariable Long id, Model model) {
    return productService.getProductByIdReactive(id)
            .zipWith(getCurrentBasketData())
            .map(tuple -> {
              Product product = tuple.getT1();
              Map<Long, Integer> productsInBasket = (Map<Long, Integer>) tuple.getT2().get("productsInBasket");

              Item item = new Item(product);
              item.setCount(productsInBasket.getOrDefault(id, 0));
              model.addAttribute("item", item);
              return "item";
            });
  }

  private Mono<Void> handleBasketAction(Long productId, String action) {
    return getCurrentBasketData()
            .flatMap(basketData -> {
              Basket basket = (Basket) basketData.get("basket");
              return switch (action) {
                case "plus" -> productService.addProductToBasketReactive(productId, basket.getId());
                case "minus" -> productService.removeProductFromBasketReactive(productId, basket.getId());
                case "delete" -> productService.dropProductFromBasketReactive(productId, basket.getId());
                default -> Mono.empty();
              };
            });
  }

  @PostMapping("/items/{id}")
  public Mono<String> handleProductItemAction(
          @PathVariable Long id,
          @RequestParam String action,
          Model model) {
    return handleBasketAction(id, action)
            .thenReturn("redirect:/items/" + id);
  }

  @PostMapping("/cart/items/{id}")
  public Mono<String> handleBasketItemAction(
          @PathVariable Long id,
          @RequestParam String action,
          Model model) {
    return handleBasketAction(id, action)
            .thenReturn("redirect:/cart/items");
  }

  @PostMapping("/main/items/{id}")
  public Mono<String> handleMainPageItemAction(
          @PathVariable Long id,
          @RequestParam String action,
          Model model) {
    return handleBasketAction(id, action)
            .thenReturn("redirect:/main/items");
  }

  @GetMapping("/cart/items")
  public Mono<String> getBasket(Model model) {
    return getCurrentBasketData()
            .map(basketData -> {
              Basket basket = (Basket) basketData.get("basket");
              List<Item> itemList = basket.getBasketItems().stream()
                      .map(basketItem -> {
                        Item item = new Item(basketItem.getProduct());
                        item.setCount(basketItem.getQuantity());
                        return item;
                      })
                      .toList();

              model.addAttribute("items", itemList);
              model.addAttribute("empty", itemList.isEmpty());
              model.addAttribute("total", basketData.get("total"));
              return "cart";
            });
  }

  @GetMapping("/orders")
  public Mono<String> getOrders(Model model) {
    return getCurrentBasketData()
            .flatMap(basketData -> {
              Basket basket = (Basket) basketData.get("basket");
              return productService.findAllOrdersReactive(basket.getId())
                      .collectList()
                      .map(orders -> {
                        List<OrderModel> orderModels = orders.stream()
                                .map(order -> {
                                  BigDecimal total = order.getOrderItems().stream()
                                          .map(oi -> oi.getPrice().multiply(BigDecimal.valueOf(oi.getQuantity())))
                                          .reduce(BigDecimal.ZERO, BigDecimal::add);

                                  List<Item> items = order.getOrderItems().stream()
                                          .map(Item::new)
                                          .toList();

                                  return new OrderModel(order.getId(), total, items);
                                })
                                .toList();

                        model.addAttribute("orders", orderModels);
                        return "orders";
                      });
            });
  }

  @GetMapping("/orders/{id}")
  public Mono<String> getOrder(
          @PathVariable Long orderId,
          @RequestParam(name = "newOrder", required = false, defaultValue = "false") Boolean newOrder,
          Model model) {

    return productService.findOrderByIdReactive(orderId)
            .map(order -> {
              BigDecimal total = order.getOrderItems().stream()
                      .map(oi -> oi.getPrice().multiply(BigDecimal.valueOf(oi.getQuantity())))
                      .reduce(BigDecimal.ZERO, BigDecimal::add);

              List<Item> items = order.getOrderItems().stream()
                      .map(oi -> {
                        Item item = new Item(oi);
                        item.setId(oi.getProductId());
                        return item;
                      })
                      .toList();

              model.addAttribute("order", new OrderModel(orderId, total, items));
              return "order";
            });
  }

  @PostMapping("/buy")
  public Mono<String> postBuy(RedirectAttributes redirectAttributes) {
    return getCurrentBasketData()
            .flatMap(basketData -> {
              Basket basket = (Basket) basketData.get("basket");
              return productService.makeOrderReactive(basket);
            })
            .flatMap(orderId -> {
              redirectAttributes.addAttribute("id", orderId);
              redirectAttributes.addFlashAttribute("newOrder", true);
              return Mono.just("redirect:/orders/" + orderId);
            });
  }

  // Вспомогательная record для заказов
  record OrderModel(Long id, BigDecimal totalSum, List<Item> items) {}
}

 */