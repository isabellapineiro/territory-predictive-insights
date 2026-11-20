import dominio.Medicao;
import excecao.DadosInvalidosException;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MedicaoTest {

    @Test
    void valorNegativoLancaDadosInvalidosException() {
        // Arrange
        LocalDateTime agora = LocalDateTime.now();

        // Act + Assert
        DadosInvalidosException erro = assertThrows(DadosInvalidosException.class,
                () -> new Medicao("PLUV-01", -5, agora));
        assertEquals(true, erro.getMessage().contains("PLUV-01"));
    }

    @Test
    void valorValidoCriaMedicaoComOsDadosInformados() throws Exception {
        // Arrange
        LocalDateTime agora = LocalDateTime.now();

        // Act
        Medicao medicao = new Medicao("PLUV-01", 42.5, agora);

        // Assert
        assertEquals("PLUV-01", medicao.getIdSensor());
        assertEquals(42.5, medicao.getValor(), 0.0001);
    }
}
