package br.com.softwareprisma.licitacao.controller.form;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CatItemLoteForm {

    @NotBlank(message = "Informe os itens para cadastro em lote.")
    private String itensTexto;

    public CatItemLoteForm(String itensTexto) {
        this.itensTexto = itensTexto;
    }
}
