package br.com.softwareprisma.licitacao.controller.form;

import br.com.softwareprisma.licitacao.domain.enums.PerfilEmpresa;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EmpresaAdminUsuarioForm {

    @NotBlank(message = "Informe o nome.")
    private String nome;

    @NotBlank(message = "Informe o login.")
    private String login;

    private String senha;

    @NotNull(message = "Selecione um perfil.")
    private PerfilEmpresa perfil = PerfilEmpresa.USUARIO;

    private Boolean ativo = true;
}
