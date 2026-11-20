package dominio;

import excecao.DadosInvalidosException;
import java.util.List;

/** Previsão = média das últimas N medições. */
public class PrevisaoMediaMovel extends EstrategiaPrevisao {
    private final int janela;

    public PrevisaoMediaMovel(int janela) {
        super("Média móvel das últimas " + janela + " leituras", janela);
        if (janela < 1) {
            throw new IllegalArgumentException("A janela deve ser pelo menos 1.");
        }
        this.janela = janela;
    }

    @Override
    public double prever(List<Medicao> historico) throws DadosInvalidosException {
        validarHistorico(historico);
        return media(historico, 0, janela);
    }
}
