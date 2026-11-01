package br.com.nexo.api.service;

import br.com.nexo.api.dto.AreaResponse;
import br.com.nexo.api.dto.PaginaResponse;
import br.com.nexo.api.repository.AreaAplicacaoRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AreaConsultaService {
    private final AreaAplicacaoRepository areas;

    public AreaConsultaService(AreaAplicacaoRepository areas) {
        this.areas = areas;
    }

    public PaginaResponse<AreaResponse> listar(int page, int size) {
        return PaginaResponse.from(areas.findAll(PageRequest.of(page, size, Sort.by("id").ascending()))
            .map(AreaResponse::from));
    }
}
