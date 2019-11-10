package com.nstanogias.dateapp.dtos;

import lombok.Data;

import java.util.Date;

@Data
public class MessageForCreate {
    private Long senderId;
    private Long recipientId;
    private Date messageSent = new Date();
    private String content;
}
