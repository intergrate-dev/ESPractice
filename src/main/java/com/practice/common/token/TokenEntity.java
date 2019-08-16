package com.practice.common.token;

public class TokenEntity {
	private int TK_ID;
	private String TK_TOKEN;
	private String TK_EXPIRETIME;
	private String TK_SOURE;
	public int getTK_ID() {
		return TK_ID;
	}
	public void setTK_ID(int tK_ID) {
		TK_ID = tK_ID;
	}
	public String getTK_TOKEN() {
		return TK_TOKEN;
	}
	public void setTK_TOKEN(String tK_TOKEN) {
		TK_TOKEN = tK_TOKEN;
	}
	public String getTK_EXPIRETIME() {
		return TK_EXPIRETIME;
	}
	public void setTK_EXPIRETIME(String tK_EXPIRETIME) {
		TK_EXPIRETIME = tK_EXPIRETIME;
	}
	public String getTK_SOURE() {
		return TK_SOURE;
	}
	public void setTK_SOURE(String tK_SOURE) {
		TK_SOURE = tK_SOURE;
	}
	@Override
	public String toString() {
		return "TokenEntity [TK_ID=" + TK_ID + ", TK_TOKEN=" + TK_TOKEN
				+ ", TK_EXPIRETIME=" + TK_EXPIRETIME + ", TK_SOURE=" + TK_SOURE
				+ "]";
	}
	
}
