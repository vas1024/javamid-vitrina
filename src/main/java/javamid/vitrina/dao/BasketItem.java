package javamid.vitrina.dao;

import jakarta.persistence.*;

@Entity
//@Table(name="basket_items")
public class BasketItem {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne( fetch = FetchType.LAZY)
  @JoinColumn(name = "basket_id")
  private Basket basket;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id")
  private Product product;

  private int quantity;

  public void setId(Long id){this.id = id;}
  public void setBasket(Basket basket){this.basket = basket;}
  public void setProduct(Product product){this.product = product;}
  public void setQuantity(int quantity){this.quantity = quantity;}
  public Long getId(){return id;}
  public Basket getBasket(){return basket;}
  public Product getProduct(){return product;}
  public int getQuantity(){return  quantity;}

}
