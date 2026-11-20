package dominio;

import excecao.DadosInvalidosException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/** Dados do território: população e limiares de alerta. Atributos privados, alteração só por métodos validados. */
public class DadosTerritorio {

    public record Limiar(double atencao, double alerta, double critico) { }

    private final Map<String, Limiar> limiares = new LinkedHashMap<>();
    private int populacao = 0;

    public DadosTerritorio() {
        limiares.put("CHUVA_MM_H", new Limiar(30, 60, 90));
        limiares.put("NIVEL_RIO_CM", new Limiar(250, 350, 450));
        limiares.put("UMIDADE_SOLO_PCT", new Limiar(60, 80, 95));
    }

    public int getPopulacao() { return populacao; }

    public void setPopulacao(int populacao) throws DadosInvalidosException {
        if (populacao < 0) {
            throw new DadosInvalidosException("População não pode ser negativa: " + populacao);
        }
        this.populacao = populacao;
    }

    public void definirLimiares(String tipo, double atencao, double alerta, double critico)
            throws DadosInvalidosException {
        if (tipo == null || !limiares.containsKey(tipo)) {
            throw new DadosInvalidosException("Tipo de sensor desconhecido: " + tipo);
        }
        if (!(atencao > 0 && atencao < alerta && alerta < critico)) {
            throw new DadosInvalidosException(
                    "Limiares inválidos: use valores positivos em ordem crescente (atenção < alerta < crítico).");
        }
        limiares.put(tipo, new Limiar(atencao, alerta, critico));
    }

    public Limiar getLimiar(String tipo) throws DadosInvalidosException {
        Limiar limiar = limiares.get(tipo);
        if (limiar == null) {
            throw new DadosInvalidosException("Tipo de sensor sem limiares configurados: " + tipo);
        }
        return limiar;
    }

    public Map<String, Limiar> getLimiares() {
        return Collections.unmodifiableMap(limiares);
    }

    /** Compara um valor com os limiares. Vazio = dentro do normal. */
    public Optional<NivelAlerta> classificar(String tipo, double valor) throws DadosInvalidosException {
        Limiar l = getLimiar(tipo);
        if (valor >= l.critico()) return Optional.of(NivelAlerta.CRITICO);
        if (valor >= l.alerta()) return Optional.of(NivelAlerta.ALERTA);
        if (valor >= l.atencao()) return Optional.of(NivelAlerta.ATENCAO);
        return Optional.empty();
    }

    public double getLimite(String tipo, NivelAlerta nivel) throws DadosInvalidosException {
        Limiar l = getLimiar(tipo);
        return switch (nivel) {
            case ATENCAO -> l.atencao();
            case ALERTA -> l.alerta();
            case CRITICO -> l.critico();
        };
    }
}
