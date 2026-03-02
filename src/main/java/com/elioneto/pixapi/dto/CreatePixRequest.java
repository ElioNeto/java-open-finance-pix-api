package com.elioneto.pixapi.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreatePixRequest {

    @NotBlank(message = "Chave Pix é obrigatória")
    private String pixKey;

    @NotNull(message = "Valor é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor deve ser positivo")
    private BigDecimal amount;

    private String description;
}
