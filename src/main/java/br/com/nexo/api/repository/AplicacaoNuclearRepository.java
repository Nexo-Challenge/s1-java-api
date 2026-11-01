package br.com.nexo.api.repository;
import br.com.nexo.api.model.AplicacaoNuclear;
import org.springframework.data.repository.Repository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


/** Expõe somente leituras; carrega apenas a área N:1, nunca coleções. */
public interface AplicacaoNuclearRepository extends Repository<AplicacaoNuclear, Long> {
    @EntityGraph(attributePaths = "areaAplicacao")
    Page<AplicacaoNuclear> findAll(Pageable pageable);
    @EntityGraph(attributePaths = "areaAplicacao")
    java.util.Optional<AplicacaoNuclear> findById(Long id);
    @EntityGraph(attributePaths = "areaAplicacao")
    Page<AplicacaoNuclear> findByTituloContainingIgnoreCase(String titulo, Pageable pageable);
}
