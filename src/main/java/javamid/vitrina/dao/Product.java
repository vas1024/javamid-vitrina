package javamid.vitrina.dao;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name="products")
public class Product {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
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

