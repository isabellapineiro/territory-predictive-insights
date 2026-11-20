package dominio;

import excecao.FonteIndisponivelException;
import java.util.Random;

/** Simula um sensor de nível de rio. Valor em cm, faixa de 0 a 500. */
public class SensorNivelRio implements Sensor {
    private final String id;
    private final Random random = new Random();
    private boolean funcionando = true;

    public SensorNivelRio(String id) {
        this.id = id;
    }

    @Override
    public String getId() { return id; }

    @Override
    public String getTipo() { return "NIVEL_RIO_CM"; }

    @Override
    public boolean verificarFuncionamento() { return funcionando; }

    public void setFuncionando(boolean funcionando) { this.funcionando = funcionando; }

    @Override
    public double lerValor() throws FonteIndisponivelException {
        if (!verificarFuncionamento()) {
            throw new FonteIndisponivelException("Sensor " + id + " está fora de operação.");
        }
        return random.nextDouble() * 500;
    }
}
