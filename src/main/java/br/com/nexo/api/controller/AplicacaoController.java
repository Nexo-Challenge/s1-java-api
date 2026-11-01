package br.com.nexo.api.controller;
import br.com.nexo.api.dto.*;
import br.com.nexo.api.service.AplicacaoConsultaService;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/aplicacoes")
public class AplicacaoController {
    private final AplicacaoConsultaService service;
    public AplicacaoController(AplicacaoConsultaService service) { this.service = service; }
    @GetMapping("/{id}")
    public AplicacaoResponse buscar(@PathVariable Long id) { return service.buscar(id); }
    @GetMapping
    public PaginaResponse<AplicacaoResponse> listar(@jakarta.validation.constraints.Min(0) @RequestParam(defaultValue = "0") int page,
            @jakarta.validation.constraints.Min(1) @jakarta.validation.constraints.Max(100) @RequestParam(defaultValue = "20") int size, @RequestParam(required = false) String titulo) {
        return service.listar(titulo, page, size);
    }
}
