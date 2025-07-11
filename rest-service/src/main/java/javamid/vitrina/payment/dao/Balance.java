package javamid.vitrina.payment.dao;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Table("balance")
public class Balance {
    @Id
    private Long id;

    @Column("user_id")
    private Long userId;

    @Column("amount")
    private BigDecimal amount;

    public Balance() {}

    public Balance(Long id, Long userId, BigDecimal amount) {
      this.id = id;
      this.userId = userId;
      this.amount = amount;
    }

    public void setId(Long id) { this.id = id; }
    public void setUserId(Long userId) { this.userId = userId; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public BigDecimal getAmount() { return amount; }

  }
