package com.elioneto.pixapi.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "Usuário é obrigatório")
    private String username;

    @NotBlank(message = "Senha é obrigatória")
    private String password;
}
