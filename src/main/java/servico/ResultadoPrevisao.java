package servico;

import dominio.Alerta;
import java.util.Optional;

public record ResultadoPrevisao(String idSensor, String tipoSensor, String estrategia,
                                double valorPrevisto, Optional<Alerta> alerta) { }
