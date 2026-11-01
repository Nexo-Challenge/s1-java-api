package br.com.nexo.api.dto;
import br.com.nexo.api.model.AplicacaoNuclear;


public record AplicacaoResponse(Long id, String titulo, String resumo, String descricao, AreaResponse area) {
    public record AreaResponse(Long id, String nome, String descricao) {}
    public static AplicacaoResponse from(AplicacaoNuclear entity) {
        var area = entity.getAreaAplicacao();
        return new AplicacaoResponse(entity.getId(), entity.getTitulo(), entity.getResumo(), entity.getDescricao(),
            new AreaResponse(area.getId(), area.getNome(), area.getDescricao()));
    }
}
