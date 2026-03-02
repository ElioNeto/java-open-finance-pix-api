package com.elioneto.pixapi.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TokenResponse {
    private String token;
    private String tokenType;
    private long expiresIn;
}
