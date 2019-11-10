package com.nstanogias.dateapp.dtos;

import lombok.Data;

@Data
public class UserForUpdate {
    private String introduction;
    private String lookingFor;
    private String interests;
    private String city;
    private String country;
}
