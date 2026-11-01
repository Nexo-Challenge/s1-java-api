package br.com.nexo.api.repository;

import br.com.nexo.api.model.AreaAplicacao;
import java.util.Optional;
import org.springframework.data.repository.Repository;

public interface AreaAplicacaoRepository extends Repository<AreaAplicacao, Long> {
    org.springframework.data.domain.Page<AreaAplicacao> findAll(org.springframework.data.domain.Pageable pageable);
    Optional<AreaAplicacao> findById(Long id);
}
