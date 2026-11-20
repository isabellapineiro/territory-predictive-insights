package dominio;

/** Qualquer elemento monitorado do território (ponto de alagamento, UBS, rede elétrica...). */
public abstract class ComponenteTerritorial {
    private final String id;
    private final String nome;
    private final String regiao;
    private String status;

    protected ComponenteTerritorial(String id, String nome, String regiao) {
        this.id = id;
        this.nome = nome;
        this.regiao = regiao;
        this.status = "DESCONHECIDO";
    }

    public String getId() { return id; }
    public String getNome() { return nome; }
    public String getRegiao() { return regiao; }
    public String getStatus() { return status; }

    protected void setStatus(String status) { this.status = status; }

    /** Cada tipo de componente decide como calcula o próprio status. */
    public abstract String verificarStatus();
}
