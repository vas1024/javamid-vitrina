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

import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.security.oauth2.client.AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;




@Service
public class PaymentService {
  private final PaymentApi paymentApi;
  private final BalanceApi balanceApi;
  private final WebClient webClient;

  public PaymentService(
          @Value("${api.payment.base-url}") String apiBaseUrl,
          ReactiveClientRegistrationRepository clientRegistrationRepository,
          ReactiveOAuth2AuthorizedClientService authorizedClientService) {

    // Проверяем, есть ли регистрации клиентов
    System.out.println("API Base URL: " + apiBaseUrl);
    clientRegistrationRepository.findByRegistrationId("keycloak")
            .doOnNext(reg -> System.out.println("Keycloak client found: " + reg.getClientId()))
            .doOnError(e -> System.out.println("Keycloak client not found: " + e.getMessage()))
            .subscribe();

    // Создаем OAuth2 менеджер
    ReactiveOAuth2AuthorizedClientManager authorizedClientManager =
            new AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(
                    clientRegistrationRepository, authorizedClientService);

    // Создаем WebClient с OAuth2 поддержкой
    this.webClient = WebClient.builder()
            .baseUrl(apiBaseUrl)
            .filter(new ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager))
            .build();

    // Создаем ApiClient с нашим WebClient
    ApiClient apiClient = new ApiClient(this.webClient);
    apiClient.setBasePath(apiBaseUrl);

    this.paymentApi = new PaymentApi(apiClient);
    this.balanceApi = new BalanceApi(apiClient);
  }



  public Mono<PaymentResponse> processPayment(PaymentRequest request) {
    return paymentApi.paymentPost(request);
  }




  public Mono<BigDecimal> getUserBalance(Long userId) {
    return balanceApi.paymentBalanceUserIdGet(userId.toString())
            .map(balance -> BigDecimal.valueOf(balance.getAmount()))
            .timeout(Duration.ofSeconds(3))
            .retryWhen(Retry.backoff(3, Duration.ofMillis(100))
                    .doBeforeRetry(retry -> {
                      System.out.println("Попытка повторного запроса #" + retry.totalRetries());
                    }))
            .doOnSubscribe(sub -> System.out.println("Запрос баланса для user: " + userId))
            .doOnSuccess(balance -> System.out.println("Баланс получен: " + balance))
            .doOnError(error -> {
              System.out.println("Ошибка при запросе баланса: " + error.getMessage());
              error.printStackTrace(); // ← Это важно!
            })
            .onErrorResume(e -> {
              System.out.println("Сервис баланса недоступен. Возвращаем значение по умолчанию (-1)");
              return Mono.just(new BigDecimal("-1"));
            });
  }





/*
  public Mono<BigDecimal> getUserBalance(Long userId) {
    return balanceApi.paymentBalanceUserIdGet(userId.toString())
            .map(balance -> BigDecimal.valueOf(balance.getAmount()))
            .timeout(Duration.ofSeconds(1))
            .retryWhen(Retry.backoff(3, Duration.ofMillis(100)))
            .onErrorResume(e -> {
              System.out.println("Сервис баланса недоступен. Возвращаем значение по умолчанию (-1)");
              return Mono.just(new BigDecimal("-1"));
            })
            .defaultIfEmpty(new BigDecimal("-1"));
  }
  */


  public Mono<Boolean> makePayment(Long userId, BigDecimal amount, String orderSignature) {
    return getUserBalance(userId)
            .flatMap(balance -> {
              if (balance.compareTo(amount) < 0) {
                return Mono.just(false);
              }

              PaymentRequest request = new PaymentRequest()
                      .userId(userId.toString())
                      .orderSignature(orderSignature)
                      .amount(amount.doubleValue());

              return paymentApi.paymentPost(request)
                      .map(response -> response.getStatus() == PaymentResponse.StatusEnum.SUCCESS)
                      .onErrorReturn(false);
            });
  }


  @PostConstruct
  public void testAuth() {
    getUserBalance(1L)
            .doOnNext(balance -> System.out.println("Balance received: " + balance))
            .doOnError(error -> System.out.println("Auth error: " + error.getMessage()))
            .subscribe();
  }

}


/*
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


  public Mono<Boolean> makePayment(Long userId, BigDecimal amount, String orderSignature) {

    return getUserBalance(userId)
            .flatMap(balance -> {
              if (balance.compareTo(amount) < 0) {
                return Mono.just(false); // Недостаточно средств
              }

              PaymentRequest request = new PaymentRequest()
                      .userId(userId.toString())
                      .orderSignature(orderSignature)
                      .amount(amount.doubleValue());

              return paymentApi.paymentPost(request)
                      .map(response -> response.getStatus() == PaymentResponse.StatusEnum.SUCCESS)
                      .onErrorReturn(false);
            });
  }

}
 */

