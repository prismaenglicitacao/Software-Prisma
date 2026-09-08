package br.com.softwareprisma.licitacao.controller.form;

import br.com.softwareprisma.licitacao.domain.enums.PerfilEmpresa;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class AdminUsuarioForm {

    @NotBlank(message = "Informe o nome.")
    private String nome;

    @NotBlank(message = "Informe o login.")
    private String login;

    @NotBlank(message = "Informe a senha.")
    private String senha;

    @Valid
    @NotEmpty(message = "Vincule o usuário a pelo menos uma empresa.")
    private List<VinculoForm> vinculos = new ArrayList<>();

    @Getter
    @Setter
    @NoArgsConstructor
    public static class VinculoForm {

        @NotNull(message = "Selecione uma empresa.")
        private Long empresaId;

        @NotNull(message = "Selecione um perfil.")
        private PerfilEmpresa perfil = PerfilEmpresa.USUARIO;
    }
}
