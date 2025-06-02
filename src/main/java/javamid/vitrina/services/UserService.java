package javamid.vitrina.services;


import jakarta.transaction.Transactional;
import javamid.vitrina.model.*;
import javamid.vitrina.repositories.BasketRepository;
import javamid.vitrina.repositories.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {
  private final UserRepository userRepository;
  private final BasketRepository basketRepository;

  UserService( UserRepository userRepository, BasketRepository basketRepository ) {
    this.userRepository = userRepository;
    this.basketRepository = basketRepository;
  }

  @Transactional
  public User createUserWithBasket(String username) {
    Basket basket = new Basket();
    basketRepository.save(basket);

    User user = new User();
    user.setName(username);
    user.setBasket(basket);
    basket.setUser(user);

    return userRepository.save(user);
  }
}
