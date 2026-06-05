package com.company.demo.entity;

public class LoginResponse {
    private String token;
    private String username;
    private Long expireTime;

    public LoginResponse(String token, String username, Long expireTime) {
        this.token = token;
        this.username = username;
        this.expireTime = expireTime;
    }

    // getter / setter
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public Long getExpireTime() { return expireTime; }
    public void setExpireTime(Long expireTime) { this.expireTime = expireTime; }
}