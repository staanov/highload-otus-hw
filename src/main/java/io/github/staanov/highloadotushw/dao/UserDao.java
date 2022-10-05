package io.github.staanov.highloadotushw.dao;

import io.github.staanov.highloadotushw.dto.RegisterDto;
import io.github.staanov.highloadotushw.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
public class UserDao extends JdbcDaoSupport {

  @Autowired
  DataSource dataSource;

  @Autowired
  BCryptPasswordEncoder bCryptPasswordEncoder;

  @PostConstruct
  private void initialize() {
    setDataSource(dataSource);
  }

  public List<User> getAllUsers() {
    String sql = "SELECT * FROM user";
    List<Map<String, Object>> rowsUsers = getJdbcTemplate().queryForList(sql);

    List<User> result = new ArrayList<>();
    for (Map<String, Object> rowUser : rowsUsers) {
      User user = new User();
      user.setId((Long) rowUser.get("user_id"));
      user.setLogin((String) rowUser.get("login"));
      user.setFirstName((String) rowUser.get("first_name"));
      user.setLastName((String) rowUser.get("last_name"));
      user.setAge((Integer) rowUser.get("age"));
      user.setCity((String) rowUser.get("city"));

      String gender = (String) rowUser.get("gender");
      if (gender.equalsIgnoreCase("MALE")) {
        user.setGender(User.Gender.MALE);
      } else if (gender.equalsIgnoreCase("FEMALE")) {
        user.setGender(User.Gender.FEMALE);
      } else {
        throw new IllegalArgumentException("Gender is MALE or FEMALE");
      }

      sql = "SELECT interest FROM interest WHERE user_id = " + user.getId();
      List<Map<String, Object>> rowsInterests = getJdbcTemplate().queryForList(sql);
      List<String> interests = new ArrayList<>();
      for (Map<String, Object> rowInterest : rowsInterests) {
        interests.add((String) rowInterest.get("interest"));
      }
      user.setInterests(interests);

      result.add(user);
    }

    return result;
  }

  public void insertUser(RegisterDto registerDto) {

    GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
    String sqlUsers = "INSERT INTO user(login, first_name, last_name, age, gender, city) " +
        "VALUES (?, ?, ?, ?, ?, ?)";

    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

    jdbcTemplate.update(con -> {
      PreparedStatement preparedStatement = con.prepareStatement(sqlUsers, Statement.RETURN_GENERATED_KEYS);
      preparedStatement.setString(1, registerDto.getLogin());
      preparedStatement.setString(2, registerDto.getFirstName());
      preparedStatement.setString(3, registerDto.getLastName());
      preparedStatement.setInt(4, registerDto.getAge());
      preparedStatement.setString(5, registerDto.getGender().toString());
      preparedStatement.setString(6, registerDto.getCity());
      return preparedStatement;
    }, keyHolder);

    Integer id = keyHolder.getKey().intValue();

    String sqlInterests = "INSERT INTO interest(user_id, interest) VALUES (?, ?)";
    for (String interest : registerDto.getInterests()) {
      getJdbcTemplate().update(sqlInterests, id, interest);
    }

    String sqlSecurity = "INSERT INTO security(user_id, login, password) VALUES (?, ?, ?)";
    getJdbcTemplate().update(sqlSecurity, id,
        registerDto.getLogin(),
        bCryptPasswordEncoder.encode(registerDto.getPassword()));

    String sqlAuthority = "INSERT INTO authority(login, authority) VALUES (?, ?)";
    getJdbcTemplate().update(sqlAuthority, registerDto.getLogin(), "ROLE_USER");
  }

  public User getUserByLogin(String login) {
    String sql = "SELECT * FROM user WHERE login = '" + login + "'";

    Map<String, Object> row = getJdbcTemplate().queryForList(sql).get(0);
    User user = new User();
    user.setId((Long) row.get("user_id"));
    user.setLogin((String) row.get("login"));
    user.setFirstName((String) row.get("first_name"));
    user.setLastName((String) row.get("last_name"));
    user.setAge((Integer) row.get("age"));
    user.setCity((String) row.get("city"));

    String gender = (String) row.get("gender");
    if (gender.equalsIgnoreCase("MALE")) {
      user.setGender(User.Gender.MALE);
    } else if (gender.equalsIgnoreCase("FEMALE")) {
      user.setGender(User.Gender.FEMALE);
    } else {
      throw new IllegalArgumentException("Gender is MALE or FEMALE");
    }

    sql = "SELECT interest FROM interest WHERE user_id = " + user.getId();
    List<Map<String, Object>> rowsInterests = getJdbcTemplate().queryForList(sql);
    List<String> interests = new ArrayList<>();
    for (Map<String, Object> rowInterest : rowsInterests) {
      interests.add((String) rowInterest.get("interest"));
    }
    user.setInterests(interests);

    return user;
  }
}
