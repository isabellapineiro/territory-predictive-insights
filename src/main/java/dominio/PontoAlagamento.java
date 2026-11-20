package dominio;

import java.util.List;

/** Ponto vulnerável a alagamento, monitorado por vários sensores. */
public class PontoAlagamento extends ComponenteTerritorial {
    private final List<Sensor> sensores;

    public PontoAlagamento(String id, String nome, String regiao, List<Sensor> sensores) {
        super(id, nome, regiao);
        this.sensores = sensores;
    }

    public List<Sensor> getSensores() { return sensores; }

    @Override
    public String verificarStatus() {
        for (Sensor sensor : sensores) {
            if (!sensor.verificarFuncionamento()) {
                setStatus("FALHA");
                return getStatus();
            }
        }
        setStatus("OPERANTE");
        return getStatus();
    }
}
