package br.com.nexo.api.controller;

import br.com.nexo.api.dto.AreaResponse;
import br.com.nexo.api.dto.PaginaResponse;
import br.com.nexo.api.service.AreaConsultaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Áreas de Aplicação", description = "Endpoints de consulta de áreas de aplicação da tecnologia nuclear (ex: Saúde, Defesa, Agricultura)")
@RestController
@RequestMapping("/api/areas")
public class AreaController {
    private final AreaConsultaService consulta;

    public AreaController(AreaConsultaService consulta) {
        this.consulta = consulta;
    }

    @Operation(summary = "Lista todas as áreas de aplicação com paginação")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Página de áreas de aplicação retornada com sucesso")
    })
    @GetMapping
    public PaginaResponse<AreaResponse> listar(
            @Parameter(description = "Número da página (inicia em 0)", example = "0")
            @Min(value = 0, message = "Página deve ser maior ou igual a zero.") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Quantidade de registros por página", example = "20")
            @Min(value = 1, message = "Tamanho deve ser pelo menos 1.")
            @Max(value = 100, message = "Tamanho deve ser no máximo 100.") @RequestParam(defaultValue = "20") int size) {
        return consulta.listar(page, size);
    }
}
