package servico;

import dominio.EstrategiaPrevisao;
import dominio.PrevisaoComparacao;
import dominio.PrevisaoMediaMovel;
import excecao.DadosInvalidosException;
import java.util.Map;
import java.util.function.IntFunction;

/** Factory: cria a estratégia de previsão a partir do nome, sem if/switch espalhado pelo código. */
public final class FabricaPrevisao {

    private static final Map<String, IntFunction<EstrategiaPrevisao>> CRIADORES = Map.of(
            "MEDIA_MOVEL", PrevisaoMediaMovel::new,
            "COMPARACAO", PrevisaoComparacao::new);

    private FabricaPrevisao() { }

    public static EstrategiaPrevisao criar(String tipo, int janela) throws DadosInvalidosException {
        IntFunction<EstrategiaPrevisao> criador = (tipo == null) ? null : CRIADORES.get(tipo.toUpperCase());
        if (criador == null) {
            throw new DadosInvalidosException("Estratégia de previsão desconhecida: " + tipo);
        }
        if (janela < 1) {
            throw new DadosInvalidosException("A janela da previsão deve ser pelo menos 1.");
        }
        return criador.apply(janela);
    }
}
