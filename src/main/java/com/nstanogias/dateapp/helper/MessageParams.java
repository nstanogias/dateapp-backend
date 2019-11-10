package com.nstanogias.dateapp.helper;

import lombok.Data;

@Data
public class MessageParams {
    public static final int maxPageSize = 50;

    private int pageNumber = 1;
    private int pageSize = 10;
    private long userId;
    private String messageContainer = "Unread";
}
