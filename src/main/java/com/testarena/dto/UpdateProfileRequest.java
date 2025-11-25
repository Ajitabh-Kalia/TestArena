package com.testarena.dto;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String name;
    private String password;
}
