package excecao;

/** Lançada quando um sensor ou gerador de dados falha. */
public class FonteIndisponivelException extends Exception {
    public FonteIndisponivelException(String mensagem) {
        super(mensagem);
    }
}
