package io.github.staanov.highloadotushw.dao;

import io.github.staanov.highloadotushw.dto.RegisterDto;
import io.github.staanov.highloadotushw.mapper.UserRowMapper;
import io.github.staanov.highloadotushw.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

  public void insertUser(RegisterDto registerDto) {

    Integer id = insertUsers(registerDto);
    insertInterests(registerDto, id);
    insertSecurity(registerDto, id);
    insertAuthority(registerDto);

  }

  private Integer insertUsers(RegisterDto registerDto) {
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

    return keyHolder.getKey().intValue();
  }

  private void insertInterests(RegisterDto registerDto, Integer id) {
    String sqlInterests = "INSERT INTO interest(user_id, interest) VALUES (?, ?)";
    for (String interest : registerDto.getInterests()) {
      getJdbcTemplate().update(sqlInterests, id, interest);
    }
  }

  private void insertSecurity(RegisterDto registerDto, Integer id) {
    String sqlSecurity = "INSERT INTO security(user_id, login, password) VALUES (?, ?, ?)";
    getJdbcTemplate().update(sqlSecurity, id,
        registerDto.getLogin(),
        bCryptPasswordEncoder.encode(registerDto.getPassword()));
  }

  private void insertAuthority(RegisterDto registerDto) {
    String sqlAuthority = "INSERT INTO authority(login, authority) VALUES (?, ?)";
    getJdbcTemplate().update(sqlAuthority, registerDto.getLogin(), "ROLE_USER");
  }

  public List<User> getAllUsers() {
    String sql = "SELECT * FROM user";
    List<User> result = getJdbcTemplate().query(sql, new UserRowMapper());

    for (User user : result) {
      sql = "SELECT interest FROM interest WHERE user_id = :user_id";
      NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
      List<String> interests = jdbcTemplate.query(
          sql,
          new MapSqlParameterSource().addValue("user_id", user.getId()),
          new InterestRowMapper());
      user.setInterests(interests);
    }

    return result;
  }

  public User getUserByLogin(String login) {
    String sql = "SELECT * FROM user WHERE login = '" + login + "'";
    User user = getJdbcTemplate().query(sql, new UserRowMapper()).get(0);

    sql = "SELECT interest FROM interest WHERE user_id = :user_id";
    NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    List<String> interests = jdbcTemplate.query(
        sql,
        new MapSqlParameterSource().addValue("user_id", user.getId()),
        new InterestRowMapper());
    user.setInterests(interests);

    return user;
  }

  private class InterestRowMapper implements RowMapper<String> {
    @Override
    public String mapRow(ResultSet rs, int rowNum) throws SQLException {
      return rs.getString("interest");
    }
  }
}
