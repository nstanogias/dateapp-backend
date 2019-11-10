package com.nstanogias.dateapp.dtos;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class UserForDetailed {
    private Long id;
    private String username;
    private String gender;
    private int age;
    private String knownAs;
    private Date created;
    private Date lastActive;
    private String introduction;
    private String lookingFor;
    private String interests;
    private String city;
    private String country;
    private String photoUrl;
    private List<PhotosForDetailed> photos;
}
