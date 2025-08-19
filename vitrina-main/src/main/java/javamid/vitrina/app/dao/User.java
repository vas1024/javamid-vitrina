package javamid.vitrina.app.dao;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;


@Table(name="users")
public class User implements UserDetails  {
  @Id
  private Long id;

  private String name;
  private String login;
  private String password;

  @Column("basket_id")
  private Long basketId;

  public User () {}
  public User ( Long id, String name, String login, String password ) {
    this.id = id;
    this.name = name;
    this.login = login;
    this.password = password;
  }


  public void setId(Long id){this.id = id;}
  public void setName(String name){this.name = name;}
  public void setLogin(String login){this.login = login;}
  public void setPassword(String password){this.password = password;}
  public void setBasketId(Long basketId){this.basketId = basketId;}
  public Long getId(){return id;}
  public String getName(){return name;}
  public String getLogin(){return login;}
  public String getPassword(){return password;}
  public Long getBasketId(){return basketId;}

  // Реализация методов UserDetails

  public String getUsername(){return login;}

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    // Возвращаем коллекцию с одной дефолтной ролью "USER"
    if( "admin".equals(login) ) return Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"));
    else
    return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

}
