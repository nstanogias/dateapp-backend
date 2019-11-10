package com.nstanogias.dateapp.dtos;

import lombok.Data;

import java.util.Date;

@Data
public class UserForList {
    private Long id;
    private String username;
    private String gender;
    private int age;
    private String knownAs;
    private Date created;
    private Date lastActive;
    private String city;
    private String country;
    private String photoUrl;
}
