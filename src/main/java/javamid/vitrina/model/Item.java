package javamid.vitrina.model;

import javamid.vitrina.dao.Product;

import java.math.BigDecimal;
import java.math.BigInteger;

public class Item {

  private Long id;
  private String title;
  private BigDecimal price;
  private String description;
  private int count;

  public Long getId() { return id; }
  public String getTitle(){ return title; }
  public BigDecimal getPrice() { return price; }
  public String getDescription() { return description; }
  public int getCount(){ return count; }

  public Item( Product product ){
    this.id = product.getId();
    this.title = product.getName();
    this.price = new BigDecimal( product.getPrice().toString() );
    this.description = product.getDescription();
    this.count = 0;
  }

}
