package javamid.vitrina.dao;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="baskets")
public class Basket {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne
  @JoinColumn(name = "user_id")  // Столбец с внешним ключом в таблице baskets
  private User user;

  @OneToMany(mappedBy = "basket", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY )
  private List<BasketItem> basketItems = new ArrayList<>();


  public void setId(Long id){this.id = id;}
  public void setUser(User user){this.user = user;}
  public void setBasketItems(List<BasketItem> basketItems){this.basketItems = basketItems;}
  public Long getId(){return id;}
  public User getUser(){return user;}
  public List<BasketItem> getBasketItems(){return basketItems;}

}
