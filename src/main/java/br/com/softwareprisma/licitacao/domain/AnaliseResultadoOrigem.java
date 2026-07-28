package br.com.softwareprisma.licitacao.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "analise_resultado_origem")
@Getter
@Setter
@NoArgsConstructor
public class AnaliseResultadoOrigem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "item_persistido_id", nullable = false)
    private AnaliseResultadoItemPersistido itemPersistido;

    @Column(name = "cat_nome", nullable = false, length = 200)
    private String catNome;

    @Column(name = "engenheiro_nome", length = 200)
    private String engenheiroNome;
}
