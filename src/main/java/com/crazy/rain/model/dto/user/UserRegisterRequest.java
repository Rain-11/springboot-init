package com.crazy.rain.model.dto.user;

import lombok.Data;

import java.io.Serializable;


@Data
public class UserRegisterRequest implements Serializable {

    private static final long serialVersionUID = 3191241716373120793L;

    private String email;

    private String userPassword;

    private String verificationCode;
}
