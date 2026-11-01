package br.com.nexo.api.repository;

import br.com.nexo.api.model.AplicacaoImpacto;
import br.com.nexo.api.model.AplicacaoImpactoId;
import org.springframework.data.repository.Repository;

/** Consulta de vínculos; não expõe escrita nem remoção de associações. */
public interface AplicacaoImpactoRepository extends Repository<AplicacaoImpacto, AplicacaoImpactoId> {
    boolean existsByAplicacao_Id(Long aplicacaoId);
}
