package br.com.nexo.api.controller;

import br.com.nexo.api.dto.AreaResponse;
import br.com.nexo.api.dto.PaginaResponse;
import br.com.nexo.api.service.AreaConsultaService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/areas")
public class AreaController {
    private final AreaConsultaService consulta;

    public AreaController(AreaConsultaService consulta) {
        this.consulta = consulta;
    }

    @GetMapping
    public PaginaResponse<AreaResponse> listar(
            @Min(value = 0, message = "Página deve ser maior ou igual a zero.") @RequestParam(defaultValue = "0") int page,
            @Min(value = 1, message = "Tamanho deve ser pelo menos 1.")
            @Max(value = 100, message = "Tamanho deve ser no máximo 100.") @RequestParam(defaultValue = "20") int size) {
        return consulta.listar(page, size);
    }
}
