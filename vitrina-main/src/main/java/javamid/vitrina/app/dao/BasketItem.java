package javamid.vitrina.app.dao;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("basket_item")
public class BasketItem {
  @Id
  private Long id;

  @Column("basket_id")
  private Long basketId;
  @Column("product_id")
  private Long productId;
  private int quantity;


  public BasketItem() {}

  public BasketItem(Long id, Long basketId, Long productId, int quantity) {
    this.id = id;
    this.basketId = basketId;
    this.productId = productId;
    this.quantity = quantity;
  }

  public void setId(Long id) { this.id = id; }
  public void setBasketId(Long basketId) { this.basketId = basketId; }
  public void setProductId(Long productId) { this.productId = productId; }
  public void setQuantity(int quantity) { this.quantity = quantity; }
  public Long getId() { return id; }
  public Long getBasketId() { return basketId; }
  public Long getProductId() { return productId; }
  public int getQuantity() { return quantity; }

}
