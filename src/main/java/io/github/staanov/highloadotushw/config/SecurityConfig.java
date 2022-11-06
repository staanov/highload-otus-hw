package io.github.staanov.highloadotushw.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.jdbc.JdbcDaoImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import javax.sql.DataSource;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class SecurityConfig {

  public DataSource dataSource;

  @Autowired
  public SecurityConfig(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf()
        .disable()
        .formLogin()
        .disable()
        .authorizeRequests()
        .antMatchers("/api/v1/auth/**")
        .permitAll()
        .antMatchers("/admin/**")
        .permitAll()
        .anyRequest()
        .authenticated()
        .and()
        .logout()
        .logoutUrl("/logout")
        .invalidateHttpSession(true)
        .deleteCookies("JSESSIONID")
        .and()
        .httpBasic();

    return http.build();
  }

  @Bean
  public UserDetailsService userDetailsService() {
    JdbcDaoImpl jdbcImpl = new JdbcDaoImpl();
    jdbcImpl.setDataSource(dataSource);
    jdbcImpl.setUsersByUsernameQuery("SELECT login, password, enabled FROM security WHERE login = ?");
    jdbcImpl.setAuthoritiesByUsernameQuery("SELECT login, authority FROM authority WHERE login = ?");
    return jdbcImpl;
  }

  @Bean
  public BCryptPasswordEncoder bCryptPasswordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationManager authManager(HttpSecurity http, BCryptPasswordEncoder bCryptPasswordEncoder,
                                           UserDetailsService userDetailsService) throws Exception {
    return http.getSharedObject(AuthenticationManagerBuilder.class)
        .userDetailsService(userDetailsService)
        .passwordEncoder(bCryptPasswordEncoder)
        .and()
        .build();
  }
}
