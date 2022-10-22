package io.github.staanov.highloadotushw.service;

import io.github.staanov.highloadotushw.dao.UserDao;
import io.github.staanov.highloadotushw.dto.RegisterDto;
import io.github.staanov.highloadotushw.exception.FriendNotFoundException;
import io.github.staanov.highloadotushw.exception.NotAllDataProvidedException;
import io.github.staanov.highloadotushw.exception.RepeatedFriendAttemptException;
import io.github.staanov.highloadotushw.exception.UserNotFoundException;
import io.github.staanov.highloadotushw.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

  UserDao userDao;

  @Autowired
  public UserService(UserDao userDao) {
    this.userDao = userDao;
  }

  public List<User> getAllUsers() {
    return userDao.getAllUsers();
  }

  public void insertUser(RegisterDto registerDto) throws NotAllDataProvidedException {
    userDao.insertUser(registerDto);
  }

  public User getUserByLogin(String login) throws UserNotFoundException {
    return userDao.getUserByLogin(login);
  }

  public void addFriend(User currentUser, User friendUser) throws RepeatedFriendAttemptException {
    userDao.addFriend(currentUser, friendUser);
  }

  public void removeFriend(User currentUser, User friendUser) throws FriendNotFoundException {
    userDao.removeFriend(currentUser, friendUser);
  }

  public List<User> getUserFriends(User user) {
    return userDao.getUserFriends(user);
  }

  public List<User> getUsersByNamesPrefix(String firstNamePrefix, String lastNamePrefix) {
    firstNamePrefix = firstNamePrefix + "%";
    lastNamePrefix = lastNamePrefix + "%";
    return userDao.getUsersByNamesPrefix(firstNamePrefix, lastNamePrefix);
  }
}
