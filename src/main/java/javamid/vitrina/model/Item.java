package javamid.vitrina.model;

import javamid.vitrina.dao.OrderItem;
import javamid.vitrina.dao.Product;

import java.math.BigDecimal;
import java.math.BigInteger;

public class Item {

  private Long id;
  private String title;
  private BigDecimal price;
  private String description;
  private int count;
  private String imgPath = "0";


  public void setId( Long id ) { this.id = id; }
  public void setTitle( String title ) { this.title = title; }
  public void setPrice( BigDecimal price ) { this.price = price; }
  public void setDescription( String description ) { this.description = description; }
  public void setCount( int count ){ this.count = count; }
//  public void setIngPath( String imgPath ) { this.imgPath = imgPath; }
  public void setImgPath( Long id ) { this.imgPath = id.toString(); }

  public Long getId() { return id; }
  public String getTitle(){ return title; }
  public BigDecimal getPrice() { return price; }
  public String getDescription() { return description; }
  public int getCount(){ return count; }
  public String getImgPath(){ return imgPath; }


  public Item(){}

  public Item( Product product ){
    this.id = product.getId();
    this.title = product.getName();
    this.price = new BigDecimal( product.getPrice().toString() );
    this.description = product.getDescription();
    this.count = 0;
    if ( product.getImage() != null ) this.imgPath = product.getId().toString();
  }

  public Item( OrderItem orderItem ){
    this.id = orderItem.getId();
    this.title = orderItem.getName();
    this.price = new BigDecimal( orderItem.getPrice().toString() );
    this.description = "";
    this.count = orderItem.getQuantity();
    if ( orderItem.getImage() != null ) this.imgPath = orderItem.getId().toString();
  }


}
