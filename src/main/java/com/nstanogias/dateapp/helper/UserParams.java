package com.nstanogias.dateapp.helper;

import lombok.Data;

import javax.validation.constraints.Max;

@Data
public class UserParams {
    private int pageNumber = 1;
    @Max(50)
    private int pageSize = 10;
    private int userId;
    private String gender;
    private int minAge = 18;
    private int maxAge = 99;
    private String orderBy;
    private Boolean likees;
    private Boolean likers;
}
