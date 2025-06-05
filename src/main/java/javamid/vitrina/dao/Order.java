package javamid.vitrina.dao;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="orders")
public class Order {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "user_id") // ← Здесь уже указывается имя колонки в БД
  private User user; // ← Это то самое поле, на которое ссылается mappedBy

  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<OrderItem> orderItems = new ArrayList<>();

  public void setId(Long id){this.id = id;}
  public void setUser(User user){this.user = user;}
  public void setOrderItems(List<OrderItem> orderItems){this.orderItems = orderItems;}
  public Long getId(){return id;}
  public User getUser(){return user;}
  public List<OrderItem> getOrderItems(){return orderItems;}

}
