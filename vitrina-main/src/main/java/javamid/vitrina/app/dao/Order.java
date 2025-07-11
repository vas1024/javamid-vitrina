package javamid.vitrina.app.dao;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("orders")
public class Order {
  @Id
  private Long id;
  @Column("user_id")
  private Long userId;

  public Order() {}

  public Order(Long id, Long userId) {
    this.id = id;
    this.userId = userId;
  }

  public void setId(Long id) { this.id = id;  }
  public void setUserId(Long userId) { this.userId = userId;  }
  public Long getId() { return id; }
  public Long getUserId() { return userId; }

}
