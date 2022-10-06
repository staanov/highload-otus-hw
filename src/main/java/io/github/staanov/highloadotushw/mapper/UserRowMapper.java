package io.github.staanov.highloadotushw.mapper;

import io.github.staanov.highloadotushw.model.User;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UserRowMapper implements RowMapper<User> {

  @Override
  public User mapRow(ResultSet rs, int rowNum) throws SQLException {
    User user = new User();
    user.setId(rs.getLong("user_id"));
    user.setLogin(rs.getString("login"));
    user.setFirstName(rs.getString("first_name"));
    user.setLastName(rs.getString("last_name"));
    user.setAge(rs.getInt("age"));
    user.setCity(rs.getString("city"));

    String gender = rs.getString("gender");
    if (gender.equalsIgnoreCase("MALE")) {
      user.setGender(User.Gender.MALE);
    } else if (gender.equalsIgnoreCase("FEMALE")) {
      user.setGender(User.Gender.FEMALE);
    } else {
      throw new IllegalArgumentException("Gender is MALE or FEMALE");
    }

    return user;
  }
}
