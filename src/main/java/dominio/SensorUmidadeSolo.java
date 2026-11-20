package dominio;

import excecao.FonteIndisponivelException;
import java.util.Random;

/** Simula um sensor de umidade do solo. Valor em %, faixa de 0 a 100. */
public class SensorUmidadeSolo implements Sensor {
    private final String id;
    private final Random random = new Random();
    private boolean funcionando = true;

    public SensorUmidadeSolo(String id) {
        this.id = id;
    }

    @Override
    public String getId() { return id; }

    @Override
    public String getTipo() { return "UMIDADE_SOLO_PCT"; }

    @Override
    public boolean verificarFuncionamento() { return funcionando; }

    public void setFuncionando(boolean funcionando) { this.funcionando = funcionando; }

    @Override
    public double lerValor() throws FonteIndisponivelException {
        if (!verificarFuncionamento()) {
            throw new FonteIndisponivelException("Sensor " + id + " está fora de operação.");
        }
        return random.nextDouble() * 100;
    }
}
