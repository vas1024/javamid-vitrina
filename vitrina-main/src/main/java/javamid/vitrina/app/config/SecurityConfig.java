package javamid.vitrina.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.csrf.CsrfWebFilter;
import org.springframework.security.web.server.util.matcher.AndServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.NegatedServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import reactor.core.publisher.Mono;

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

            .authorizeExchange(exchanges -> exchanges
                    .pathMatchers("/cart/**").authenticated()
                    .pathMatchers("/orders/**").authenticated()
                    .pathMatchers("/admin/**").hasRole("ADMIN")
                    .pathMatchers("/check").permitAll()
                    .pathMatchers("/login").permitAll()
                    .pathMatchers("/logout").permitAll()
                    .pathMatchers("/").permitAll()
                    .pathMatchers("/main/items/**").permitAll()
                    .anyExchange().authenticated()
            )

//            .formLogin(withDefaults())
            .csrf(ServerHttpSecurity.CsrfSpec::disable) // Для тестов, в продакшене включить


            .formLogin(form -> form
                    .authenticationFailureHandler((exchange, e) -> {
                      // Реактивное чтение формы
                      System.out.println("Ошибка при логине: " + e.getMessage());
                      return Mono.error(e);
                    })
            )


            .build();
  }


  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
    // Или другой алгоритм:
    // return new Argon2PasswordEncoder();
  }

}