package br.com.softwareprisma.licitacao.controller.form;

import br.com.softwareprisma.licitacao.domain.AnaliseItem;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class AnaliseItemForm {

    @NotBlank(message = "Informe a descricao.")
    private String descricao;

    @NotNull(message = "Informe a quantidade.")
    @DecimalMin(value = "0.0", inclusive = false, message = "Informe uma quantidade maior que zero.")
    private BigDecimal quantidade;

    @NotBlank(message = "Informe a unidade.")
    private String unidade;

    public static AnaliseItemForm fromEntity(AnaliseItem item) {
        AnaliseItemForm form = new AnaliseItemForm();
        form.setDescricao(item.getDescricao());
        form.setQuantidade(item.getQuantidade());
        form.setUnidade(item.getUnidade());
        return form;
    }

    public AnaliseItem toEntity() {
        AnaliseItem item = new AnaliseItem();
        item.setDescricao(descricao);
        item.setQuantidade(quantidade);
        item.setUnidade(unidade);
        return item;
    }
}
