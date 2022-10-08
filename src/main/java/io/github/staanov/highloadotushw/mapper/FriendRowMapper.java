package io.github.staanov.highloadotushw.mapper;

import io.github.staanov.highloadotushw.model.Friend;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class FriendRowMapper implements RowMapper<Friend> {
  @Override
  public Friend mapRow(ResultSet rs, int rowNum) throws SQLException {
    Friend friend = new Friend();
    friend.setFirstUserId(rs.getLong("first_user_id"));
    friend.setSecondUserId(rs.getLong("second_user_id"));
    return friend;
  }
}
