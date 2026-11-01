package br.com.nexo.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;


/** Identidade do par aplicação/impacto; não altere uma chave persistida. */
@Embeddable
public class AplicacaoImpactoId implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Column(name = "aplicacao_id", nullable = false)
    private Long aplicacaoId;

    @Column(name = "impacto_id", nullable = false)
    private Long impactoId;

    public AplicacaoImpactoId() {
    }

    public AplicacaoImpactoId(Long aplicacaoId, Long impactoId) {
        this.aplicacaoId = aplicacaoId;
        this.impactoId = impactoId;
    }

    public Long getAplicacaoId() {
        return aplicacaoId;
    }

    public void setAplicacaoId(Long aplicacaoId) {
        this.aplicacaoId = aplicacaoId;
    }

    public Long getImpactoId() {
        return impactoId;
    }

    public void setImpactoId(Long impactoId) {
        this.impactoId = impactoId;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof AplicacaoImpactoId that)) {
            return false;
        }
        return Objects.equals(aplicacaoId, that.aplicacaoId)
                && Objects.equals(impactoId, that.impactoId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(aplicacaoId, impactoId);
    }
}
