package br.com.nexo.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.transaction.annotation.Transactional;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
class AplicacaoConsultaTest extends IsolatedDatabaseTest {
    @Autowired WebApplicationContext context;
    MockMvc mvc;
    @BeforeEach void setup() { mvc = MockMvcBuilders.webAppContextSetup(context).build(); }

    @Autowired jakarta.persistence.EntityManager em;
    private Long areaId;
    private java.util.List<Long> ids;
    private void seed() {
        var area = new br.com.nexo.api.model.AreaAplicacao();
        area.setNome("Area sintetica"); area.setDescricao("Descricao area"); em.persist(area);
        var aplicacoes = new java.util.ArrayList<br.com.nexo.api.model.AplicacaoNuclear>();
        for (String titulo : new String[]{"Teste NUCLEAR Z", "Outro", "Teste NUCLEAR A"}) {
            var app = new br.com.nexo.api.model.AplicacaoNuclear();
            app.setAreaAplicacao(area);
            app.setTitulo(titulo);
            app.setResumo("Resumo sintetico"); app.setDescricao("Descricao sintetica"); em.persist(app);
            aplicacoes.add(app);
        }
        em.flush();
        areaId = area.getId();
        ids = aplicacoes.stream().map(br.com.nexo.api.model.AplicacaoNuclear::getId).sorted().toList();
        em.clear();
    }
    @Test void filtraTituloIgnorandoCaixaEPaginaPorId() throws Exception {
        seed();
        mvc.perform(get("/api/aplicacoes").param("titulo", "nuclear").param("page", "1").param("size", "1"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].id").value(equalTo(ids.get(2)), Long.class))
            .andExpect(jsonPath("$.content[0].area.nome").value("Area sintetica"))
            .andExpect(jsonPath("$.totalElements").value(2)).andExpect(jsonPath("$.totalPages").value(2));
    }
    @Test void buscaPorIdComoDto() throws Exception {
        seed();
        mvc.perform(get("/api/aplicacoes/{id}", ids.get(0)))
            .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(equalTo(ids.get(0)), Long.class))
            .andExpect(jsonPath("$.area.id").value(equalTo(areaId), Long.class))
            .andExpect(jsonPath("$.resumo").value("Resumo sintetico"))
            .andExpect(jsonPath("$.descricao").value("Descricao sintetica"))
            .andExpect(jsonPath("$.impactos").doesNotExist());
    }
    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.CsvSource({"page,-1", "size,0", "size,101", "page,abc", "size,abc"})
    void rejeitaPaginacaoInvalida(String parameter, String value) throws Exception {
        mvc.perform(get("/api/aplicacoes").param(parameter, value))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
            .andExpect(jsonPath("$.status").value(400));
    }
    @Test void ausenciaRetorna404Estruturado() throws Exception {
        mvc.perform(get("/api/aplicacoes/999"))
            .andExpect(status().isNotFound())
            .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
            .andExpect(jsonPath("$.status").value(404));
    }
    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(ints = {1, 100})
    void aceitaLimitesDoTamanhoDaPagina(int size) throws Exception {
        mvc.perform(get("/api/aplicacoes").param("size", Integer.toString(size)))
            .andExpect(status().isOk()).andExpect(jsonPath("$.size").value(size));
    }
    @Test void tituloEmBrancoListaTudoOrdenadoPorId() throws Exception {
        seed();
        mvc.perform(get("/api/aplicacoes").param("titulo", "   "))
            .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].id").value(equalTo(ids.get(0)), Long.class))
            .andExpect(jsonPath("$.content[1].id").value(equalTo(ids.get(1)), Long.class))
            .andExpect(jsonPath("$.content[2].id").value(equalTo(ids.get(2)), Long.class))
            .andExpect(jsonPath("$.totalElements").value(3));
    }
    @Test void paginaAlemDoFimVazia() throws Exception {
        seed();
        mvc.perform(get("/api/aplicacoes").param("page", "20").param("size", "1"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.content").isEmpty())
            .andExpect(jsonPath("$.totalElements").value(3));
    }
    @Test void curingasSaoLiteraisESemResultadoRetornaVazio() throws Exception {
        seed();
        mvc.perform(get("/api/aplicacoes").param("titulo", "%"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.content").isEmpty());
    }
    @Test void idInvalidoRetorna400Estruturado() throws Exception {
        mvc.perform(get("/api/aplicacoes/abc"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400));
    }
    @Test void patchNaoEExposto() throws Exception {
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/api/aplicacoes"))
            .andExpect(status().isMethodNotAllowed());
    }
    @Test void dtoPermaneceUtilizavelAposTransacao() {
        seed();
        var service = context.getBean(br.com.nexo.api.service.AplicacaoConsultaService.class);
        var dto = service.buscar(ids.get(0));
        org.springframework.test.context.transaction.TestTransaction.end();
        org.junit.jupiter.api.Assertions.assertEquals("Area sintetica", dto.area().nome());
        org.junit.jupiter.api.Assertions.assertEquals("Descricao area", dto.area().descricao());
    }
    @Test void preservaNomeLiteralDaColunaDeAreaNoSql() {
        java.util.List<?> columns = em.createNativeQuery(
            "select column_name from information_schema.columns where table_name = 'TB_APLICACAO_NUCLEAR'")
            .getResultList();
        org.junit.jupiter.api.Assertions.assertTrue(columns.contains("AREA_APLICACAO_ID"), columns.toString());
        org.junit.jupiter.api.Assertions.assertFalse(columns.contains("AREAAPLICACAOID"));
    }
    @Test void listaPaginaVazia() throws Exception {
        mvc.perform(get("/api/aplicacoes"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isEmpty())
            .andExpect(jsonPath("$.page").value(0))
            .andExpect(jsonPath("$.size").value(20))
            .andExpect(jsonPath("$.totalElements").value(0));
    }
}
