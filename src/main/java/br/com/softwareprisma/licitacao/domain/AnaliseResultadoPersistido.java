package br.com.softwareprisma.licitacao.domain;

import br.com.softwareprisma.licitacao.domain.enums.ResultadoAnalise;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "analise_resultado_persistido")
@Getter
@Setter
@NoArgsConstructor
public class AnaliseResultadoPersistido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "analise_id", nullable = false, unique = true)
    private Long analiseId;

    @Enumerated(EnumType.STRING)
    @Column(name = "resultado", nullable = false, length = 20)
    private ResultadoAnalise resultado;

    @Column(name = "cobertura", precision = 5, scale = 2)
    private BigDecimal cobertura;

    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao = LocalDateTime.now();

    @OneToMany(mappedBy = "resultadoPersistido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AnaliseResultadoItemPersistido> itens = new ArrayList<>();
}
