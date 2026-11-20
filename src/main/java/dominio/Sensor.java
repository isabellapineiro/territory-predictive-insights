package dominio;

import excecao.FonteIndisponivelException;

public interface Sensor {
    String getId();
    String getTipo();
    boolean verificarFuncionamento();
    double lerValor() throws FonteIndisponivelException;
}
