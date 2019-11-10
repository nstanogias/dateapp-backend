package com.nstanogias.dateapp.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessagePK implements Serializable {
    protected Date messageSent;
    protected User sender;
    protected User recipient;
}
