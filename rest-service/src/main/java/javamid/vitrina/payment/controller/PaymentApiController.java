package javamid.vitrina.payment.controller;

import javamid.vitrina.payment.dao.Balance;
import javamid.vitrina.payment.dao.Payment;
import javamid.vitrina.payment.model.PaymentRequest;
import javamid.vitrina.payment.model.PaymentResponse;
import javamid.vitrina.payment.api.PaymentApi;

import javamid.vitrina.payment.repository.BalanceRepository;
import javamid.vitrina.payment.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.context.request.NativeWebRequest;

import jakarta.validation.constraints.*;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import jakarta.annotation.Generated;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2025-07-10T15:00:19.514954900+03:00[Europe/Moscow]", comments = "Generator version: 7.12.0")
@Controller
@RequestMapping("${openapi.paymentService.base-path:}")
public class PaymentApiController implements PaymentApi {
  private final BalanceRepository balanceRepository;
  private final PaymentRepository paymentRepository;
  @Autowired
  public PaymentApiController(BalanceRepository balanceRepository,
                              PaymentRepository paymentRepository) {
    this.balanceRepository = balanceRepository;
    this.paymentRepository = paymentRepository;
  }


  @Override
  public Mono<ResponseEntity<PaymentResponse>> paymentPost(
          @Valid @RequestBody Mono<PaymentRequest> paymentRequest,
          ServerWebExchange exchange) {

    return paymentRequest.flatMap(request -> {
      BigDecimal amount = new BigDecimal(request.getAmount().toString());
      Long userId = Long.parseLong(request.getUserId());

      return balanceRepository.findByUserId(userId)
              .switchIfEmpty(Mono.error(new RuntimeException("Balance not found")))
              .flatMap(balance -> {
                BigDecimal newAmount = balance.getAmount().subtract(amount);

                if (newAmount.compareTo(BigDecimal.ZERO) < 0) {
                  return Mono.error(new RuntimeException("Insufficient funds"));
                }

                balance.setAmount(newAmount);
                return balanceRepository.save(balance)
                        .flatMap(savedBalance -> {
                          Payment payment = new Payment();
                          payment.setUserId(userId);
                          payment.setAmount(amount);
                          payment.setOrderSignature(request.getOrderSignature());
                          payment.setDateTime(OffsetDateTime.now());
                          return paymentRepository.save(payment);
                        });

              })
              .map(payment -> ResponseEntity.ok(
                      new PaymentResponse()
                              .status(PaymentResponse.StatusEnum.SUCCESS)
                              .paymentId(payment.getId().toString())
                              .dateTime(payment.getDateTime())
              ))
              .onErrorResume(e -> Mono.just(
                      ResponseEntity.badRequest().body(
                              new PaymentResponse()
                                      .status(PaymentResponse.StatusEnum.FAILED)
                      )
              ));
    }).switchIfEmpty(Mono.just(ResponseEntity.badRequest().build()));
  }


}
