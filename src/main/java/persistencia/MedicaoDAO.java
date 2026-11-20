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

    /** Histórico completo de um sensor, do mais recente para o mais antigo. */
    public List<Medicao> buscarHistoricoPorSensor(String idSensor) throws SQLException {
        String sql = "SELECT id_sensor, valor, data_hora FROM medicao "
                + "WHERE id_sensor = ? ORDER BY data_hora DESC, id DESC";
        return consultar(sql, idSensor, null);
    }

    /** Últimas N medições de um sensor (base da média móvel). */
    public List<Medicao> buscarUltimas(String idSensor, int quantidade) throws SQLException {
        String sql = "SELECT id_sensor, valor, data_hora FROM medicao "
                + "WHERE id_sensor = ? ORDER BY data_hora DESC, id DESC LIMIT ?";
        return consultar(sql, idSensor, quantidade);
    }

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
