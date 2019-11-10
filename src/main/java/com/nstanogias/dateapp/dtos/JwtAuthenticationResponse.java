package com.nstanogias.dateapp.dtos;

import lombok.Data;

@Data
public class JwtAuthenticationResponse {
    private String token;
    private UserForList user;
    private String tokenType = "Bearer";

    public JwtAuthenticationResponse(String accessToken, UserForList userForList) {
        this.token = accessToken;
        this.user = userForList;
    }
}
