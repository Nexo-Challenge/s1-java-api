package br.com.nexo.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** IDs atribuídos explicitamente até o acordo sobre a estratégia de geração no banco. */
@Entity
@Table(name = "TB_IMPACTO_SOCIAL")
public class ImpactoSocial {

    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "titulo", unique = true, length = 150)
    private String titulo;

    @Column(name = "descricao", length = 1000)
    private String descricao;

    public ImpactoSocial() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}
