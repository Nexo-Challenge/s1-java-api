package br.com.nexo.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** ID gerado pelo banco com IDENTITY, conforme acordo relatado pelo solicitante. */
@Entity
@Table(name = "TB_APLICACAO_NUCLEAR")
public class AplicacaoNuclear {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "area_aplicacao_id", nullable = false)
    private AreaAplicacao areaAplicacao;

    @Column(name = "titulo", length = 150, nullable = false)
    private String titulo;

    @Column(name = "resumo", length = 500, nullable = false)
    private String resumo;

    @Column(name = "descricao", length = 4000, nullable = false)
    private String descricao;

    public AplicacaoNuclear() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AreaAplicacao getAreaAplicacao() {
        return areaAplicacao;
    }

    public void setAreaAplicacao(AreaAplicacao areaAplicacao) {
        this.areaAplicacao = areaAplicacao;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getResumo() {
        return resumo;
    }

    public void setResumo(String resumo) {
        this.resumo = resumo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}
