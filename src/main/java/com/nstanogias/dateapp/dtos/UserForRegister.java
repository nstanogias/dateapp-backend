package com.nstanogias.dateapp.dtos;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

@Data
public class UserForRegister {
    @NotBlank(message = "username is required")
    private String username;

    @NotBlank(message = "Password field is required")
    @Size(min = 6, max = 20)
    private String password;

    @NotBlank
    private String gender;

    @NotBlank
    private String knownAs;

    @NotNull
    private Date dateOfBirth;

    @NotBlank
    private String city;

    @NotBlank
    private String country;

    private Date created = new Date();

    private Date lastActive = new Date();
}
