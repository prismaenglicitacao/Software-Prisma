package br.com.softwareprisma.licitacao.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cat")
@Getter
@Setter
@NoArgsConstructor
public class Cat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "engenheiro_id", nullable = false)
    private Engenheiro engenheiro;

    @NotBlank
    @Column(nullable = false, length = 150)
    private String nome;

    @NotBlank
    @Column(name = "numero_cat", nullable = false, length = 60)
    private String numeroCat;

    @NotBlank
    @Column(nullable = false, length = 120)
    private String municipio;

    @Column(length = 1000)
    private String observacoes;

    @OneToMany(mappedBy = "cat", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<CatItem> itens = new ArrayList<>();
}
