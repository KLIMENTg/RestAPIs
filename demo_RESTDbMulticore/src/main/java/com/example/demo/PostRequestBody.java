package com.example.demo;

public class PostRequestBody {
	private String sushi_name;

	public String getSushi_name() {
		return sushi_name;
	}

	public void setSushi_name(String sushi_name) {
		this.sushi_name = sushi_name;
	}

	@Override
	public String toString() {
		return "PostRequestBody [sushi_name=" + sushi_name + "]";
	}
}
