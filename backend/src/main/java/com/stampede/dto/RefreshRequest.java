package com.stampede.dto;

import jakarta.validation.constraints.NotBlank;

public class RefreshRequest {

    @NotBlank
    private String refreshToken;

    public RefreshRequest() {
    }

    public String getRefreshToken()                 { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
}
