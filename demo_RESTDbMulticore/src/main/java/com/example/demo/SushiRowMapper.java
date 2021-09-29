package com.example.demo;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SushiRowMapper implements RowMapper<Sushi>{

	@Override
	public Sushi mapRow(ResultSet rs, int rowNum) throws SQLException {
		
		Sushi sushi = new Sushi();
		sushi.setId( rs.getInt( "id" ) );
		sushi.setName( rs.getString("name") );
		sushi.setTimeToMake( rs.getInt( "time_to_make" ));
		
		return sushi;
	}
}