package com.example.demo;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class StatusRowMapper implements RowMapper<Status>{

	@Override
	public Status mapRow(ResultSet rs, int rowNum) throws SQLException {
		
		Status status = new Status();
		status.setId( rs.getInt("id") );
		status.setName( rs.getString( "name" ));
		
		return status;
	}

}
