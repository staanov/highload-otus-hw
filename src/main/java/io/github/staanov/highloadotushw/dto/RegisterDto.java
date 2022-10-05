package io.github.staanov.highloadotushw.dto;

import io.github.staanov.highloadotushw.model.User;

import java.util.List;

public class RegisterDto {
  private String login;
  private String password;
  private String firstName;
  private String lastName;
  private int age;
  private User.Gender gender;
  private List<String> interests;
  private String city;

  public RegisterDto() {
  }

  public RegisterDto(String firstName, String lastName, int age, User.Gender gender, List<String> interests, String city) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.age = age;
    this.gender = gender;
    this.interests = interests;
    this.city = city;
  }

  public enum Gender {
    MALE,
    FEMALE
  }

  public String getLogin() {
    return login;
  }

  public void setLogin(String login) {
    this.login = login;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public int getAge() {
    return age;
  }

  public void setAge(int age) {
    this.age = age;
  }

  public User.Gender getGender() {
    return gender;
  }

  public void setGender(User.Gender gender) {
    this.gender = gender;
  }

  public List<String> getInterests() {
    return interests;
  }

  public void setInterests(List<String> interests) {
    this.interests = interests;
  }

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }
}
