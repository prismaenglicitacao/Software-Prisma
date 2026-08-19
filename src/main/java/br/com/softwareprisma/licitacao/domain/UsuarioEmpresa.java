package br.com.softwareprisma.licitacao.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "usuario_empresa", uniqueConstraints = {
    @UniqueConstraint(name = "uk_usuario_empresa", columnNames = {"usuario_id", "empresa_id"})
})
@Getter
@Setter
@NoArgsConstructor
public class UsuarioEmpresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Column(nullable = false)
    private Boolean ativo = true;

    @Column(name = "data_concessao", nullable = false)
    private LocalDateTime dataConcessao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concedido_por")
    private Usuario concedidoPor;

    @PrePersist
    protected void onCreate() {
        if (dataConcessao == null) {
            dataConcessao = LocalDateTime.now();
        }
    }
}
