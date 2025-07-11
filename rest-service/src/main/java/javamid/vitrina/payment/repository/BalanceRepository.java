package javamid.vitrina.payment.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import javamid.vitrina.payment.dao.Balance;


public interface BalanceRepository extends ReactiveCrudRepository<Balance, Long> {

  // Стандартные методы уже включены в ReactiveCrudRepository:
  // save(), saveAll(), findById(), findAll(), deleteById(), deleteAll() и т.д.

  // Добавляем кастомные запросы
  Mono<Balance> findByUserId(Long userId);








}
