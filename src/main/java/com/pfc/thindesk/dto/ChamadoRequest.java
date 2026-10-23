package com.pfc.thindesk.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChamadoRequest(
        @NotBlank(message = "O título é obrigatório")
        @Size(max = 120, message = "O título pode ter no máximo 120 caracteres")
        String titulo,

        @NotBlank(message = "A descrição é obrigatória")
        String descricao) {
}
