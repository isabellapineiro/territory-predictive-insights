package dominio;

import excecao.DadosInvalidosException;
import java.util.List;

/** Compara as últimas N leituras com as N anteriores e projeta a tendência para o próximo período. */
public class PrevisaoComparacao extends EstrategiaPrevisao {
    private final int janela;

    public PrevisaoComparacao(int janela) {
        super("Comparação com o período anterior (" + janela + " x " + janela + " leituras)", janela * 2);
        if (janela < 1) {
            throw new IllegalArgumentException("A janela deve ser pelo menos 1.");
        }
        this.janela = janela;
    }

    @Override
    public double prever(List<Medicao> historico) throws DadosInvalidosException {
        validarHistorico(historico);
        double recente = media(historico, 0, janela);
        double anterior = media(historico, janela, janela * 2);
        double previsto = recente + (recente - anterior); // mantém a tendência
        return Math.max(0, previsto);
    }
}
