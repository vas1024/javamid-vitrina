package javamid.vitrina.payment.dao;


import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Table("payments")
public class Payment {
  @Id
  private Long id;

  @Column("user_id")
  private Long userId;

  @Column("amount")
  private BigDecimal amount;

  @Column("created_at")
  private OffsetDateTime dateTime;

  @Column("order_signature")
  private String orderSignature;


  public Payment() {}

  public Payment(Long id, Long userId, BigDecimal amount, OffsetDateTime dateTime, String orderSignature ) {
    this.id = id;
    this.userId = userId;
    this.amount = amount;
    this.dateTime = dateTime;
    this.orderSignature = orderSignature;
  }

  public void setId(Long id) { this.id = id; }
  public void setUserId(Long userId) { this.userId = userId; }
  public void setAmount(BigDecimal amount) { this.amount = amount; }
  public void setDateTime( OffsetDateTime dateTime ) { this.dateTime = dateTime; }
  public void setOrderSignature( String orderSignature ) { this.orderSignature = orderSignature; }
  public Long getId() { return id; }
  public Long getUserId() { return userId; }
  public BigDecimal getAmount() { return amount; }
  public OffsetDateTime getDateTime() { return  dateTime;}
  public String getOrderSignature() { return  orderSignature; }

}
