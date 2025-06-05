package javamid.vitrina.controllers;

import javamid.vitrina.model.Item;
import javamid.vitrina.model.Paging;
import javamid.vitrina.dao.Product;
import javamid.vitrina.services.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
public class ProductController {

  private final ProductService productService;
  public ProductController(ProductService productService) {
    this.productService = productService;
  }


  @GetMapping("/")
  public String homePage() { return "redirect:/main/items"; }


  @GetMapping("/main/items")
  public String getItems(
          @RequestParam(name = "search", required = false, defaultValue = "" ) String search,
          @RequestParam(name = "pageSize", defaultValue = "10") int size,
          @RequestParam(name = "pageNumber", defaultValue = "1") int page,
          Model model
          ) {

    //public Page<Product> getProducts(String keyword, int page, int size) {

    Page<Product> productPage = productService.getProducts("",0,10 );

    System.out.println("keyword is: " + search );
    System.out.println("found this products:");
    for( Product product : productPage.getContent()){
      System.out.println("id " + product.getId() );
      System.out.println("name " + product.getName() );
      System.out.println("description " + product.getDescription() );
    }

    List<Item> itemList = new ArrayList<>();
    for( Product product : productPage.getContent() ){
      itemList.add( new Item(product));
    }

    model.addAttribute("items", itemList);

    Paging paging = new Paging();
    paging.setPageNumber(1);
    paging.setPageSize(10);
    paging.setNext(false);
    paging.setPrevious(false);

    model.addAttribute("paging", paging );

    return "main.html";
  }

}
