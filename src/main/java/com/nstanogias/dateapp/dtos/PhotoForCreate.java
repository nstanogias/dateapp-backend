package com.nstanogias.dateapp.dtos;

import lombok.Data;

@Data
public class PhotoForCreate {
    private String url;
    private Boolean isMain;
    private String description;
    private String publicId;
}
