package io.github.staanov.highloadotushw.service;

import io.github.staanov.highloadotushw.dao.UserDao;
import io.github.staanov.highloadotushw.dto.RegisterDto;
import io.github.staanov.highloadotushw.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

  @Autowired
  UserDao userDao;

  public List<User> getAllUsers() {
    return userDao.getAllUsers();
  }

  public void insertUser(RegisterDto registerDto) {
    userDao.insertUser(registerDto);
  }

  public User getUserByLogin(String login) {
    return userDao.getUserByLogin(login);
  }
}
