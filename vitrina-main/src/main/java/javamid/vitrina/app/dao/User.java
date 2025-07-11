package javamid.vitrina.app.dao;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.mapping.Column;


@Table(name="users")
public class User {
  @Id
  private Long id;

  private String name;

  @Column("basket_id")  // Теперь храним только ID корзины
  private Long basketId;

  public User () {}

  public void setId(Long id){this.id = id;}
  public void setName(String name){this.name = name;}
  public void setBasketId(Long basketId){this.basketId = basketId;}
  public Long getId(){return id;}
  public String getName(){return name;}
  public Long getBasketId(){return basketId;}
}
