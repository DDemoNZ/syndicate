package com.task10.models;

import lombok.Data;

@Data
public class SignUpRequestDto {

    private String firstName;
    private String lastName;
    private String email;
    private String password;

}
