package com.example.demo;

public class Sushi {
	int id;
	String name;
	int timeToMake;
	
	protected Sushi( int id, String name, int timeToMake ) {
		this.id = id;
		this.name = name;
		this.timeToMake = timeToMake;
	}
	
	public Sushi() {
		
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getTimeToMake() {
		return timeToMake;
	}

	public void setTimeToMake(int timeToMake) {
		this.timeToMake = timeToMake;
	}

	@Override
	public String toString() {
		return "Sushi [id=" + id + ", name=" + name + ", timeToMake=" + timeToMake + "]";
	}
	
}
