package br.com.nexo.api;

import br.com.nexo.api.model.AplicacaoImpacto;
import br.com.nexo.api.model.AplicacaoNuclear;
import br.com.nexo.api.model.AreaAplicacao;
import br.com.nexo.api.model.ImpactoSocial;
import br.com.nexo.api.repository.AplicacaoImpactoRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class AplicacaoIntegridadeTest extends IsolatedDatabaseTest {
    @Autowired EntityManager em;
    @Autowired PlatformTransactionManager transactionManager;
    @Autowired WebApplicationContext context;
    @MockitoSpyBean AplicacaoImpactoRepository vinculos;

    @Test void fkRealProtegeQuandoPrecheckNaoVeVinculo() throws Exception {
        var tx = new TransactionTemplate(transactionManager);
        var chave = tx.execute(status -> {
            var area = new AreaAplicacao();
            area.setNome("Área de integridade sintética");
            em.persist(area);
            var aplicacao = new AplicacaoNuclear();
            aplicacao.setAreaAplicacao(area);
            aplicacao.setTitulo("Aplicação sintética");
            aplicacao.setResumo("Resumo sintético");
            aplicacao.setDescricao("Descrição sintética");
            em.persist(aplicacao);
            var impacto = new ImpactoSocial();
            impacto.setTitulo("Impacto de integridade sintético");
            em.persist(impacto);
            var vinculo = new AplicacaoImpacto();
            vinculo.setAplicacao(aplicacao);
            vinculo.setImpacto(impacto);
            vinculo.setExplicacao("Explicação sintética de integridade");
            em.persist(vinculo);
            em.flush();
            return vinculo.getId();
        });
        try {
            // Simula leitura anterior ao vínculo concorrente, sem simular a falha SQL.
            doReturn(false).when(vinculos).existsByAplicacao_Id(chave.getAplicacaoId());
            MockMvcBuilders.webAppContextSetup(context).build()
                .perform(delete("/api/aplicacoes/{id}", chave.getAplicacaoId()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Operação não permitida por uma restrição de integridade dos dados."));
            tx.executeWithoutResult(status -> {
                assertNotNull(em.find(AplicacaoNuclear.class, chave.getAplicacaoId()));
                assertNotNull(em.find(AplicacaoImpacto.class, chave));
                assertNotNull(em.find(ImpactoSocial.class, chave.getImpactoId()));
            });
        } finally {
            tx.executeWithoutResult(status -> {
                em.createQuery("delete from AplicacaoImpacto").executeUpdate();
                em.createQuery("delete from AplicacaoNuclear").executeUpdate();
                em.createQuery("delete from ImpactoSocial").executeUpdate();
                em.createQuery("delete from AreaAplicacao").executeUpdate();
            });
        }
    }
}
