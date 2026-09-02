package br.com.softwareprisma.licitacao.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "analise_resultado_item_persistido")
@Getter
@Setter
@NoArgsConstructor
public class AnaliseResultadoItemPersistido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "resultado_persistido_id", nullable = false)
    private AnaliseResultadoPersistido resultadoPersistido;

    @Column(name = "descricao", nullable = false, columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "unidade", length = 20)
    private String unidade;

    @Column(name = "exigido", precision = 10, scale = 2)
    private BigDecimal exigido;

    @Column(name = "encontrado", precision = 10, scale = 2)
    private BigDecimal encontrado;

    @Column(name = "atende", nullable = false)
    private Boolean atende;

    @OneToMany(mappedBy = "itemPersistido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AnaliseResultadoOrigem> origens = new ArrayList<>();
}
