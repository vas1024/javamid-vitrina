package javamid.vitrina.payment.repository;

import javamid.vitrina.payment.dao.Payment;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;


public interface PaymentRepository extends ReactiveCrudRepository<Payment, Long> {

  // Стандартные методы уже включены в ReactiveCrudRepository:
  // save(), saveAll(), findById(), findAll(), deleteById(), deleteAll() и т.д.

  // Добавляем кастомные запросы









}
