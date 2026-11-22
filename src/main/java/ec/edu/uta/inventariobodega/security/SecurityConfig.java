package ec.edu.uta.inventariobodega.security;

import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
  @Bean
  org.springframework.boot.web.servlet.FilterRegistrationBean<EstadoUsuarioFilter> registroEstado(
      EstadoUsuarioFilter filtro) {
    var registro = new org.springframework.boot.web.servlet.FilterRegistrationBean<>(filtro);
    registro.setEnabled(false);
    return registro;
  }

  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  SecurityFilterChain filterChain(HttpSecurity http, EstadoUsuarioFilter estado) throws Exception {
    return http.addFilterAfter(
            estado, org.springframework.security.web.access.intercept.AuthorizationFilter.class)
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers("/login", "/css/**", "/vendor/**", "/error")
                    .permitAll()
                    .requestMatchers("/admin/**", "/entradas/*/aprobar", "/entradas/*/rechazar")
                    .hasRole("ADMIN")
                    .anyRequest()
                    .hasAnyRole("ADMIN", "BODEGUERO"))
        .formLogin(form -> form.loginPage("/login").defaultSuccessUrl("/", true).permitAll())
        .logout(logout -> logout.logoutSuccessUrl("/login?logout"))
        .exceptionHandling(ex -> ex.accessDeniedPage("/acceso-denegado"))
        .build();
  }
}
