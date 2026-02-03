package com.smarthiring.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JwtAuthResponse {

    private String accessToken;
    private String tokenType = "Bearer";
    private Long expiresIn;
    private UserResponse user;
    private List<String> roles;

    public JwtAuthResponse(String accessToken, UserResponse user, List<String> roles) {
        this.accessToken = accessToken;
        this.tokenType = "Bearer";
        this.user = user;
        this.roles = roles;
    }
}