package br.com.nexo.api.service;

import br.com.nexo.api.dto.AplicacaoRequest;
import br.com.nexo.api.dto.AplicacaoResponse;
import br.com.nexo.api.model.AplicacaoNuclear;
import br.com.nexo.api.repository.AplicacaoNuclearRepository;
import br.com.nexo.api.repository.AreaAplicacaoRepository;
import br.com.nexo.api.repository.AplicacaoImpactoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = false)
public class AplicacaoEscritaService {
    private final AplicacaoNuclearRepository aplicacoes;
    private final AreaAplicacaoRepository areas;

    private final AplicacaoImpactoRepository vinculos;

    public AplicacaoEscritaService(AplicacaoNuclearRepository aplicacoes, AreaAplicacaoRepository areas, AplicacaoImpactoRepository vinculos) {
        this.vinculos = vinculos;
        this.aplicacoes = aplicacoes;
        this.areas = areas;
    }

    public AplicacaoResponse criar(AplicacaoRequest entrada) {
        var aplicacao = new AplicacaoNuclear();
        preencher(aplicacao, entrada);
        return AplicacaoResponse.from(aplicacoes.saveAndFlush(aplicacao));
    }

    public AplicacaoResponse substituir(Long id, AplicacaoRequest entrada) {
        var aplicacao = aplicacoes.findById(id).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Aplicação não encontrada."));
        preencher(aplicacao, entrada);
        return AplicacaoResponse.from(aplicacoes.saveAndFlush(aplicacao));
    }

    public void excluir(Long id) {
        var aplicacao = aplicacoes.findById(id).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Aplicação não encontrada."));
        if (vinculos.existsByAplicacao_Id(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Aplicação possui vínculos com impactos sociais e não pode ser excluída.");
        }
        aplicacoes.delete(aplicacao);
        aplicacoes.flush();
    }

    private void preencher(AplicacaoNuclear aplicacao, AplicacaoRequest entrada) {
        var area = areas.findById(entrada.areaAplicacaoId()).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Área de aplicação não encontrada."));
        aplicacao.setAreaAplicacao(area);
        aplicacao.setTitulo(entrada.titulo());
        aplicacao.setResumo(entrada.resumo());
        aplicacao.setDescricao(entrada.descricao());
    }
}
