package javamid.vitrina.dao;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("baskets")
public class Basket {
  @Id
  private Long id;

  @Column("user_id")
  private Long userId;

  public Basket() {}

  public Basket(Long id, Long userId) {
    this.id = id;
    this.userId = userId;
  }

  public void setId(Long id) { this.id = id; }
  public void setUserId(Long userId) { this.userId = userId; }
  public Long getId() { return id; }
  public Long getUserId() { return userId; }

}