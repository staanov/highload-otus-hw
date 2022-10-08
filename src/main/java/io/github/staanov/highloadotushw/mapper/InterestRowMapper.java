package io.github.staanov.highloadotushw.mapper;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class InterestRowMapper implements RowMapper<String> {
  @Override
  public String mapRow(ResultSet rs, int rowNum) throws SQLException {
    return rs.getString("interest");
  }
}
