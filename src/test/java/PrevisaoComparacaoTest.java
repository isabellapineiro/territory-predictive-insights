import dominio.Medicao;
import dominio.PrevisaoComparacao;
import excecao.DadosInvalidosException;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PrevisaoComparacaoTest {

    private Medicao medicao(double valor) throws DadosInvalidosException {
        return new Medicao("RIO-01", valor, LocalDateTime.now());
    }

    @Test
    void tendenciaDeAltaProjetaValorMaiorQueORecente() throws Exception {
        // Arrange: recentes (30) contra anteriores (10) -> sobe 20 por período
        PrevisaoComparacao estrategia = new PrevisaoComparacao(3);
        List<Medicao> historico = List.of(
                medicao(30), medicao(30), medicao(30),
                medicao(10), medicao(10), medicao(10));

        // Act
        double previsto = estrategia.prever(historico);

        // Assert: 30 + (30 - 10) = 50
        assertEquals(50.0, previsto, 0.0001);
    }

    @Test
    void previsaoNuncaFicaNegativa() throws Exception {
        // Arrange: queda forte (10 contra 100) projetaria valor negativo
        PrevisaoComparacao estrategia = new PrevisaoComparacao(1);
        List<Medicao> historico = List.of(medicao(10), medicao(100));

        // Act
        double previsto = estrategia.prever(historico);

        // Assert
        assertEquals(0.0, previsto, 0.0001);
    }
}
