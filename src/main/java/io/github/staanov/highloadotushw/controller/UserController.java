package io.github.staanov.highloadotushw.controller;

import io.github.staanov.highloadotushw.dto.LoginDto;
import io.github.staanov.highloadotushw.dto.RegisterDto;
import io.github.staanov.highloadotushw.model.User;
import io.github.staanov.highloadotushw.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class UserController {

  @Autowired
  UserService userService;

  @Autowired
  AuthenticationManager authenticationManager;

  @GetMapping("/users")
  public List<User> getUsers() {
    return userService.getAllUsers();
  }

  @GetMapping("/users/{login}")
  public User getUserByLogin(@PathVariable String login) {
    return userService.getUserByLogin(login);
  }

  @PostMapping("/auth/register")
  public void registerUser(@RequestBody RegisterDto registerDto) {
    userService.insertUser(registerDto);
  }

  @PostMapping("/auth/login")
  public void loginUser(@RequestBody LoginDto loginDto) {
    Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
        loginDto.getLogin(), loginDto.getPassword()));
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }

  @GetMapping("/logout")
  public void logoutUser() {
    SecurityContextHolder.getContext().setAuthentication(null);
  }

}
