package dominio;

import java.time.LocalDateTime;

public class Alerta {
    private final String idSensor;
    private final NivelAlerta nivel;
    private final double valorPrevisto;
    private final double limite;
    private final String estrategia;
    private final String mensagem;
    private final LocalDateTime dataHora;

    public Alerta(String idSensor, NivelAlerta nivel, double valorPrevisto, double limite,
                  String estrategia, String mensagem, LocalDateTime dataHora) {
        this.idSensor = idSensor;
        this.nivel = nivel;
        this.valorPrevisto = valorPrevisto;
        this.limite = limite;
        this.estrategia = estrategia;
        this.mensagem = mensagem;
        this.dataHora = dataHora;
    }

    public String getIdSensor() { return idSensor; }
    public NivelAlerta getNivel() { return nivel; }
    public double getValorPrevisto() { return valorPrevisto; }
    public double getLimite() { return limite; }
    public String getEstrategia() { return estrategia; }
    public String getMensagem() { return mensagem; }
    public LocalDateTime getDataHora() { return dataHora; }
}
