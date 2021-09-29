package com.example.demo;

public class Status {
	int id;
	String name;
	
	protected Status( int id, String name ) {
		this.id = id;
		this.name = name;
	}
	
	public Status() {
		
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
	
    @Override
    public String toString() {
        return "ID: " + id + " name: " + name;
    }
}
