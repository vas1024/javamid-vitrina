package javamid.vitrina.payment.controller;

import javamid.vitrina.payment.api.BalanceApi;
import javamid.vitrina.payment.repository.BalanceRepository;
import javamid.vitrina.payment.model.Balance;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.annotation.Generated;

import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2025-07-09T19:04:40.667526100+03:00[Europe/Moscow]", comments = "Generator version: 7.12.0")
@Controller
@RequestMapping("${openapi.paymentService.base-path:}")
public class BalanceApiController implements BalanceApi {
  private final BalanceRepository balanceRepository;

  @Autowired
  public BalanceApiController(BalanceRepository balanceRepository) {
    this.balanceRepository = balanceRepository;
  }

  @Override
  public Mono<ResponseEntity<Balance>> paymentBalanceUserIdGet(
          @PathVariable("userId") String userIdStr,
          ServerWebExchange exchange
  ) {

    long userId;
    try {
      userId = Long.parseLong(userIdStr);
    } catch (NumberFormatException e) {
      return Mono.just(ResponseEntity.badRequest().build());
    }
    return balanceRepository.findByUserId(userId)
            .map(this::convertDaoToDto)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
  }

  private Balance convertDaoToDto( javamid.vitrina.payment.dao.Balance balanceDao){
    return new Balance()
            .userId(String.valueOf( balanceDao.getUserId() ) )
            .amount(balanceDao.getAmount().doubleValue());
  }

}
