package javamid.vitrina.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.csrf.CookieServerCsrfTokenRepository;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import reactor.core.publisher.Mono;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.csrf.CookieServerCsrfTokenRepository;
import org.springframework.security.web.server.csrf.CsrfWebFilter;
import org.springframework.security.web.server.util.matcher.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Optional;

import static org.springframework.security.config.Customizer.withDefaults;


@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

  private final ReactiveUserDetailsService userDetailsService;

  public SecurityConfig(ReactiveUserDetailsService userDetailsService) {
    this.userDetailsService = userDetailsService;
  }

  @Bean
  public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
    return http
/*
            .csrf(csrf -> csrf
                    .requireCsrfProtectionMatcher(
                            new AndServerWebExchangeMatcher(
                                    CsrfWebFilter.DEFAULT_CSRF_MATCHER,
                                    new NegatedServerWebExchangeMatcher(
                                            ServerWebExchangeMatchers.pathMatchers("/logout")
                                    )
                            )
                    )
            )
*/
/*
            .csrf(csrf -> csrf
                    .csrfTokenRepository(CookieServerCsrfTokenRepository.withHttpOnlyFalse())
                    .accessDeniedHandler((exchange, ex) -> {
                      System.out.println("CSRF DENIED! Reason: " + ex.getMessage());
                      System.out.println("Request URI: " + exchange.getRequest().getURI());
                      return Mono.error(ex);
                    })
            )
*/
/*
            .csrf(csrf -> csrf
                    .csrfTokenRepository(CookieServerCsrfTokenRepository.withHttpOnlyFalse())
            )
*/

            .authorizeExchange(exchanges -> exchanges
                    .pathMatchers("/cart/**").authenticated()
                    .pathMatchers("/orders/**").authenticated()
                    .pathMatchers("/admin/**").hasRole("ADMIN")
                    .pathMatchers("/check").permitAll()
                    .pathMatchers("/login").permitAll()
                    .pathMatchers("/logout").permitAll()
                    .pathMatchers("/").permitAll()
                    .pathMatchers("/main/items/**").permitAll()
                    .pathMatchers("/items/**").permitAll()
                    .pathMatchers("/images/**").permitAll()
                    .pathMatchers("/orderimages/**").authenticated()
                    .pathMatchers("/buy/**").authenticated()
//                    .anyExchange().authenticated()
            )


//            .formLogin(withDefaults())

            .formLogin(form -> form
                    .authenticationSuccessHandler((exchange, authentication) -> {
                      System.out.println("✅ Вход: " + authentication.getName() +
                              " | Роли: " + authentication.getAuthorities());
                      exchange.getExchange().getResponse()
                              .setStatusCode(HttpStatus.SEE_OTHER);
                      exchange.getExchange().getResponse()
                              .getHeaders().setLocation(URI.create("/"));
                      return Mono.empty();
                    })
            )

            .logout(logout -> logout
                    .logoutUrl("/logout")
                    //  красивый костыль, по сути замена лямбды.
                    //  и всё потому, чт в реактивном вебфлюксе нет .logoutSuccessUrl(
                    .logoutSuccessHandler(this::handleLogout)
            )

            .csrf(ServerHttpSecurity.CsrfSpec::disable) // Для тестов, в продакшене включить


            .build();
  }


  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
    // Или другой алгоритм:
    // return new Argon2PasswordEncoder();
  }

  private Mono<Void> handleLogout(WebFilterExchange exchange, Authentication auth) {
    ServerWebExchange serverExchange = exchange.getExchange();
    serverExchange.getResponse().setStatusCode(HttpStatus.FOUND);
    serverExchange.getResponse().getHeaders().setLocation(URI.create("/"));
    return serverExchange.getResponse().setComplete();
  }

}