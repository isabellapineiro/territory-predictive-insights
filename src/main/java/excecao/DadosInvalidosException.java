package excecao;

/** Lançada quando um dado do território é inválido (ex.: chuva negativa, limiar inválido). */
public class DadosInvalidosException extends Exception {
    public DadosInvalidosException(String mensagem) {
        super(mensagem);
    }
}
