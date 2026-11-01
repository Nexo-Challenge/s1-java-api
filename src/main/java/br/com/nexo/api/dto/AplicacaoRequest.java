package br.com.nexo.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** Entrada de escrita sem identidade nem vínculos de impacto. */
public record AplicacaoRequest(
    @NotNull(message = "Área de aplicação é obrigatória.")
    @Positive(message = "Área de aplicação deve ter ID positivo.") Long areaAplicacaoId,
    @NotBlank(message = "Título é obrigatório e não pode estar em branco.")
    @Size(max = 150, message = "Título deve ter no máximo 150 caracteres.") String titulo,
    @NotBlank(message = "Resumo é obrigatório e não pode estar em branco.")
    @Size(max = 500, message = "Resumo deve ter no máximo 500 caracteres.") String resumo,
    @NotBlank(message = "Descrição é obrigatória e não pode estar em branco.")
    @Size(max = 4000, message = "Descrição deve ter no máximo 4000 caracteres.") String descricao
) {}
