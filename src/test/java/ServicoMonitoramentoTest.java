import dominio.Alerta;
import dominio.DadosTerritorio;
import dominio.Medicao;
import dominio.NivelAlerta;
import dominio.PrevisaoMediaMovel;
import dominio.SensorNivelRio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import persistencia.AlertaDAO;
import persistencia.MedicaoDAO;
import servico.ResultadoPrevisao;
import servico.ServicoMonitoramento;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Testa a lógica de alerta sem banco: os DAOs são mocks do Mockito. */
@ExtendWith(MockitoExtension.class)
class ServicoMonitoramentoTest {

    @Mock
    private MedicaoDAO medicaoDAO;

    @Mock
    private AlertaDAO alertaDAO;

    private List<Medicao> tresLeituras(double valor) throws Exception {
        return List.of(
                new Medicao("RIO-01", valor, LocalDateTime.now()),
                new Medicao("RIO-01", valor, LocalDateTime.now()),
                new Medicao("RIO-01", valor, LocalDateTime.now()));
    }

    @Test
    void previsaoAcimaDoLimiteGravaAlertaDeNivelAlerta() throws Exception {
        // Arrange: média 400 fica entre ALERTA (350) e CRÍTICO (450)
        when(medicaoDAO.buscarUltimas("RIO-01", 3)).thenReturn(tresLeituras(400));
        ServicoMonitoramento servico = new ServicoMonitoramento(medicaoDAO, alertaDAO, new DadosTerritorio());

        // Act
        ResultadoPrevisao resultado = servico.preverEAlertar(new SensorNivelRio("RIO-01"), new PrevisaoMediaMovel(3));

        // Assert
        assertEquals(400.0, resultado.valorPrevisto(), 0.0001);
        assertTrue(resultado.alerta().isPresent());
        ArgumentCaptor<Alerta> capturado = ArgumentCaptor.forClass(Alerta.class);
        verify(alertaDAO).salvar(capturado.capture());
        assertEquals(NivelAlerta.ALERTA, capturado.getValue().getNivel());
    }

    @Test
    void previsaoDentroDoNormalNaoGravaAlerta() throws Exception {
        // Arrange: média 10 está abaixo do limite de ATENÇÃO (250)
        when(medicaoDAO.buscarUltimas("RIO-01", 3)).thenReturn(tresLeituras(10));
        ServicoMonitoramento servico = new ServicoMonitoramento(medicaoDAO, alertaDAO, new DadosTerritorio());

        // Act
        ResultadoPrevisao resultado = servico.preverEAlertar(new SensorNivelRio("RIO-01"), new PrevisaoMediaMovel(3));

        // Assert
        assertTrue(resultado.alerta().isEmpty());
        verify(alertaDAO, never()).salvar(any());
    }
}
