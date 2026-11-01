package br.com.nexo.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

/**
 * Associação com conteúdo próprio. Atribua as referências dos pais antes de persistir.
 * A chave composta deve corresponder aos IDs dos pais; não altere um par persistido.
 * Sem cascatas: persistência e exclusão dos pais são operações separadas.
 */
@Entity
@Table(name = "TB_APLICACAO_IMPACTO")
public class AplicacaoImpacto {

    @EmbeddedId
    private AplicacaoImpactoId id = new AplicacaoImpactoId();

    @MapsId("aplicacaoId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "aplicacao_id", nullable = false)
    private AplicacaoNuclear aplicacao;

    @MapsId("impactoId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "impacto_id", nullable = false)
    private ImpactoSocial impacto;

    @Column(name = "explicacao", length = 2000)
    private String explicacao;

    @Column(name = "fonte_url", length = 500)
    private String fonteUrl;

    public AplicacaoImpacto() {
    }

    public AplicacaoImpactoId getId() {
        return id;
    }

    public void setId(AplicacaoImpactoId id) {
        this.id = id;
    }

    public AplicacaoNuclear getAplicacao() {
        return aplicacao;
    }

    public void setAplicacao(AplicacaoNuclear aplicacao) {
        this.aplicacao = aplicacao;
    }

    public ImpactoSocial getImpacto() {
        return impacto;
    }

    public void setImpacto(ImpactoSocial impacto) {
        this.impacto = impacto;
    }

    public String getExplicacao() {
        return explicacao;
    }

    public void setExplicacao(String explicacao) {
        this.explicacao = explicacao;
    }

    public String getFonteUrl() {
        return fonteUrl;
    }

    public void setFonteUrl(String fonteUrl) {
        this.fonteUrl = fonteUrl;
    }
}
