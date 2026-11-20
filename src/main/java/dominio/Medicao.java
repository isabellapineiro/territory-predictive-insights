package dominio;

import excecao.DadosInvalidosException;
import java.time.LocalDateTime;

public class Medicao {
    private final String idSensor;
    private final double valor;
    private final LocalDateTime dataHora;

    public Medicao(String idSensor, double valor, LocalDateTime dataHora) throws DadosInvalidosException {
        if (idSensor == null || idSensor.isBlank()) {
            throw new DadosInvalidosException("Medição sem identificação de sensor.");
        }
        if (Double.isNaN(valor) || Double.isInfinite(valor) || valor < 0) {
            throw new DadosInvalidosException("Medição inválida do sensor " + idSensor + ": valor " + valor + " fora da faixa.");
        }
        this.idSensor = idSensor;
        this.valor = valor;
        this.dataHora = dataHora;
    }

    public String getIdSensor() { return idSensor; }
    public double getValor() { return valor; }
    public LocalDateTime getDataHora() { return dataHora; }
}
