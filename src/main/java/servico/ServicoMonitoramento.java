package servico;

import dominio.Alerta;
import dominio.DadosTerritorio;
import dominio.EstrategiaPrevisao;
import dominio.Medicao;
import dominio.NivelAlerta;
import dominio.Sensor;
import excecao.DadosInvalidosException;
import excecao.FonteIndisponivelException;
import persistencia.AlertaDAO;
import persistencia.MedicaoDAO;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** Lógica de negócio: lê, valida, grava, prevê e emite alertas. Recebe os DAOs pelo construtor (facilita testar com mock). */
public class ServicoMonitoramento {
    private final MedicaoDAO medicaoDAO;
    private final AlertaDAO alertaDAO;
    private final DadosTerritorio dados;

    public ServicoMonitoramento(MedicaoDAO medicaoDAO, AlertaDAO alertaDAO, DadosTerritorio dados) {
        this.medicaoDAO = medicaoDAO;
        this.alertaDAO = alertaDAO;
        this.dados = dados;
    }

    public Medicao lerEGravar(Sensor sensor)
            throws FonteIndisponivelException, DadosInvalidosException, SQLException {
        double valor = sensor.lerValor();
        Medicao medicao = new Medicao(sensor.getId(), valor, LocalDateTime.now());
        medicaoDAO.salvar(medicao);
        return medicao;
    }

    public ResultadoPrevisao preverEAlertar(Sensor sensor, EstrategiaPrevisao estrategia)
            throws DadosInvalidosException, SQLException {
        List<Medicao> historico = medicaoDAO.buscarUltimas(sensor.getId(), estrategia.getMinimoLeituras());
        double previsto = estrategia.prever(historico);

        Optional<NivelAlerta> nivel = dados.classificar(sensor.getTipo(), previsto);
        Optional<Alerta> alerta = Optional.empty();

        if (nivel.isPresent()) {
            double limite = dados.getLimite(sensor.getTipo(), nivel.get());
            String mensagem = String.format(
                    "Sensor %s (%s): valor previsto %.1f atingiu o limite de %s (%.1f). Método: %s.",
                    sensor.getId(), sensor.getTipo(), previsto, nivel.get().getRotulo(), limite,
                    estrategia.getNome());
            Alerta novo = new Alerta(sensor.getId(), nivel.get(), previsto, limite,
                    estrategia.getNome(), mensagem, LocalDateTime.now());
            alertaDAO.salvar(novo);
            alerta = Optional.of(novo);
        }
        return new ResultadoPrevisao(sensor.getId(), sensor.getTipo(), estrategia.getNome(), previsto, alerta);
    }
}
