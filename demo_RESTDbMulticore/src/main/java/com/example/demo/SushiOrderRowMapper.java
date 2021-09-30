package com.example.demo;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SushiOrderRowMapper implements RowMapper<SushiOrder>{

	@Override
	public SushiOrder mapRow(ResultSet rs, int rowNum) throws SQLException {
		
		SushiOrder sushi = new SushiOrder();
		sushi.setId( rs.getInt( "id" ) );
		sushi.setStatus_id( rs.getInt("status_id") );
		sushi.setSushi_id( rs.getInt( "sushi_id" ) );
		sushi.setTime_left( rs.getInt( "time_left" ) );
		sushi.setTime_spent( rs.getInt( "time_spent" ) );
		sushi.setCreatedAt( rs.getString( "createdAt" ) );
		
		return sushi;
	}
}