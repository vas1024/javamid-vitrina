package javamid.vitrina.app.dao;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Table("order_items")
public class OrderItem {
  @Id
  private Long id;
  @Column("order_id")
  private Long orderId;
  @Column("product_id")
  private Long productId;
  private String name;
  private byte[] image;
  private BigDecimal price;
  private int quantity;


  public OrderItem() {}

  public OrderItem(Long id, Long orderId, Long productId, String name, byte[] image, BigDecimal price, int quantity) {
    this.id = id;
    this.orderId = orderId;
    this.productId = productId;
    this.name = name;
    this.image = image;
    this.price = price;
    this.quantity = quantity;
  }

  public void setId(Long id) { this.id = id; }
  public void setOrderId(Long orderId) { this.orderId = orderId; }
  public void setProductId(Long productId) { this.productId = productId; }
  public void setName(String name) { this.name = name; }
  public void setImage(byte[] image) { this.image = image; }
  public void setPrice(BigDecimal price) { this.price = price; }
  public void setQuantity(int quantity) { this.quantity = quantity; }
  public Long getId() { return id; }
  public Long getOrderId() { return orderId; }
  public Long getProductId() { return productId; }
  public String getName() { return name; }
  public byte[] getImage() { return image; }
  public BigDecimal getPrice() { return price; }
  public int getQuantity() { return quantity; }

}

