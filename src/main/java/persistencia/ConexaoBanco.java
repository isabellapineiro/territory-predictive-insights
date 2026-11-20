package persistencia;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/** H2 em arquivo (./data/territorio): os dados sobrevivem ao reiniciar o programa. */
public class ConexaoBanco {
    private static final String URL = "jdbc:h2:./data/territorio";
    private static final String USUARIO = "sa";
    private static final String SENHA = "";

    private ConexaoBanco() { }

    public static Connection obter() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, SENHA);
    }

    public static void inicializar() throws SQLException {
        try (Connection c = obter(); Statement st = c.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS medicao ("
                    + "id BIGINT AUTO_INCREMENT PRIMARY KEY, "
                    + "id_sensor VARCHAR(50) NOT NULL, "
                    + "valor DOUBLE NOT NULL, "
                    + "data_hora TIMESTAMP NOT NULL)");
            st.execute("CREATE TABLE IF NOT EXISTS alerta ("
                    + "id BIGINT AUTO_INCREMENT PRIMARY KEY, "
                    + "id_sensor VARCHAR(50) NOT NULL, "
                    + "nivel VARCHAR(20) NOT NULL, "
                    + "valor_previsto DOUBLE NOT NULL, "
                    + "limite DOUBLE NOT NULL, "
                    + "estrategia VARCHAR(150) NOT NULL, "
                    + "mensagem VARCHAR(500) NOT NULL, "
                    + "data_hora TIMESTAMP NOT NULL)");
        }
    }
}
