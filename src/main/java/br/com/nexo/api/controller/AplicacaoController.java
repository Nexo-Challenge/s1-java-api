package br.com.nexo.api.controller;
import br.com.nexo.api.dto.*;
import br.com.nexo.api.service.AplicacaoConsultaService;
import br.com.nexo.api.service.AplicacaoEscritaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.bind.annotation.*;
@Tag(name = "Aplicações Nucleares", description = "Endpoints de gerenciamento e consulta de aplicações da tecnologia nuclear")
@RestController
@RequestMapping("/api/aplicacoes")
public class AplicacaoController {
    private final AplicacaoConsultaService service;
    private final AplicacaoEscritaService escrita;
    public AplicacaoController(AplicacaoConsultaService service, AplicacaoEscritaService escrita) {
        this.service = service;
        this.escrita = escrita;
    }
    @Operation(summary = "Substitui uma aplicação nuclear existente por ID (atualização completa)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Aplicação atualizada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
        @ApiResponse(responseCode = "404", description = "Aplicação não encontrada"),
        @ApiResponse(responseCode = "409", description = "Conflito de integridade (ex.: título duplicado)")
    })
    @PutMapping("/{id}")
    public AplicacaoResponse substituir(@Parameter(description = "ID da aplicação nuclear", example = "1") @jakarta.validation.constraints.Positive(message = "ID deve ser positivo.") @PathVariable Long id, @Valid @RequestBody AplicacaoRequest entrada) {
        return escrita.substituir(id, entrada);
    }
    @Operation(summary = "Exclui uma aplicação nuclear por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Aplicação excluída com sucesso"),
        @ApiResponse(responseCode = "404", description = "Aplicação não encontrada"),
        @ApiResponse(responseCode = "409", description = "Conflito de integridade ao tentar excluir")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@Parameter(description = "ID da aplicação nuclear", example = "1") @jakarta.validation.constraints.Positive(message = "ID deve ser positivo.") @PathVariable Long id) {
        escrita.excluir(id);
        return ResponseEntity.noContent().build();
    }
    @Operation(summary = "Cadastra uma nova aplicação nuclear")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Aplicação criada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
        @ApiResponse(responseCode = "409", description = "Conflito de integridade (ex.: título duplicado)")
    })
    @PostMapping
    public ResponseEntity<AplicacaoResponse> criar(@Valid @RequestBody AplicacaoRequest entrada) {
        var resposta = escrita.criar(entrada);
        var location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
            .buildAndExpand(resposta.id()).toUri();
        return ResponseEntity.created(location).body(resposta);
    }
    @Operation(summary = "Busca os detalhes de uma aplicação nuclear por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Aplicação localizada com sucesso"),
        @ApiResponse(responseCode = "404", description = "Aplicação não encontrada")
    })
    @GetMapping("/{id}")
    public AplicacaoResponse buscar(@Parameter(description = "ID da aplicação nuclear", example = "1") @jakarta.validation.constraints.Positive(message = "ID deve ser positivo.") @PathVariable Long id) { return service.buscar(id); }
    @Operation(summary = "Lista aplicações nucleares com paginação e filtro opcional por título")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Página de aplicações retornada com sucesso")
    })
    @GetMapping
    public PaginaResponse<AplicacaoResponse> listar(@Parameter(description = "Número da página (inicia em 0)", example = "0") @jakarta.validation.constraints.Min(0) @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Quantidade de registros por página", example = "20") @jakarta.validation.constraints.Min(1) @jakarta.validation.constraints.Max(100) @RequestParam(defaultValue = "20") int size, @Parameter(description = "Filtro parcial de título da aplicação (opcional)", example = "Propulsão") @RequestParam(required = false) String titulo) {
        return service.listar(titulo, page, size);
    }
}
