package com.davidborsamo.campusevents.dto;

import com.davidborsamo.campusevents.model.Role;

public class AuthResponse {
    private String token;
    private Long userId;
    private String username;
    private Role role;

    public AuthResponse(String token, Long userId, String username, Role role) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.role = role;
    }

    public String getToken() { return token; }
    public Long getUserId() { return userId; }
    public String getUsername() { return username; }
    public Role getRole() { return role; }
}
