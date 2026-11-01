package br.com.nexo.api;

import br.com.nexo.api.model.AreaAplicacao;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Requisições sem transação externa: verifica commits e rollbacks reais no H2. */
@SpringBootTest
class AplicacaoCrudTest extends IsolatedDatabaseTest {
    @Autowired WebApplicationContext context;
    @Autowired EntityManager em;
    @Autowired PlatformTransactionManager transactionManager;
    MockMvc mvc;
    TransactionTemplate tx;
    Long areaId;

    @BeforeEach void preparar() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
        tx = new TransactionTemplate(transactionManager);
        limpar();
        areaId = tx.execute(status -> {
            var area = new AreaAplicacao();
            area.setNome("Área sintética");
            em.persist(area);
            return area.getId();
        });
    }

    @AfterEach void limpar() {
        tx.executeWithoutResult(status -> {
            em.createQuery("delete from AplicacaoImpacto").executeUpdate();
            em.createQuery("delete from AplicacaoNuclear").executeUpdate();
            em.createQuery("delete from ImpactoSocial").executeUpdate();
            em.createQuery("delete from AreaAplicacao").executeUpdate();
        });
    }

    @Test void substituiPreservandoIdentidadeEAtualizaCampos() throws Exception {
        String location = criar();
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put(location)
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo("Título atualizado", "Resumo atualizado", "Descrição atualizada")))
            .andExpect(status().isOk()).andExpect(jsonPath("$.titulo").value("Título atualizado"));
        mvc.perform(get(location)).andExpect(status().isOk())
            .andExpect(jsonPath("$.resumo").value("Resumo atualizado"))
            .andExpect(jsonPath("$.descricao").value("Descrição atualizada"));
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put(location)
                .contentType(MediaType.APPLICATION_JSON).content(corpo("Substituído", "Novo resumo", "Nova descrição")))
            .andExpect(status().isOk()).andExpect(jsonPath("$.titulo").value("Substituído"));
        mvc.perform(get(location)).andExpect(status().isOk()).andExpect(jsonPath("$.titulo").value("Substituído"));
    }

    @Test void cicloCrudCompletoTerminaEm404() throws Exception {
        String location = criar();
        mvc.perform(get(location)).andExpect(status().isOk());
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put(location)
                .contentType(MediaType.APPLICATION_JSON).content(corpo("Atualizada")))
            .andExpect(status().isOk());
        mvc.perform(get(location)).andExpect(jsonPath("$.titulo").value("Atualizada"));
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete(location))
            .andExpect(status().isNoContent()).andExpect(content().string(""));
        mvc.perform(get(location)).andExpect(status().isNotFound());
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete(location))
            .andExpect(status().isNotFound());
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.CsvSource({"titulo,Título é obrigatório e não pode estar em branco.", "resumo,Resumo é obrigatório e não pode estar em branco.", "descricao,Descrição é obrigatória e não pode estar em branco."})
    void rejeitaCamposObrigatoriosEmBranco(String campo, String mensagemEsperada) throws Exception {
        String t = campo.equals("titulo") ? "   " : "Título válido";
        String r = campo.equals("resumo") ? "   " : "Resumo válido";
        String d = campo.equals("descricao") ? "   " : "Descrição válida";
        mvc.perform(post("/api/aplicacoes").contentType(MediaType.APPLICATION_JSON).content(corpo(t, r, d)))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
            .andExpect(jsonPath("$.errors[0].campo").value(campo))
            .andExpect(jsonPath("$.errors[0].mensagem").value(mensagemEsperada));
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.CsvSource({"titulo,150", "resumo,500", "descricao,4000"})
    void validaLimiteExatoEExcedido(String campo, int limite) throws Exception {
        for (int tamanho : new int[]{limite, limite + 1}) {
            String t = campo.equals("titulo") ? "x".repeat(tamanho) : "Título válido";
            String r = campo.equals("resumo") ? "x".repeat(tamanho) : "Resumo válido";
            String d = campo.equals("descricao") ? "x".repeat(tamanho) : "Descrição válida";
            String payload = corpo(t, r, d);
            var resultado = mvc.perform(post("/api/aplicacoes").contentType(MediaType.APPLICATION_JSON).content(payload));
            if (tamanho == limite) resultado.andExpect(status().isCreated());
            else resultado.andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors[0].campo").value(campo));
        }
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(strings = {"null", "0", "-1"})
    void rejeitaAreaNulaOuNaoPositiva(String area) throws Exception {
        mvc.perform(post("/api/aplicacoes").contentType(MediaType.APPLICATION_JSON)
                .content("{\"titulo\":\"Teste\",\"resumo\":\"Resumo\",\"descricao\":\"Descricao\",\"areaAplicacaoId\":" + area + "}"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors[0].campo").value("areaAplicacaoId"));
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(strings = {"0", "-1", "abc", "9223372036854775808"})
    void rejeitaIdsInvalidosEmTodasOperacoes(String id) throws Exception {
        String url = "/api/aplicacoes/" + id;
        mvc.perform(get(url)).andExpect(status().isBadRequest());
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put(url)
            .contentType(MediaType.APPLICATION_JSON).content(corpo("Teste"))).andExpect(status().isBadRequest());
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete(url))
            .andExpect(status().isBadRequest());
    }

    @Test void exclusaoComVinculoRetorna409EPreservaFilhos() throws Exception {
        String location = criar();
        long id = Long.parseLong(location.substring(location.lastIndexOf('/') + 1));
        var chave = tx.execute(status -> {
            var impacto = new br.com.nexo.api.model.ImpactoSocial();
            impacto.setTitulo("Impacto sintético");
            em.persist(impacto);
            var vinculo = new br.com.nexo.api.model.AplicacaoImpacto();
            vinculo.setAplicacao(em.find(br.com.nexo.api.model.AplicacaoNuclear.class, id));
            vinculo.setImpacto(impacto);
            vinculo.setExplicacao("Explicação sintética");
            em.persist(vinculo);
            em.flush();
            return vinculo.getId();
        });
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put(location)
                .contentType(MediaType.APPLICATION_JSON).content(corpo("Atualizada com vínculo")))
            .andExpect(status().isOk()).andExpect(jsonPath("$.id").value((int) id));
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete(location))
            .andExpect(status().isConflict()).andExpect(jsonPath("$.detail")
                .value("Aplicação possui vínculos com impactos sociais e não pode ser excluída."));
        mvc.perform(get(location)).andExpect(status().isOk());
        tx.executeWithoutResult(status -> {
            var filho = em.find(br.com.nexo.api.model.AplicacaoImpacto.class, chave);
            org.junit.jupiter.api.Assertions.assertNotNull(filho);
            org.junit.jupiter.api.Assertions.assertEquals("Explicação sintética", filho.getExplicacao());
            org.junit.jupiter.api.Assertions.assertEquals(id, filho.getAplicacao().getId());
            org.junit.jupiter.api.Assertions.assertNotNull(em.find(br.com.nexo.api.model.ImpactoSocial.class, chave.getImpactoId()));
        });
    }

    @Test void consultaAreasPaginadasPorIdSemEntidades() throws Exception {
        Long segunda = tx.execute(status -> {
            var area = new AreaAplicacao(); area.setNome("Segunda área sintética"); em.persist(area); return area.getId();
        });
        mvc.perform(get("/api/areas").param("size", "1"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].id").value(areaId.intValue()))
            .andExpect(jsonPath("$.content[0].nome").value("Área sintética"))
            .andExpect(jsonPath("$.totalElements").value(2)).andExpect(jsonPath("$.totalPages").value(2));
        mvc.perform(get("/api/areas").param("page", "1").param("size", "1"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].id").value(segunda.intValue()));
        mvc.perform(get("/api/areas").param("page", "2").param("size", "1"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.content").isEmpty());
        for (String size : new String[]{"0", "101", "abc"}) {
            mvc.perform(get("/api/areas").param("size", size)).andExpect(status().isBadRequest());
        }
        mvc.perform(get("/api/areas").param("page", "-1")).andExpect(status().isBadRequest());
        mvc.perform(post("/api/areas")).andExpect(status().isMethodNotAllowed());
    }

    @Test void errosDeParametrosECorpoSaoPortuguesesESemDetalhesInternos() throws Exception {
        mvc.perform(get("/api/aplicacoes/0"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors[0].campo").value("id"))
            .andExpect(jsonPath("$.errors[0].mensagem").value("ID deve ser positivo."));
        mvc.perform(get("/api/aplicacoes/abc"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.detail").value("Parâmetro com tipo inválido."));
        mvc.perform(post("/api/aplicacoes").contentType(MediaType.APPLICATION_JSON).content("{"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.detail").value("Corpo JSON ausente, inválido ou com tipos incompatíveis."));
        String location = criar();
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put(location)
                .contentType(MediaType.APPLICATION_JSON).content(corpo(" ")))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors[0].campo").value("titulo"));
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(strings = {"", "null", "[]", "{}", "{\"titulo\":null}", "{\"titulo\":{}}", "{\"areaAplicacaoId\":\"abc\",\"titulo\":\"Teste\"}"})
    void rejeitaCorposInvalidos(String payload) throws Exception {
        mvc.perform(post("/api/aplicacoes").contentType(MediaType.APPLICATION_JSON).content(payload))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400));
    }

    @Test void areaAusenteRetorna404SemCriarOuAlterarAplicacao() throws Exception {
        String payload = "{\"areaAplicacaoId\":" + Long.MAX_VALUE + ",\"titulo\":\"Teste\",\"resumo\":\"Resumo\",\"descricao\":\"Descricao\"}";
        mvc.perform(post("/api/aplicacoes").contentType(MediaType.APPLICATION_JSON).content(payload))
            .andExpect(status().isNotFound()).andExpect(jsonPath("$.detail").value("Área de aplicação não encontrada."));
        mvc.perform(get("/api/aplicacoes")).andExpect(jsonPath("$.totalElements").value(0));
        String location = criar();
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put(location)
            .contentType(MediaType.APPLICATION_JSON).content(payload)).andExpect(status().isNotFound());
        mvc.perform(get(location)).andExpect(jsonPath("$.titulo").value("Aplicação sintética"));
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/aplicacoes/{id}", Long.MAX_VALUE)
            .contentType(MediaType.APPLICATION_JSON).content(corpo("Teste"))).andExpect(status().isNotFound());
    }

    @Test void rejeitaTiposIncompativeisNoCorpo() throws Exception {
        for (String payload : new String[]{
                "{\"areaAplicacaoId\":true,\"titulo\":\"Teste\",\"resumo\":\"Resumo\",\"descricao\":\"Descricao\"}",
                "{\"areaAplicacaoId\":[1],\"titulo\":\"Teste\",\"resumo\":\"Resumo\",\"descricao\":\"Descricao\"}",
                "{\"areaAplicacaoId\":{\"id\":1},\"titulo\":\"Teste\",\"resumo\":\"Resumo\",\"descricao\":\"Descricao\"}",
                "{\"areaAplicacaoId\":" + areaId + ",\"titulo\":[\"Teste\"],\"resumo\":\"Resumo\",\"descricao\":\"Descricao\"}"}
        ) {
            mvc.perform(post("/api/aplicacoes").contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isBadRequest());
        }
    }

    String corpo(String titulo) {
        return corpo(titulo, "Resumo sintético", "Descrição sintética");
    }

    String corpo(String titulo, String resumo, String descricao) {
        return "{\"areaAplicacaoId\":" + areaId + ",\"titulo\":\"" + titulo + "\",\"resumo\":\"" + resumo + "\",\"descricao\":\"" + descricao + "\"}";
    }

    String criar() throws Exception {
        return mvc.perform(post("/api/aplicacoes").contentType(MediaType.APPLICATION_JSON)
                .content(corpo("Aplicação sintética")))
            .andExpect(status().isCreated()).andExpect(header().exists("Location"))
            .andExpect(jsonPath("$.id").value(greaterThan(0)))
            .andExpect(jsonPath("$.area.id").value(areaId.intValue()))
            .andExpect(jsonPath("$.titulo").value("Aplicação sintética"))
            .andExpect(jsonPath("$.impactos").doesNotExist())
            .andReturn().getResponse().getHeader("Location");
    }

    @Test void criaComIdentityELocationEConsultaAposCommit() throws Exception {
        String location = criar();
        mvc.perform(get(location)).andExpect(status().isOk())
            .andExpect(jsonPath("$.titulo").value("Aplicação sintética"));
    }
}
