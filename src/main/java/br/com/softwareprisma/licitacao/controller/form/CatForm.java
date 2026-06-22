package br.com.softwareprisma.licitacao.controller.form;

import br.com.softwareprisma.licitacao.domain.Cat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CatForm {

    @NotNull(message = "Selecione o engenheiro.")
    private Long engenheiroId;

    @NotBlank(message = "Informe o nome da CAT.")
    private String nome;

    @NotBlank(message = "Informe o numero da CAT.")
    private String numeroCat;

    @NotBlank(message = "Informe o municipio.")
    private String municipio;

    private String observacoes;

    public static CatForm fromEntity(Cat cat) {
        CatForm form = new CatForm();
        form.setEngenheiroId(cat.getEngenheiro().getId());
        form.setNome(cat.getNome());
        form.setNumeroCat(cat.getNumeroCat());
        form.setMunicipio(cat.getMunicipio());
        form.setObservacoes(cat.getObservacoes());
        return form;
    }

    public Cat toEntity() {
        Cat cat = new Cat();
        cat.setNome(nome);
        cat.setNumeroCat(numeroCat);
        cat.setMunicipio(municipio);
        cat.setObservacoes(observacoes);
        return cat;
    }
}
