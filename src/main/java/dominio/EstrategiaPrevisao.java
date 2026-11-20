package dominio;

import excecao.DadosInvalidosException;
import java.util.List;

/** Base das estratégias de previsão. O histórico chega do mais recente para o mais antigo. */
public abstract class EstrategiaPrevisao {
    private final String nome;
    private final int minimoLeituras;

    protected EstrategiaPrevisao(String nome, int minimoLeituras) {
        this.nome = nome;
        this.minimoLeituras = minimoLeituras;
    }

    public String getNome() { return nome; }
    public int getMinimoLeituras() { return minimoLeituras; }

    /** O sistema se cala quando não há leituras suficientes para uma previsão confiável. */
    protected void validarHistorico(List<Medicao> historico) throws DadosInvalidosException {
        int total = (historico == null) ? 0 : historico.size();
        if (total < minimoLeituras) {
            throw new DadosInvalidosException(nome + " precisa de pelo menos " + minimoLeituras
                    + " leituras no histórico (atual: " + total + ").");
        }
    }

    protected double media(List<Medicao> historico, int inicio, int fim) {
        double soma = 0;
        for (int i = inicio; i < fim; i++) {
            soma += historico.get(i).getValor();
        }
        return soma / (fim - inicio);
    }

    public abstract double prever(List<Medicao> historico) throws DadosInvalidosException;
}
