# Territory Predictive Insights — Global Solution 2º/2026 (POO)

**Tema:** CitizenTech
**Integrantes:**
- Gabriel Vergueiro — RM 564439
- Isabella Piñeiro Santana — RM 562779
- Pam Mitchell Barbosa Durham — RM 562635
**Recorte:** Clima local — chuva intensa e risco de alagamento no ponto "Córrego Central" (Zona Leste).

---

## 1. Público beneficiado e decisão apoiada

**Quem sofre o problema:** moradores e comerciantes de áreas próximas a córregos e pontos baixos, que sofrem com alagamentos quando a chuva forte coincide com rio cheio e solo saturado.

**Decisão que o sistema apoia:** a associação de bairro ou a defesa civil local decide, com algumas horas de antecedência, se avisa a comunidade, libera a limpeza de bueiros, posiciona equipes ou prepara um ponto de abrigo. O sistema **não decide** por ninguém: ele mostra o valor previsto, qual limite foi atingido e por quê.

O sistema não usa dados pessoais. O território é descrito apenas por região e ponto monitorado.

---

## 2. Como rodar

Requisitos: **JDK 17+** e **Maven**.

```bash
mvn compile exec:java   # abre o menu
mvn test                # roda os 12 testes
```

No IntelliJ: abrir a pasta como projeto Maven, executar `SistemaMonitoramento` e, para os testes, botão direito em `src/test/java` → *Run All Tests*.

**Banco de dados:** H2 em arquivo (`./data/territorio`), criado automaticamente na primeira execução, sem instalação nem configuração. Os dados sobrevivem ao reiniciar o programa. Para começar do zero, apague a pasta `data/`.

### Menu

| Opção | O que faz |
|---|---|
| 1 | Lê os sensores e grava cada medição válida no banco |
| 2 | Roda as duas previsões sobre o histórico do banco e emite alertas |
| 3 | Lista o histórico de medições de um sensor |
| 4 | Lista os alertas já emitidos |
| 5 | Mostra e altera os limiares de alerta |
| 6 | Status completo do ponto e dos sensores |
| 7 | Simula 10 rodadas de leitura (para ter histórico rapidamente) |

**Roteiro rápido de demonstração:** `7` → `2` → `4` → `5` (alterar um limiar) → `2` de novo.

---

## 3. Estrutura do projeto

```
src/main/java/
├── dominio/       Sensor (interface), SensorChuva, SensorNivelRio, SensorUmidadeSolo,
│                  ComponenteTerritorial (abstrata), PontoAlagamento,
│                  EstrategiaPrevisao (abstrata), PrevisaoMediaMovel, PrevisaoComparacao,
│                  DadosTerritorio, Medicao, Alerta, NivelAlerta
├── excecao/       DadosInvalidosException, FonteIndisponivelException
├── persistencia/  ConexaoBanco, MedicaoDAO, AlertaDAO   (JDBC)
├── servico/       ServicoMonitoramento, FabricaPrevisao, ResultadoPrevisao
└── SistemaMonitoramento.java   (menu principal)
src/test/java/     MedicaoTest, PrevisaoMediaMovelTest, PrevisaoComparacaoTest,
                   ServicoMonitoramentoTest, FabricaPrevisaoTest
```

### Como cada conceito do enunciado foi aplicado

- **Abstração e herança:** `ComponenteTerritorial` é abstrata (id, nome, região, status) com o método abstrato `verificarStatus()`. `PontoAlagamento` é a filha concreta: o status é "FALHA" se algum sensor estiver parado, senão "OPERANTE".
- **Interface e polimorfismo:** `Sensor` tem 3 implementações (chuva em mm/h, nível do rio em cm, umidade do solo em %). O menu e o serviço tratam todas como `Sensor`.
- **Estratégias de previsão:** `EstrategiaPrevisao` define `prever()`. `PrevisaoMediaMovel` calcula a média das últimas N leituras. `PrevisaoComparacao` compara as últimas N com as N anteriores e projeta a tendência. Ambas chamam `super(...)` no construtor e sobrescrevem `prever()`. A escolha da estratégia é feita sem `if tipo ==` nem `instanceof`: o menu percorre uma `List<EstrategiaPrevisao>`.
- **Encapsulamento:** `DadosTerritorio` mantém limiares e população em atributos privados. Só se altera por `definirLimiares()` e `setPopulacao()`, que validam e lançam exceção.
- **Exceções customizadas:** `DadosInvalidosException` (medição negativa, limiar fora de ordem, população negativa, histórico insuficiente) e `FonteIndisponivelException` (sensor fora de operação). São lançadas em `Medicao`, `DadosTerritorio`, nos sensores e nas estratégias, e capturadas no menu com mensagem clara. Não há `catch` vazio.
- **Persistência (JDBC):** `MedicaoDAO` e `AlertaDAO` usam `PreparedStatement` (nunca concatenação de String no SQL) e `try-with-resources`. Cada medição válida e cada alerta emitido são gravados. O menu lista o histórico por sensor e os alertas.

### Limiares padrão

| Sensor | ATENÇÃO | ALERTA | CRÍTICO |
|---|---|---|---|
| Chuva (mm/h) | 30 | 60 | 90 |
| Nível do rio (cm) | 250 | 350 | 450 |
| Umidade do solo (%) | 60 | 80 | 95 |

Os valores podem ser alterados na opção 5 do menu. O sistema exige ordem crescente (atenção < alerta < crítico).

---

## 4. Padrão criacional: Factory (`FabricaPrevisao`)

`FabricaPrevisao.criar("MEDIA_MOVEL", 3)` devolve a estratégia pedida a partir de um nome, usando um mapa de criadores.

**Por que aqui e não em outro lugar:** a criação das estratégias é o ponto que mais tende a mudar (novas formas de prever). Com a fábrica, o menu e o serviço só conhecem o tipo abstrato `EstrategiaPrevisao`; para incluir uma nova estratégia basta registrá-la na fábrica, sem espalhar `if`/`switch` pelo código. A fábrica também concentra a validação (nome desconhecido ou janela menor que 1 lançam `DadosInvalidosException`). Não faria sentido usar Singleton nos DAOs ou Builder nas medições, porque esses objetos são simples e não têm configuração a montar.

---

## 5. Testes

12 testes JUnit 5, no padrão AAA (Arrange, Act, Assert):

- `MedicaoTest`: medição negativa lança exceção (`assertThrows`); medição válida guarda os dados.
- `PrevisaoMediaMovelTest`: calcula a média; usa só a janela; histórico insuficiente lança exceção (`assertThrows`).
- `PrevisaoComparacaoTest`: tendência de alta projeta valor maior; a previsão nunca fica negativa.
- `FabricaPrevisaoTest`: cria cada estratégia pelo nome; nome desconhecido lança exceção (`assertThrows`).
- `ServicoMonitoramentoTest` (**Mockito**): os DAOs são mocks, então a lógica de alerta é testada sem banco: previsão acima do limite grava alerta de nível ALERTA; previsão normal não grava alerta.

---

## 6. Decisões técnicas

- **JDBC em vez de Spring/JPA:** menos configuração, SQL visível e mais fácil de explicar e depurar.
- **H2 em arquivo:** roda sem instalar nada e mantém os dados ao reiniciar o programa.
- **Janela de 3 leituras:** permite demonstrar o sistema rapidamente. A média móvel precisa de 3 leituras e a comparação, de 6.
- **Sensores simulados com `Random`:** permitido pelo enunciado, em faixas realistas para cada tipo de sensor.
- **Limiares configuráveis:** ficam em `DadosTerritorio` e podem ser alterados pelo menu, com validação.
- **Sem API REST nem interface gráfica:** fora do escopo da GS; usamos menu de console.

**Limitações conhecidas:** cada vez que a opção 2 roda, ela grava novos alertas, mesmo que sejam parecidos com os anteriores. Não há tratamento de duplicidade.

---

## 7. Reflexão crítica

**Quando o sistema deve se calar?** Uma previsão só é útil se tiver base. Hoje o sistema exige 3 leituras para a média móvel e 6 para a comparação com o período anterior; com menos que isso ele não prevê e informa quantas leituras faltam. Mesmo assim, 3 leituras são pouco para confiar em um fenômeno como a chuva, que muda rápido. Num sistema real, o número mínimo deveria depender do intervalo entre as leituras: três leituras com um minuto de diferença dizem pouco, e três leituras espaçadas por horas podem já estar desatualizadas. Também faria sentido o sistema se calar quando um sensor está fora de operação ou quando as leituras são muito antigas, em vez de prever com dados velhos. Um alerta falso tem custo: se a comunidade recebe avisos demais sem motivo, passa a ignorá-los. Por isso preferimos que o sistema diga "não tenho dados suficientes" a emitir um alerta sem base.

**O que os dados não representam?** Os valores são simulados com `Random`, então não refletem a chuva real, e há apenas um sensor de cada tipo para o ponto inteiro. Um pluviômetro não representa todas as ruas do bairro, o relevo, a drenagem local, o lixo nos bueiros nem a chuva que caiu mais acima, na nascente do córrego. As duas previsões também são simples: a média móvel ignora a tendência e a comparação assume que a tendência recente continua, o que nem sempre acontece. Os limiares (por exemplo, 60 mm/h de chuva como ALERTA) foram escolhidos por nós e precisariam ser calibrados com dados históricos e com a defesa civil. O sistema também não enxerga quem é mais vulnerável: não sabe onde há idosos, crianças ou pessoas com dificuldade de locomoção, e de propósito não usa dados pessoais.

**Por que o alerta apoia a decisão da pessoa, em vez de decidir por ela?** O alerta mostra qual sensor, qual valor foi previsto e qual limite foi atingido, e é a pessoa quem decide o que fazer: avisar a comunidade, limpar bueiros, acionar a defesa civil ou apenas observar mais um pouco. Quem conhece o território sabe coisas que o sistema não sabe, como uma obra que bloqueou uma rua ou um evento marcado para aquele dia. Evacuar um bairro ou deixar de avisar tem consequências sérias, e a responsabilidade por elas precisa ficar com uma pessoa que possa explicar a decisão. Por isso a mensagem do alerta explica o porquê, para que quem decide consiga confiar no sistema ou duvidar dele com conhecimento.

---

## 8. Uso de IA

Usamos o **Claude (Anthropic)** como assistente no desenvolvimento. Ele gerou a base do código em etapas (domínio, exceções, persistência JDBC, estratégias de previsão, alertas, fábrica, testes e a estrutura inicial deste README). O código foi executado no IntelliJ e os 12 testes foram rodados e passaram.

Depois de receber o código, executamos o sistema no IntelliJ, testamos o menu (leitura, histórico, previsões, alertas e alteração de limiares) e rodamos os 12 testes. Cada integrante leu as classes da sua parte e estudou como funcionam o polimorfismo das estratégias de previsão, o tratamento das exceções customizadas, o `PreparedStatement` com `try-with-resources` e o uso de mocks no teste do serviço. A reflexão crítica foi revisada pelo grupo para refletir o que realmente pensamos sobre o sistema.

---

## 9. Divisão de tarefas

| Integrante | Responsabilidade |
|---|---|
| Gabriel Vergueiro | Domínio e exceções (sensores, `ComponenteTerritorial`, `DadosTerritorio`, exceções customizadas) |
| Isabella Piñeiro Santana | Persistência (`ConexaoBanco`, `MedicaoDAO`, `AlertaDAO`) |
| Pam Mitchell Barbosa Durham | Previsões, alertas, fábrica e testes (`EstrategiaPrevisao`, `ServicoMonitoramento`, `FabricaPrevisao`, testes) |

Todos os integrantes fizeram commits no repositório do GitHub.
