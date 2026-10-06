const vinculosContainer = document.getElementById('vinculos');
const usuarioForm = vinculosContainer.closest('form');
const empresaOptionTemplate = document.getElementById('empresa-option-template');
const perfilOptionTemplate = document.getElementById('perfil-option-template');

document.getElementById('adicionar-vinculo').addEventListener('click', () => {
    const index = vinculosContainer.querySelectorAll('.vinculo-row').length;
    const row = document.createElement('div');
    row.className = 'row g-2 mb-2 vinculo-row';

    const colEmpresa = document.createElement('div');
    colEmpresa.className = 'col-md-6';

    const selectEmpresa = document.createElement('select');
    selectEmpresa.className = 'form-select empresa-select';
    selectEmpresa.name = `vinculos[${index}].empresaId`;
    selectEmpresa.required = true;

    const optionEmpresaVazia = document.createElement('option');
    optionEmpresaVazia.value = '';
    optionEmpresaVazia.textContent = 'Selecione uma empresa';
    selectEmpresa.appendChild(optionEmpresaVazia);
    selectEmpresa.appendChild(empresaOptionTemplate.content.cloneNode(true));

    colEmpresa.appendChild(selectEmpresa);
    row.appendChild(colEmpresa);

    const colPerfil = document.createElement('div');
    colPerfil.className = 'col-md-4';

    const selectPerfil = document.createElement('select');
    selectPerfil.className = 'form-select';
    selectPerfil.name = `vinculos[${index}].perfil`;
    selectPerfil.required = true;
    selectPerfil.appendChild(perfilOptionTemplate.content.cloneNode(true));

    colPerfil.appendChild(selectPerfil);
    row.appendChild(colPerfil);

    const colBotao = document.createElement('div');
    colBotao.className = 'col-md-2';

    const botaoRemover = document.createElement('button');
    botaoRemover.type = 'button';
    botaoRemover.className = 'btn btn-outline-danger w-100 remover-vinculo';
    botaoRemover.textContent = 'Remover';

    colBotao.appendChild(botaoRemover);
    row.appendChild(colBotao);
    vinculosContainer.appendChild(row);
});

vinculosContainer.addEventListener('click', event => {
    if (event.target.classList.contains('remover-vinculo')) {
        event.target.closest('.vinculo-row').remove();
    }
});

usuarioForm.addEventListener('submit', event => {
    const selecionadas = [...usuarioForm.querySelectorAll('.empresa-select')].map(select => select.value);
    if (new Set(selecionadas).size !== selecionadas.length) {
        event.preventDefault();
        alert('Não é permitido repetir uma empresa.');
    }
});
