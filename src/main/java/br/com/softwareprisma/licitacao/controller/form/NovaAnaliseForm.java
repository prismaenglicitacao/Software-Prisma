package br.com.softwareprisma.licitacao.controller.form;

import br.com.softwareprisma.licitacao.domain.enums.Area;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class NovaAnaliseForm {

    @NotNull(message = "Selecione a area da analise.")
    private Area area;

    @Valid
    private List<AnaliseItemForm> itens = new ArrayList<>();
}
