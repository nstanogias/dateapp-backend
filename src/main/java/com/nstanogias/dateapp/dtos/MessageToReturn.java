package com.nstanogias.dateapp.dtos;

import lombok.Data;

import java.util.Date;

@Data
public class MessageToReturn {
    private int id;
    private Long senderId;
    private String senderKnownAs;
    private String senderPhotoUrl;
    private Long recipientId;
    private String recipientKnownAs;
    private String recipientPhotoUrl;
    private String content;
    private Boolean isRead;
    private Date dateRead;
    private Date messageSent;
}
