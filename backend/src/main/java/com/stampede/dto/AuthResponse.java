package com.stampede.dto;

/** Returned on register / login / refresh. */
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";

    public AuthResponse(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public String getAccessToken()  { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public String getTokenType()    { return tokenType; }
}
