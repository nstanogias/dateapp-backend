package com.nstanogias.dateapp.dtos;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class UserForLogin {
    @NotBlank
    private String username;

    @NotBlank
    private String password;
}
