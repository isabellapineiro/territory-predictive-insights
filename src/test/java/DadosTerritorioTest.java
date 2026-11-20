import dominio.DadosTerritorio;
import dominio.NivelAlerta;
import excecao.DadosInvalidosException;
import org.junit.jupiter.api.Test;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DadosTerritorioTest {

    @Test
    void limiaresForaDeOrdemLancamDadosInvalidosException() {
        // Arrange
        DadosTerritorio dados = new DadosTerritorio();

        // Act + Assert: atenção (50) maior que alerta (30) é inválido
        assertThrows(DadosInvalidosException.class,
                () -> dados.definirLimiares("CHUVA_MM_H", 50, 30, 90));
    }

    @Test
    void chuvaDe70MmPorHoraEClassificadaComoAlerta() throws Exception {
        // Arrange: limiares padrão da chuva são 30 / 60 / 90
        DadosTerritorio dados = new DadosTerritorio();

        // Act
        Optional<NivelAlerta> nivel = dados.classificar("CHUVA_MM_H", 70);

        // Assert
        assertEquals(Optional.of(NivelAlerta.ALERTA), nivel);
    }

    @Test
    void valorAbaixoDoLimiteDeAtencaoNaoGeraNivelDeAlerta() throws Exception {
        // Arrange
        DadosTerritorio dados = new DadosTerritorio();

        // Act
        Optional<NivelAlerta> nivel = dados.classificar("CHUVA_MM_H", 10);

        // Assert
        assertTrue(nivel.isEmpty());
    }

    @Test
    void populacaoNegativaLancaDadosInvalidosException() {
        // Arrange
        DadosTerritorio dados = new DadosTerritorio();

        // Act + Assert
        assertThrows(DadosInvalidosException.class, () -> dados.setPopulacao(-1));
    }
}