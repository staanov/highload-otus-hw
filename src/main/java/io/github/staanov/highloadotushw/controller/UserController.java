package io.github.staanov.highloadotushw.controller;

import io.github.staanov.highloadotushw.dto.FriendDto;
import io.github.staanov.highloadotushw.dto.LoginDto;
import io.github.staanov.highloadotushw.dto.RegisterDto;
import io.github.staanov.highloadotushw.exception.FriendNotFoundException;
import io.github.staanov.highloadotushw.exception.NotAllDataProvidedException;
import io.github.staanov.highloadotushw.exception.RepeatedFriendAttemptException;
import io.github.staanov.highloadotushw.exception.UserNotFoundException;
import io.github.staanov.highloadotushw.model.User;
import io.github.staanov.highloadotushw.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
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

  UserService userService;
  AuthenticationManager authenticationManager;

  @Autowired
  public UserController(UserService userService, AuthenticationManager authenticationManager) {
    this.userService = userService;
    this.authenticationManager = authenticationManager;
  }

  @PostMapping("/auth/register")
  public ResponseEntity<String> registerUser(@RequestBody RegisterDto registerDto) {
    try {
      userService.insertUser(registerDto);
      return ResponseEntity.ok("The user registered successfully");
    } catch (NotAllDataProvidedException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  @PostMapping("/auth/login")
  public ResponseEntity<String> loginUser(@RequestBody LoginDto loginDto) {
    try {
      Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
          loginDto.getLogin(), loginDto.getPassword()));
      SecurityContextHolder.getContext().setAuthentication(authentication);
      return ResponseEntity.ok("The user authorized successfully");
    } catch (AuthenticationException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Login or password are invalid. Please try again.");
    }

  }

  @GetMapping("/logout")
  public ResponseEntity<String> logoutUser() {
    SecurityContextHolder.getContext().setAuthentication(null);
    return ResponseEntity.ok("The user logout successfully");
  }

  @GetMapping("/users")
  public ResponseEntity<List<User>> getUsers() {
    return ResponseEntity.ok(userService.getAllUsers());
  }

  @GetMapping("/users/{login}")
  public ResponseEntity<User> getUserByLogin(@PathVariable String login) {
    try {
      return ResponseEntity.ok(userService.getUserByLogin(login));
    } catch (UserNotFoundException e) {
      return ResponseEntity.notFound().build();
    }
  }

  @PostMapping("/add-friend")
  public ResponseEntity<String> addFriend(@RequestBody FriendDto friendDto) {
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    String username = ((UserDetails) principal).getUsername();
    User currentUser = new User();
    User friendUser = new User();
    try {
      currentUser = userService.getUserByLogin(username);
      friendUser = userService.getUserByLogin(friendDto.getFriendLogin());
    } catch (UserNotFoundException e) {
      return ResponseEntity.notFound().build();
    }

    try {
      userService.addFriend(currentUser, friendUser);
    } catch (RepeatedFriendAttemptException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }

    return ResponseEntity.ok("The user is added to your friends successfully");
  }

  @DeleteMapping("/remove-friend")
  public ResponseEntity<String> removeFriend(@RequestBody FriendDto friendDto) {
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    String username = ((UserDetails) principal).getUsername();
    User currentUser = new User();
    User friendUser = new User();
    try {
      currentUser = userService.getUserByLogin(username);
      friendUser = userService.getUserByLogin(friendDto.getFriendLogin());
    } catch (UserNotFoundException e) {
      return ResponseEntity.notFound().build();
    }

    try {
      userService.removeFriend(currentUser, friendUser);
    } catch (FriendNotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    return ResponseEntity.ok("The user has removed from your friends");
  }

  @GetMapping("/friends/{login}")
  public ResponseEntity<List<User>> getUserFriends(@PathVariable String login) {
    User user = new User();
    try {
      user = userService.getUserByLogin(login);
    } catch (UserNotFoundException e) {
      return ResponseEntity.notFound().build();
    }

    List<User> friends = userService.getUserFriends(user);

    return ResponseEntity.ok(friends);
  }

  @GetMapping("/my-friends")
  public ResponseEntity<List<User>> getAuthorizedUserFriends() {
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    String username = ((UserDetails) principal).getUsername();
    List<User> myFriends = getUserFriends(username).getBody();
    return ResponseEntity.ok(myFriends);
  }

}
