package br.com.nexo.api.service;
import br.com.nexo.api.dto.*;
import br.com.nexo.api.repository.AplicacaoNuclearRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
@Service
@Transactional(readOnly = true)
public class AplicacaoConsultaService {
    private final AplicacaoNuclearRepository repository;
    public AplicacaoConsultaService(AplicacaoNuclearRepository repository) { this.repository = repository; }
    public AplicacaoResponse buscar(Long id) {
        return repository.findById(id).map(AplicacaoResponse::from).orElseThrow(() ->
            new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Aplicacao nao encontrada"));
    }
    public PaginaResponse<AplicacaoResponse> listar(String titulo, int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        var result = titulo == null || titulo.isBlank() ? repository.findAll(pageable)
            : repository.findByTituloContainingIgnoreCase(titulo.trim(), pageable);
        return PaginaResponse.from(result.map(AplicacaoResponse::from));
    }
}
