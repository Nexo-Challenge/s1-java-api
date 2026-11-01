package br.com.nexo.api.controller;
import br.com.nexo.api.dto.*;
import br.com.nexo.api.service.AplicacaoConsultaService;
import br.com.nexo.api.service.AplicacaoEscritaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/aplicacoes")
public class AplicacaoController {
    private final AplicacaoConsultaService service;
    private final AplicacaoEscritaService escrita;
    public AplicacaoController(AplicacaoConsultaService service, AplicacaoEscritaService escrita) {
        this.service = service;
        this.escrita = escrita;
    }
    @PutMapping("/{id}")
    public AplicacaoResponse substituir(@jakarta.validation.constraints.Positive(message = "ID deve ser positivo.") @PathVariable Long id, @Valid @RequestBody AplicacaoRequest entrada) {
        return escrita.substituir(id, entrada);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@jakarta.validation.constraints.Positive(message = "ID deve ser positivo.") @PathVariable Long id) {
        escrita.excluir(id);
        return ResponseEntity.noContent().build();
    }
    @PostMapping
    public ResponseEntity<AplicacaoResponse> criar(@Valid @RequestBody AplicacaoRequest entrada) {
        var resposta = escrita.criar(entrada);
        var location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
            .buildAndExpand(resposta.id()).toUri();
        return ResponseEntity.created(location).body(resposta);
    }
    @GetMapping("/{id}")
    public AplicacaoResponse buscar(@jakarta.validation.constraints.Positive(message = "ID deve ser positivo.") @PathVariable Long id) { return service.buscar(id); }
    @GetMapping
    public PaginaResponse<AplicacaoResponse> listar(@jakarta.validation.constraints.Min(0) @RequestParam(defaultValue = "0") int page,
            @jakarta.validation.constraints.Min(1) @jakarta.validation.constraints.Max(100) @RequestParam(defaultValue = "20") int size, @RequestParam(required = false) String titulo) {
        return service.listar(titulo, page, size);
    }
}
