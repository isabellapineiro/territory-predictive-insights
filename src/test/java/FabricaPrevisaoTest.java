import dominio.PrevisaoComparacao;
import dominio.PrevisaoMediaMovel;
import excecao.DadosInvalidosException;
import org.junit.jupiter.api.Test;
import servico.FabricaPrevisao;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FabricaPrevisaoTest {

    @Test
    void criaMediaMovelPeloNome() throws Exception {
        // Arrange + Act
        Object estrategia = FabricaPrevisao.criar("MEDIA_MOVEL", 3);

        // Assert
        assertInstanceOf(PrevisaoMediaMovel.class, estrategia);
    }

    @Test
    void criaComparacaoPeloNome() throws Exception {
        // Arrange + Act
        Object estrategia = FabricaPrevisao.criar("COMPARACAO", 3);

        // Assert
        assertInstanceOf(PrevisaoComparacao.class, estrategia);
    }

    @Test
    void nomeDesconhecidoLancaDadosInvalidosException() {
        // Arrange + Act + Assert
        assertThrows(DadosInvalidosException.class, () -> FabricaPrevisao.criar("XYZ", 3));
    }
}
