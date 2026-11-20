package persistencia;

import dominio.Alerta;
import dominio.NivelAlerta;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class AlertaDAO {

    public void salvar(Alerta alerta) throws SQLException {
        String sql = "INSERT INTO alerta (id_sensor, nivel, valor_previsto, limite, estrategia, mensagem, data_hora) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = ConexaoBanco.obter();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, alerta.getIdSensor());
            ps.setString(2, alerta.getNivel().name());
            ps.setDouble(3, alerta.getValorPrevisto());
            ps.setDouble(4, alerta.getLimite());
            ps.setString(5, alerta.getEstrategia());
            ps.setString(6, alerta.getMensagem());
            ps.setTimestamp(7, Timestamp.valueOf(alerta.getDataHora()));
            ps.executeUpdate();
        }
    }

    public List<Alerta> listarTodos() throws SQLException {
        String sql = "SELECT id_sensor, nivel, valor_previsto, limite, estrategia, mensagem, data_hora "
                + "FROM alerta ORDER BY data_hora DESC, id DESC";
        List<Alerta> lista = new ArrayList<>();
        try (Connection c = ConexaoBanco.obter();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Alerta(
                        rs.getString("id_sensor"),
                        NivelAlerta.valueOf(rs.getString("nivel")),
                        rs.getDouble("valor_previsto"),
                        rs.getDouble("limite"),
                        rs.getString("estrategia"),
                        rs.getString("mensagem"),
                        rs.getTimestamp("data_hora").toLocalDateTime()));
            }
        }
        return lista;
    }
}
