package com.example.demo;

public class SushiOrder {
	private int id;
	private int status_id;
	private int sushi_id;
	private int time_left;
	private String createdAt;
	
	public SushiOrder() {
	}
	public SushiOrder( int id ) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getStatus_id() {
		return status_id;
	}
	public void setStatus_id(int status_id) {
		this.status_id = status_id;
	}
	public int getSushi_id() {
		return sushi_id;
	}
	public void setSushi_id(int sushi_id) {
		this.sushi_id = sushi_id;
	}
	public String getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}
	public int getTime_left() {
		return time_left;
	}
	public void setTime_left(int time_left) {
		this.time_left = time_left;
	}
	
	@Override
	public String toString() {
		return "\nSushiOrder [id=" + id + ", status_id=" + status_id + ", sushi_id=" + sushi_id + ", createdAt="
				+ createdAt + ", time_left=" + time_left + "]";
	}
}
