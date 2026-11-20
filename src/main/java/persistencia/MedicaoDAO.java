package persistencia;

import dominio.Medicao;
import excecao.DadosInvalidosException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class MedicaoDAO {

    /**
     * Grava uma medição no banco.
     * Usa PreparedStatement com "?" no lugar dos valores: o SQL e os dados vão separados,
     * então ninguém consegue injetar comandos SQL por um valor (e não há concatenação de String).
     * O try-with-resources fecha a conexão e o statement sozinho, mesmo se acontecer um erro.
     */
    public void salvar(Medicao medicao) throws SQLException {
        String sql = "INSERT INTO medicao (id_sensor, valor, data_hora) VALUES (?, ?, ?)";
        try (Connection c = ConexaoBanco.obter();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, medicao.getIdSensor());
            ps.setDouble(2, medicao.getValor());
            ps.setTimestamp(3, Timestamp.valueOf(medicao.getDataHora()));
            ps.executeUpdate();
        }
    }

    /**
     * Devolve o histórico completo de um sensor, da medição mais recente para a mais antiga
     * (ORDER BY data_hora DESC). É o que o menu usa para mostrar o histórico por sensor.
     */
    public List<Medicao> buscarHistoricoPorSensor(String idSensor) throws SQLException {
        String sql = "SELECT id_sensor, valor, data_hora FROM medicao "
                + "WHERE id_sensor = ? ORDER BY data_hora DESC, id DESC";
        return consultar(sql, idSensor, null);
    }

    /**
     * Devolve só as N medições mais recentes de um sensor (LIMIT ?).
     * Essas leituras são a base da previsão: a média móvel e a comparação
     * trabalham em cima do histórico guardado no banco.
     */
    public List<Medicao> buscarUltimas(String idSensor, int quantidade) throws SQLException {
        String sql = "SELECT id_sensor, valor, data_hora FROM medicao "
                + "WHERE id_sensor = ? ORDER BY data_hora DESC, id DESC LIMIT ?";
        return consultar(sql, idSensor, quantidade);
    }

    /**
     * Executa a consulta recebida e transforma cada linha do banco em um objeto Medicao.
     * Serve para os dois métodos de busca, evitando repetir o mesmo código.
     * Se algum registro estiver inválido, converte DadosInvalidosException em SQLException
     * com uma mensagem explicando que o registro está corrompido.
     */
    private List<Medicao> consultar(String sql, String idSensor, Integer limite) throws SQLException {
        List<Medicao> lista = new ArrayList<>();
        try (Connection c = ConexaoBanco.obter();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, idSensor);
            if (limite != null) {
                ps.setInt(2, limite);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Medicao(
                            rs.getString("id_sensor"),
                            rs.getDouble("valor"),
                            rs.getTimestamp("data_hora").toLocalDateTime()));
                }
            }
        } catch (DadosInvalidosException e) {
            throw new SQLException("Registro corrompido no banco: " + e.getMessage(), e);
        }
        return lista;
    }
}