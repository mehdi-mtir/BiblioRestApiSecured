package com.m2i.BiblioRestApi.dto;

public class LoginResponse {
    private String token;
    private String username;
    private String role;
    private String type = "Bearer";

    // Constructeurs, getters, setters

    public LoginResponse() {
    }

    public LoginResponse(String token, String username, String role) {
        this.token = token;
        this.username = username;
        this.role = role;
    }

    public LoginResponse(String token, String username, String role, String type) {
        this.token = token;
        this.username = username;
        this.role = role;
        this.type = type;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
