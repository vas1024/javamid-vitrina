package javamid.vitrina.dao;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="users")
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;

  @OneToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "basket_id", referencedColumnName = "id")
  private Basket basket;

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Order> orders = new ArrayList<>();

  public void setId(Long id){this.id = id;}
  public void setName(String name){this.name = name;}
  public void setBasket(Basket basket){this.basket = basket;}
  public void setOrders(List<Order> orders){this.orders=orders;}
  public Long getId(){return id;}
  public String getName(){return name;}
  public Basket getBasket(){return basket;}
  public List<Order> getOrders(){return orders;}
}
