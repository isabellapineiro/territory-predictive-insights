import dominio.Alerta;
import dominio.DadosTerritorio;
import dominio.EstrategiaPrevisao;
import dominio.Medicao;
import dominio.PontoAlagamento;
import dominio.Sensor;
import dominio.SensorChuva;
import dominio.SensorNivelRio;
import dominio.SensorUmidadeSolo;
import excecao.DadosInvalidosException;
import excecao.FonteIndisponivelException;
import persistencia.AlertaDAO;
import persistencia.ConexaoBanco;
import persistencia.MedicaoDAO;
import servico.FabricaPrevisao;
import servico.ResultadoPrevisao;
import servico.ServicoMonitoramento;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/** Menu principal do sistema. */
public class SistemaMonitoramento {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public static void main(String[] args) {
        try {
            ConexaoBanco.inicializar();
        } catch (SQLException e) {
            System.out.println("Não foi possível iniciar o banco de dados: " + e.getMessage());
            return;
        }

        List<Sensor> sensores = List.of(
                new SensorChuva("PLUV-01"),
                new SensorNivelRio("RIO-01"),
                new SensorUmidadeSolo("SOLO-01"));
        PontoAlagamento ponto = new PontoAlagamento("PA-01", "Córrego Central", "Zona Leste", sensores);

        // As estratégias são criadas pela fábrica; o menu trata todas como EstrategiaPrevisao
        List<EstrategiaPrevisao> estrategias = new ArrayList<>();
        try {
            for (String tipo : List.of("MEDIA_MOVEL", "COMPARACAO")) {
                estrategias.add(FabricaPrevisao.criar(tipo, 3));
            }
        } catch (DadosInvalidosException e) {
            System.out.println("Erro de configuração: " + e.getMessage());
            return;
        }

        DadosTerritorio dados = new DadosTerritorio();
        MedicaoDAO medicaoDAO = new MedicaoDAO();
        AlertaDAO alertaDAO = new AlertaDAO();
        ServicoMonitoramento servico = new ServicoMonitoramento(medicaoDAO, alertaDAO, dados);

        try (Scanner scanner = new Scanner(System.in)) {
            int opcao;
            do {
                System.out.println("\n=== Monitoramento Territorial ===");
                System.out.println("1 - Verificar sensores (ler e gravar)");
                System.out.println("2 - Rodar previsões e alertas");
                System.out.println("3 - Ver histórico de medições por sensor");
                System.out.println("4 - Ver alertas emitidos");
                System.out.println("5 - Gerenciar dados do território (limiares)");
                System.out.println("6 - Status completo");
                System.out.println("7 - Simular 10 rodadas de leitura");
                System.out.println("0 - Sair");
                System.out.print("Opção: ");
                opcao = lerInteiro(scanner);

                switch (opcao) {
                    case 1 -> verificarSensores(sensores, servico);
                    case 2 -> rodarPrevisoes(sensores, estrategias, servico);
                    case 3 -> verHistorico(scanner, medicaoDAO);
                    case 4 -> verAlertas(alertaDAO);
                    case 5 -> gerenciarDados(scanner, dados);
                    case 6 -> statusCompleto(ponto, dados);
                    case 7 -> simular(sensores, servico);
                    case 0 -> System.out.println("Encerrando.");
                    default -> System.out.println("Opção inválida.");
                }
            } while (opcao != 0);
        }
    }

    private static void verificarSensores(List<Sensor> sensores, ServicoMonitoramento servico) {
        for (Sensor sensor : sensores) {
            try {
                Medicao m = servico.lerEGravar(sensor);
                System.out.printf("%s (%s): %.1f gravado.%n", sensor.getId(), sensor.getTipo(), m.getValor());
            } catch (FonteIndisponivelException e) {
                System.out.println("Falha no sensor: " + e.getMessage());
            } catch (DadosInvalidosException e) {
                System.out.println("Leitura descartada: " + e.getMessage());
            } catch (SQLException e) {
                System.out.println("Erro ao gravar no banco: " + e.getMessage());
            }
        }
    }

    private static void simular(List<Sensor> sensores, ServicoMonitoramento servico) {
        for (int i = 0; i < 10; i++) {
            for (Sensor sensor : sensores) {
                try {
                    servico.lerEGravar(sensor);
                } catch (FonteIndisponivelException | DadosInvalidosException | SQLException e) {
                    System.out.println("Rodada " + (i + 1) + " - " + sensor.getId() + ": " + e.getMessage());
                }
            }
        }
        System.out.println("10 rodadas gravadas para cada sensor.");
    }

    private static void rodarPrevisoes(List<Sensor> sensores, List<EstrategiaPrevisao> estrategias,
                                       ServicoMonitoramento servico) {
        for (Sensor sensor : sensores) {
            System.out.println("\n-- " + sensor.getId() + " (" + sensor.getTipo() + ") --");
            for (EstrategiaPrevisao estrategia : estrategias) {
                try {
                    ResultadoPrevisao r = servico.preverEAlertar(sensor, estrategia);
                    System.out.printf("%s: previsto %.1f%n", estrategia.getNome(), r.valorPrevisto());
                    if (r.alerta().isPresent()) {
                        Alerta a = r.alerta().get();
                        System.out.println("  [" + a.getNivel().getRotulo() + "] " + a.getMensagem());
                    } else {
                        System.out.println("  Dentro do normal: abaixo do limite de ATENÇÃO.");
                    }
                } catch (DadosInvalidosException e) {
                    System.out.println(estrategia.getNome() + ": sem previsão. " + e.getMessage());
                } catch (SQLException e) {
                    System.out.println("Erro ao acessar o banco: " + e.getMessage());
                }
            }
        }
    }

    private static void verHistorico(Scanner scanner, MedicaoDAO dao) {
        System.out.print("ID do sensor (ex.: PLUV-01): ");
        String id = scanner.nextLine().trim();
        try {
            List<Medicao> historico = dao.buscarHistoricoPorSensor(id);
            if (historico.isEmpty()) {
                System.out.println("Nenhuma medição encontrada para " + id + ".");
                return;
            }
            for (Medicao m : historico) {
                System.out.printf("%s  %.1f%n", m.getDataHora().format(FORMATO), m.getValor());
            }
        } catch (SQLException e) {
            System.out.println("Erro ao consultar o histórico: " + e.getMessage());
        }
    }

    private static void verAlertas(AlertaDAO dao) {
        try {
            List<Alerta> alertas = dao.listarTodos();
            if (alertas.isEmpty()) {
                System.out.println("Nenhum alerta emitido até agora.");
                return;
            }
            for (Alerta a : alertas) {
                System.out.println(a.getDataHora().format(FORMATO) + " [" + a.getNivel().getRotulo() + "] "
                        + a.getMensagem());
            }
        } catch (SQLException e) {
            System.out.println("Erro ao consultar alertas: " + e.getMessage());
        }
    }

    private static void gerenciarDados(Scanner scanner, DadosTerritorio dados) {
        System.out.println("Limiares atuais:");
        for (Map.Entry<String, DadosTerritorio.Limiar> e : dados.getLimiares().entrySet()) {
            DadosTerritorio.Limiar l = e.getValue();
            System.out.printf("  %s -> atenção %.1f | alerta %.1f | crítico %.1f%n",
                    e.getKey(), l.atencao(), l.alerta(), l.critico());
        }
        System.out.print("Tipo para alterar (ex.: CHUVA_MM_H) ou Enter para voltar: ");
        String tipo = scanner.nextLine().trim();
        if (tipo.isEmpty()) {
            return;
        }
        try {
            System.out.print("Novo limite de ATENÇÃO: ");
            double atencao = lerDouble(scanner);
            System.out.print("Novo limite de ALERTA: ");
            double alerta = lerDouble(scanner);
            System.out.print("Novo limite CRÍTICO: ");
            double critico = lerDouble(scanner);
            dados.definirLimiares(tipo, atencao, alerta, critico);
            System.out.println("Limiares atualizados.");
        } catch (NumberFormatException e) {
            System.out.println("Valor numérico inválido. Nada foi alterado.");
        } catch (DadosInvalidosException e) {
            System.out.println("Não foi possível alterar: " + e.getMessage());
        }
    }

    private static void statusCompleto(PontoAlagamento ponto, DadosTerritorio dados) {
        System.out.println("Ponto: " + ponto.getNome() + " (" + ponto.getRegiao() + ") - " + ponto.verificarStatus());
        for (Sensor s : ponto.getSensores()) {
            System.out.println("  " + s.getId() + " [" + s.getTipo() + "]: "
                    + (s.verificarFuncionamento() ? "funcionando" : "FORA DE OPERAÇÃO"));
        }
        System.out.println("População do território: " + dados.getPopulacao());
    }

    private static int lerInteiro(Scanner scanner) {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static double lerDouble(Scanner scanner) {
        return Double.parseDouble(scanner.nextLine().trim().replace(',', '.'));
    }
}
