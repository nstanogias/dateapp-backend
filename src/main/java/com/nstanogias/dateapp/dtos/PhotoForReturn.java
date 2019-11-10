package com.nstanogias.dateapp.dtos;

import lombok.Data;

import java.util.Date;

@Data
public class PhotoForReturn {
    private int id;
    private String url;
    private String description;
    private Date dateAdded;
    private Boolean isMain;
    private String publicId;
}
