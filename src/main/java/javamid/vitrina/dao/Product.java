package javamid.vitrina.dao;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import java.math.BigDecimal;

@Table(name="products")
public class Product {

  @Id
  private Long id;
  private String name;
  private byte[] image;
  private String description;
  private BigDecimal price;

  public Product() {}  // для тимьяна

  public void setId(Long id){ this.id = id; }
  public void setName(String name){ this.name = name; }
  public void setImage(byte[] image){ this.image = image; }
  public void setDescription(String description){ this.description = description; }
  public void setPrice(BigDecimal price){ this.price = price; }

  public Long getId(){ return id; }
  public String getName(){ return name; }
  public byte[] getImage(){ return image; }
  public String getDescription(){ return description; }
  public BigDecimal getPrice(){ return price; }

}

