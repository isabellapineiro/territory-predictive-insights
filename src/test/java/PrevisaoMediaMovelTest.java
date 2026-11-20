import dominio.Medicao;
import dominio.PrevisaoMediaMovel;
import excecao.DadosInvalidosException;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PrevisaoMediaMovelTest {

    private Medicao medicao(double valor) throws DadosInvalidosException {
        return new Medicao("PLUV-01", valor, LocalDateTime.now());
    }

    @Test
    void calculaMediaDasUltimasLeituras() throws Exception {
        // Arrange
        PrevisaoMediaMovel estrategia = new PrevisaoMediaMovel(3);
        List<Medicao> historico = List.of(medicao(10), medicao(20), medicao(30));

        // Act
        double previsto = estrategia.prever(historico);

        // Assert
        assertEquals(20.0, previsto, 0.0001);
    }

    @Test
    void usaApenasAsLeiturasDaJanela() throws Exception {
        // Arrange: a lista vem do mais recente para o mais antigo; só as 2 primeiras contam
        PrevisaoMediaMovel estrategia = new PrevisaoMediaMovel(2);
        List<Medicao> historico = List.of(medicao(10), medicao(20), medicao(1000));

        // Act
        double previsto = estrategia.prever(historico);

        // Assert
        assertEquals(15.0, previsto, 0.0001);
    }

    @Test
    void historicoInsuficienteLancaDadosInvalidosException() throws Exception {
        // Arrange
        PrevisaoMediaMovel estrategia = new PrevisaoMediaMovel(3);
        List<Medicao> historico = List.of(medicao(10));

        // Act + Assert
        assertThrows(DadosInvalidosException.class, () -> estrategia.prever(historico));
    }
}
