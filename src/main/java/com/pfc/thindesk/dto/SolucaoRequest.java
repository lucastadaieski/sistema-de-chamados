package com.pfc.thindesk.dto;

import jakarta.validation.constraints.NotBlank;

public record SolucaoRequest(
        @NotBlank(message = "A solução é obrigatória")
        String solucao) {
}
