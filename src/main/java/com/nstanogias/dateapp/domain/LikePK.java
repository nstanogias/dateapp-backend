package com.nstanogias.dateapp.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LikePK implements Serializable {
    protected Long liker;
    protected Long likee;
}
