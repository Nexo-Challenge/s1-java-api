package br.com.nexo.api;

import br.com.nexo.api.model.AplicacaoImpacto;
import br.com.nexo.api.model.AplicacaoImpactoId;
import br.com.nexo.api.model.AplicacaoNuclear;
import br.com.nexo.api.model.AreaAplicacao;
import br.com.nexo.api.model.ImpactoSocial;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/** Verifica geração e associação apenas no H2 isolado, não no Oracle. */
@SpringBootTest
@Transactional
class IdentidadePersistenciaTest extends IsolatedDatabaseTest {
    @Autowired EntityManager em;

    @Test
    void geraIdsSimplesEPersisteAssociacaoComChaveDosPais() {
        var areas = new AreaAplicacao[]{new AreaAplicacao(), new AreaAplicacao()};
        var aplicacoes = new AplicacaoNuclear[]{new AplicacaoNuclear(), new AplicacaoNuclear()};
        var impactos = new ImpactoSocial[]{new ImpactoSocial(), new ImpactoSocial()};
        for (int i = 0; i < areas.length; i++) {
            areas[i].setNome("Area sintetica " + i);
            aplicacoes[i].setAreaAplicacao(areas[i]);
            aplicacoes[i].setTitulo("Aplicacao sintetica " + i);
            aplicacoes[i].setResumo("Resumo sintetico " + i);
            aplicacoes[i].setDescricao("Descricao sintetica " + i);
            impactos[i].setTitulo("Impacto sintetico " + i);
            assertNull(areas[i].getId());
            assertNull(aplicacoes[i].getId());
            assertNull(impactos[i].getId());
            em.persist(areas[i]);
            em.persist(aplicacoes[i]);
            em.persist(impactos[i]);
        }
        em.flush();
        for (int i = 0; i < areas.length; i++) {
            assertNotNull(areas[i].getId());
            assertNotNull(aplicacoes[i].getId());
            assertNotNull(impactos[i].getId());
        }
        // Unicidade dentro de cada tabela; tabelas distintas podem repetir IDs.
        assertNotEquals(areas[0].getId(), areas[1].getId());
        assertNotEquals(aplicacoes[0].getId(), aplicacoes[1].getId());
        assertNotEquals(impactos[0].getId(), impactos[1].getId());

        var vinculo = new AplicacaoImpacto();
        vinculo.setAplicacao(aplicacoes[0]);
        vinculo.setImpacto(impactos[1]);
        vinculo.setExplicacao("Explicacao sintetica");
        vinculo.setFonteUrl("https://example.org/fonte");
        em.persist(vinculo);
        em.flush();
        var chave = new AplicacaoImpactoId(aplicacoes[0].getId(), impactos[1].getId());
        assertEquals(chave, vinculo.getId());
        em.clear();

        var recuperado = em.find(AplicacaoImpacto.class, chave);
        assertNotNull(recuperado);
        assertEquals(chave, recuperado.getId());
        assertEquals(aplicacoes[0].getId(), recuperado.getAplicacao().getId());
        assertEquals(impactos[1].getId(), recuperado.getImpacto().getId());
        assertEquals(areas[0].getId(), recuperado.getAplicacao().getAreaAplicacao().getId());
        assertEquals("Area sintetica 0", recuperado.getAplicacao().getAreaAplicacao().getNome());
        assertEquals("Aplicacao sintetica 0", recuperado.getAplicacao().getTitulo());
        assertEquals("Impacto sintetico 1", recuperado.getImpacto().getTitulo());
        assertEquals("Explicacao sintetica", recuperado.getExplicacao());
        assertEquals("https://example.org/fonte", recuperado.getFonteUrl());
    }
}
