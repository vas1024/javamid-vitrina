package javamid.vitrina.app.services;

import jakarta.annotation.PostConstruct;
import javamid.vitrina.app.restclient.ApiClient;
import javamid.vitrina.app.restclient.api.BalanceApi;
import javamid.vitrina.app.restclient.api.PaymentApi;
import javamid.vitrina.app.restclient.model.PaymentRequest;
import javamid.vitrina.app.restclient.model.PaymentResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.math.BigDecimal;
import java.time.Duration;


@Service
public class PaymentService {
  private final PaymentApi paymentApi;
  private final BalanceApi balanceApi;


  public PaymentService(@Value("${api.payment.base-url}") String apiBaseUrl) {
    ApiClient apiClient = new ApiClient();
    apiClient.setBasePath(apiBaseUrl);
    System.out.println( "Api base url " + apiBaseUrl );
    this.paymentApi = new PaymentApi(apiClient);
    this.balanceApi = new BalanceApi(apiClient);
  }


  public Mono<PaymentResponse> processPayment(PaymentRequest request) {
    return paymentApi.paymentPost(request);
  }

/*
  public Mono<BigDecimal> getUserBalance(Long userId) {
    return balanceApi.paymentBalanceUserIdGet(userId.toString())
            .map(balance -> BigDecimal.valueOf(balance.getAmount()))
            .onErrorResume(e -> Mono.just(BigDecimal.ZERO) );
  }
*/

  public Mono<BigDecimal> getUserBalance(Long userId) {
    return balanceApi.paymentBalanceUserIdGet(userId.toString())
            .map(balance -> BigDecimal.valueOf(balance.getAmount()))
            .timeout(Duration.ofSeconds(1))
            .retryWhen(Retry.backoff(3, Duration.ofMillis(100)) )
            .onErrorResume(e -> {
              System.out.println("Сервис баланса недоступен. Возвращаем значение по умолчанию (-1)");
              return Mono.just(new BigDecimal("-1"));
            })
            .defaultIfEmpty(new BigDecimal("-1"));
  }


}

