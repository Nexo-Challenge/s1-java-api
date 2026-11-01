package br.com.nexo.api.dto;

import br.com.nexo.api.model.AreaAplicacao;

public record AreaResponse(Long id, String nome, String descricao) {
    public static AreaResponse from(AreaAplicacao area) {
        return new AreaResponse(area.getId(), area.getNome(), area.getDescricao());
    }
}
