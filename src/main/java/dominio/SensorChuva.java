package dominio;

import excecao.FonteIndisponivelException;
import java.util.Random;

/** Simula um pluviômetro. Valor em mm/h, faixa realista de 0 a 120. */
public class SensorChuva implements Sensor {
    private final String id;
    private final Random random = new Random();
    private boolean funcionando = true;

    public SensorChuva(String id) {
        this.id = id;
    }

    @Override
    public String getId() { return id; }

    @Override
    public String getTipo() { return "CHUVA_MM_H"; }

    @Override
    public boolean verificarFuncionamento() { return funcionando; }

    public void setFuncionando(boolean funcionando) { this.funcionando = funcionando; }

    @Override
    public double lerValor() throws FonteIndisponivelException {
        if (!verificarFuncionamento()) {
            throw new FonteIndisponivelException("Sensor " + id + " está fora de operação.");
        }
        return random.nextDouble() * 120;
    }
}
