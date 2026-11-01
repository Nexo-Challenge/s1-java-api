package br.com.nexo.api;

import br.com.nexo.api.repository.AplicacaoNuclearRepository;
import br.com.nexo.api.service.AplicacaoConsultaService;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AplicacaoConsultaServiceTest {
    @Test void falhaDeBancoNaoViraSucessoVazio() {
        var repository = mock(AplicacaoNuclearRepository.class);
        var failure = new DataAccessResourceFailureException("banco indisponivel sintetico");
        when(repository.findAll(any())).thenThrow(failure);
        var service = new AplicacaoConsultaService(repository);
        assertSame(failure, assertThrows(DataAccessResourceFailureException.class,
            () -> service.listar(null, 0, 20)));
    }
    @Test void servicoDeclaraTransacaoSomenteLeitura() {
        assertTrue(AplicacaoConsultaService.class.getAnnotation(Transactional.class).readOnly());
    }
}
