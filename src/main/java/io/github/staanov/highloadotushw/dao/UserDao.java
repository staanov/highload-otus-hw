package io.github.staanov.highloadotushw.dao;

import io.github.staanov.highloadotushw.dto.RegisterDto;
import io.github.staanov.highloadotushw.exception.FriendNotFoundException;
import io.github.staanov.highloadotushw.exception.NotAllDataProvidedException;
import io.github.staanov.highloadotushw.exception.RepeatedFriendAttemptException;
import io.github.staanov.highloadotushw.exception.UserNotFoundException;
import io.github.staanov.highloadotushw.mapper.FriendRowMapper;
import io.github.staanov.highloadotushw.mapper.InterestRowMapper;
import io.github.staanov.highloadotushw.mapper.UserRowMapper;
import io.github.staanov.highloadotushw.model.Friend;
import io.github.staanov.highloadotushw.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
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

@Repository
public class UserDao extends JdbcDaoSupport {

  DataSource dataSource;
  BCryptPasswordEncoder bCryptPasswordEncoder;

  @Autowired
  public UserDao(DataSource dataSource, BCryptPasswordEncoder bCryptPasswordEncoder) {
    this.dataSource = dataSource;
    this.bCryptPasswordEncoder = bCryptPasswordEncoder;
  }

  @PostConstruct
  private void initialize() {
    setDataSource(dataSource);
  }

  public void insertUser(RegisterDto registerDto) throws NotAllDataProvidedException {

    try {
      Integer id = insertUsers(registerDto);
      insertInterests(registerDto, id);
      insertSecurity(registerDto, id);
      insertAuthority(registerDto);
    } catch (DataAccessException e) {
      throw new NotAllDataProvidedException("You need to provide all of these data: " +
          "login, password, firstName, lastName, age, gender (MALE or FEMALE), list of interests and city.\n" +
          "If you provided all of these information, try to choose another login.");
    }


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

  public User getUserByLogin(String login) throws UserNotFoundException {
    String sql = "SELECT * FROM user WHERE login = '" + login + "'";
    User user = new User();
    try {
      user = getJdbcTemplate().query(sql, new UserRowMapper()).get(0);
    } catch (IndexOutOfBoundsException e) {
      throw new UserNotFoundException("User with this login is not found");
    }

    sql = "SELECT interest FROM interest WHERE user_id = :user_id";
    NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    List<String> interests = jdbcTemplate.query(
        sql,
        new MapSqlParameterSource().addValue("user_id", user.getId()),
        new InterestRowMapper());
    user.setInterests(interests);

    return user;
  }

  public void addFriend(User currentUser, User friendUser) throws RepeatedFriendAttemptException {
    String preventSql = "SELECT second_user_id FROM friend WHERE first_user_id = :first_user_id";
    NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);

    List<Long> ids = jdbcTemplate.query(preventSql,
        new MapSqlParameterSource().addValue("first_user_id", friendUser.getId()),
        (rs, rowNum) -> rs.getLong("second_user_id"));

    if (ids.contains(currentUser.getId())) {
      throw new RepeatedFriendAttemptException("This user is your friend currently");
    }

    String sql = "INSERT INTO friend VALUES (:current_user_id, :friend_user_id)";
    try {
      jdbcTemplate.update(sql,
          new MapSqlParameterSource()
              .addValue("current_user_id", currentUser.getId())
              .addValue("friend_user_id", friendUser.getId()));
    } catch (DataAccessException e) {
      throw new RepeatedFriendAttemptException("This user is your friend currently");
    }
  }

  public void removeFriend(User currentUser, User friendUser) throws FriendNotFoundException {
    String preventSql = "SELECT * FROM friend WHERE first_user_id = :first_user_id AND second_user_id = :second_user_id";
    NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    List<Friend> preventList = jdbcTemplate.query(preventSql,
        new MapSqlParameterSource()
            .addValue("first_user_id", currentUser.getId())
            .addValue("second_user_id", friendUser.getId()),
        new FriendRowMapper());

    if (!preventList.isEmpty()) {
      String sql = "DELETE FROM friend WHERE first_user_id = :first_user_id AND second_user_id = :second_user_id";
      jdbcTemplate.update(sql,
          new MapSqlParameterSource()
              .addValue("first_user_id", currentUser.getId())
              .addValue("second_user_id", friendUser.getId()));
    } else {
      preventList = jdbcTemplate.query(preventSql,
          new MapSqlParameterSource()
              .addValue("first_user_id", friendUser.getId())
              .addValue("second_user_id", currentUser.getId()),
          new FriendRowMapper());
      if (!preventList.isEmpty()) {
        String sql = "DELETE FROM friend WHERE first_user_id = :first_user_id AND second_user_id = :second_user_id";
        jdbcTemplate.update(sql,
            new MapSqlParameterSource()
                .addValue("first_user_id", friendUser.getId())
                .addValue("second_user_id", currentUser.getId()));
      } else {
        throw new FriendNotFoundException("Mentioned user is not your friend");
      }
    }
  }

  public List<User> getUserFriends(User user) {
    NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);

    String firstSql = "SELECT second_user_id FROM friend WHERE first_user_id = :user_id";
    List<Long> friendsIds = jdbcTemplate.query(firstSql,
        new MapSqlParameterSource().addValue("user_id", user.getId()),
        (rs, rowNum) -> rs.getLong("second_user_id"));

    String secondSql = "SELECT first_user_id FROM friend WHERE second_user_id = :user_id";
    List<Long> anotherFriendsIds = jdbcTemplate.query(secondSql,
        new MapSqlParameterSource().addValue("user_id", user.getId()),
        (rs, rowNum) -> rs.getLong("first_user_id"));

    friendsIds.addAll(anotherFriendsIds);

    List<User> userFriends = new ArrayList<>();
    String sql = "SELECT * FROM user WHERE user_id = :id";
    for (Long id : friendsIds) {
      User u = jdbcTemplate.query(sql, new MapSqlParameterSource().addValue("id", id), new UserRowMapper()).get(0);
      userFriends.add(u);
    }

    return userFriends;
  }

  public List<User> getUsersByNamesPrefix(String firstNamePrefix, String lastNamePrefix) {
    NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);

    String sql = "SELECT * FROM user WHERE first_name LIKE :first_name_prefix AND last_name LIKE :last_name_prefix " +
        "ORDER BY user_id";
    List<User> result = jdbcTemplate.query(sql,
        new MapSqlParameterSource()
            .addValue("first_name_prefix", firstNamePrefix)
            .addValue("last_name_prefix", lastNamePrefix),
        new UserRowMapper());

    for (User user : result) {
      sql = "SELECT interest FROM interest WHERE user_id = :user_id";
      List<String> interests = jdbcTemplate.query(
          sql,
          new MapSqlParameterSource().addValue("user_id", user.getId()),
          new InterestRowMapper());
      user.setInterests(interests);
    }

    return result;
  }
}
