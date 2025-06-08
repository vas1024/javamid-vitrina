package javamid.vitrina.dao;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name="order_items")
public class OrderItem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String name;
  private byte[] image;
  private BigDecimal price;
  private int quantity;
  private Long productId;

  @ManyToOne ( fetch = FetchType.EAGER )
  @JoinColumn(name = "order_id") // ← Здесь уже указывается имя колонки в БД
  private Order order; // ← Это то самое поле, на которое ссылается mappedBy




  public void setId(Long id){this.id = id;}
  public void setName(String name){this.name = name;}
  public void setImage(byte[] image){this.image = image;}
  public void setPrice(BigDecimal price){this.price = price;}
  public void setOrder(Order order){this.order = order;}
  public void setQuantity(int quantity) {this.quantity = quantity;}
  public void setProductId(Long productId ){ this.productId = productId; }
  public Long getId(){return id;}
  public String getName(){return name;}
  public byte[] getImage() {return image;}
  public BigDecimal getPrice() {return price;}
  public Order getOrder() {return order;}
  public int getQuantity() { return quantity;}
  public Long getProductId() {return productId; }
}

