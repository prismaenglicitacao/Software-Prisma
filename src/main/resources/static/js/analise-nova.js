const itens = [];
const itensLista = document.getElementById('itensLista');
const itensCount = document.getElementById('itensCount');
const novoItemErro = document.getElementById('novoItemErro');
const hiddenItensInputs = document.getElementById('hiddenItensInputs');
const analisarBtn = document.getElementById('analisarBtn');
const areaSelect = document.getElementById('area');
const descricaoInput = document.getElementById('novaItemDescricao');
const itensRecentesSection = document.getElementById('itensRecentesSection');
const itensRecentes = document.getElementById('itensRecentes');
const modoSelecaoMultipla = document.getElementById('modoSelecaoMultipla');
const selecaoMultiplaArea = document.getElementById('selecaoMultiplaArea');
const itensSelecionadosLista = document.getElementById('itensSelecionadosLista');
const totalItensSelecionados = document.getElementById('totalItensSelecionados');
const totalCapacidadeSelecionada = document.getElementById('totalCapacidadeSelecionada');
const erroSelecaoMultipla = document.getElementById('erroSelecaoMultipla');
const quantidadeField = document.getElementById('quantidadeField');
let descricaoSelect;
let itensSelecionadosMultipla = [];
let modoMultiplaAtivo = false;

function montarUrlItens(path, termo) {
    const params = new URLSearchParams();
    if (termo) {
        params.set('termo', termo);
    }
    const area = areaSelect.value.trim();
    if (area) {
        params.set('area', area);
    }
    const query = params.toString();
    return query ? `${path}?${query}` : path;
}

async function carregarItensRecentes() {
    try {
        const response = await fetch(montarUrlItens('/api/itens/recentes'));
        const data = await response.json();

        if (data.length > 0) {
            itensRecentesSection.style.display = 'block';
            itensRecentes.innerHTML = '';
            data.forEach(item => {
                const button = document.createElement('button');
                button.type = 'button';
                button.className = 'btn btn-sm btn-outline-primary';
                button.dataset.descricao = item.descricao;
                button.dataset.unidade = item.unidade;

                const descricaoText = document.createTextNode(item.descricao);
                button.appendChild(descricaoText);

                const unidadeSpan = document.createElement('span');
                unidadeSpan.className = 'text-body-secondary';
                unidadeSpan.textContent = ` (${item.unidade})`;
                button.appendChild(unidadeSpan);

                if (item.quantidadeDisponivel != null) {
                    const qtdSpan = document.createElement('div');
                    qtdSpan.className = 'text-body-secondary item-qtd-disponivel';
                    qtdSpan.textContent = `Disponível: ${item.quantidadeDisponivel}`;
                    button.appendChild(qtdSpan);
                }

                button.addEventListener('click', () => {
                    const quantidadeDisponivel = item.quantidadeDisponivel;
                    selecionarItemRecente(button.dataset.descricao, button.dataset.unidade, quantidadeDisponivel);
                });
                itensRecentes.appendChild(button);
            });
        } else {
            itensRecentesSection.style.display = 'none';
            itensRecentes.innerHTML = '';
        }
    } catch (error) {
        console.error('Erro ao carregar itens recentes:', error);
    }
}

function selecionarItemRecente(descricao, unidade, quantidadeDisponivel) {
    if (modoMultiplaAtivo) {
        const quantidade = quantidadeDisponivel || 0;
        const jaSelecionado = itensSelecionadosMultipla.find(
            item => item.descricao === descricao && item.unidade === unidade
        );

        if (!jaSelecionado) {
            itensSelecionadosMultipla.push({
                descricao: descricao,
                unidade: unidade,
                quantidade: quantidade
            });
            erroSelecaoMultipla.textContent = '';
            atualizarListaSelecionados();
        }
    } else {
        const id = `${descricao}|${unidade}`;
        if (!descricaoSelect.options[id]) {
            descricaoSelect.addOption({
                id: id,
                descricao: descricao,
                unidade: unidade
            });
        }
        descricaoSelect.setValue(id);
        document.getElementById('novaItemUnidade').value = unidade;
        descricaoInput.value = descricao;
        document.getElementById('novaItemQuantidade').focus();
    }
}

areaSelect.addEventListener('change', () => {
    carregarItensRecentes();
});

modoSelecaoMultipla.addEventListener('change', () => {
    modoMultiplaAtivo = modoSelecaoMultipla.checked;
    const termoAtual = descricaoSelect.control_input.value.trim();

    if (modoMultiplaAtivo) {
        selecaoMultiplaArea.style.display = 'block';
        quantidadeField.style.display = 'none';
        document.getElementById('novaItemUnidade').value = '';
        document.getElementById('botaoAdicionarItem').style.display = 'none';
        descricaoSelect.clear();
        itensSelecionadosMultipla = [];
        atualizarListaSelecionados();
        descricaoSelect.clearOptions();
        if (termoAtual.length >= 2) {
            descricaoSelect.load(termoAtual);
        }
    } else {
        selecaoMultiplaArea.style.display = 'none';
        quantidadeField.style.display = 'block';
        document.getElementById('botaoAdicionarItem').style.display = 'block';
        descricaoSelect.clear();
        itensSelecionadosMultipla = [];
        descricaoSelect.clearOptions();
        if (termoAtual.length >= 2) {
            descricaoSelect.load(termoAtual);
        }
    }
});

function toggleItemSelecionado(checkbox) {
    const descricao = checkbox.dataset.descricao;
    const unidade = checkbox.dataset.unidade;
    const quantidade = parseFloat(checkbox.dataset.quantidade);
    const jaSelecionado = itensSelecionadosMultipla.find(
        item => item.descricao === descricao && item.unidade === unidade
    );

    if (jaSelecionado) {
        itensSelecionadosMultipla = itensSelecionadosMultipla.filter(
            item => item.descricao !== descricao || item.unidade !== unidade
        );
        checkbox.checked = false;
    } else {
        itensSelecionadosMultipla.push({
            descricao: descricao,
            unidade: unidade,
            quantidade: quantidade
        });
        checkbox.checked = true;
        erroSelecaoMultipla.textContent = '';
    }

    atualizarListaSelecionados();
}

function atualizarListaSelecionados() {
    itensSelecionadosLista.innerHTML = '';

    if (itensSelecionadosMultipla.length === 0) {
        totalItensSelecionados.textContent = '0 itens selecionados';
        totalCapacidadeSelecionada.textContent = '';
        document.getElementById('capacidadeMaximaLabel').style.display = 'none';
        return;
    }

    itensSelecionadosMultipla.forEach((item, index) => {
        const itemElement = document.createElement('div');
        itemElement.className = 'list-group-item d-flex justify-content-between align-items-center';

        const infoDiv = document.createElement('div');

        const descricaoDiv = document.createElement('div');
        descricaoDiv.className = 'fw-semibold';
        descricaoDiv.textContent = item.descricao;
        infoDiv.appendChild(descricaoDiv);

        const unidadeDiv = document.createElement('div');
        unidadeDiv.className = 'text-body-secondary small';
        unidadeDiv.textContent = item.quantidade + ' ' + item.unidade;
        infoDiv.appendChild(unidadeDiv);

        itemElement.appendChild(infoDiv);

        const removeButton = document.createElement('button');
        removeButton.type = 'button';
        removeButton.className = 'btn btn-sm btn-outline-danger';
        removeButton.textContent = 'Remover';
        removeButton.addEventListener('click', function() {
            removerItemSelecionado(index);
        });
        itemElement.appendChild(removeButton);

        itensSelecionadosLista.appendChild(itemElement);
    });

    totalItensSelecionados.textContent = `${itensSelecionadosMultipla.length} itens selecionados`;

    const somaPorUnidade = {};
    itensSelecionadosMultipla.forEach(item => {
        if (!somaPorUnidade[item.unidade]) {
            somaPorUnidade[item.unidade] = 0;
        }
        somaPorUnidade[item.unidade] += item.quantidade;
    });

    const unidades = Object.keys(somaPorUnidade);

    if (unidades.length === 1) {
        const unidade = unidades[0];
        const total = somaPorUnidade[unidade];
        totalCapacidadeSelecionada.textContent = `${total.toFixed(2).replace('.', ',')} ${unidade}`;
        document.getElementById('capacidadeMaximaLabel').style.display = 'inline';
    } else {
        const somasTexto = unidades.map(unidade =>
            `${unidade}: ${somaPorUnidade[unidade].toFixed(2).replace('.', ',')}`
        ).join(', ');
        totalCapacidadeSelecionada.textContent = somasTexto;
        document.getElementById('capacidadeMaximaLabel').style.display = 'inline';
    }
}

function removerItemSelecionado(index) {
    itensSelecionadosMultipla.splice(index, 1);
    atualizarListaSelecionados();
}

document.getElementById('botaoAdicionarSelecionados').addEventListener('click', () => {
    erroSelecaoMultipla.textContent = '';

    if (itensSelecionadosMultipla.length === 0) {
        erroSelecaoMultipla.textContent = 'Selecione ao menos um item para adicionar.';
        return;
    }

    itensSelecionadosMultipla.forEach(item => {
        itens.push({
            descricao: item.descricao,
            quantidade: item.quantidade.toString(),
            quantidadeNormalized: item.quantidade.toString(),
            unidade: item.unidade
        });
    });

    itensSelecionadosMultipla = [];
    descricaoSelect.clear();
    atualizarListaSelecionados();
    atualizarItens();
    modoSelecaoMultipla.checked = false;
    modoSelecaoMultipla.dispatchEvent(new Event('change'));
});

document.addEventListener('DOMContentLoaded', function() {
    descricaoSelect = new TomSelect('#novaItemDescricao', {
        create: false,
        maxItems: 1,
        valueField: 'id',
        labelField: 'descricao',
        searchField: 'descricao',
        load: async function(query, callback) {
            if (query.length < 2) {
                callback();
                return;
            }

            const params = new URLSearchParams();
            params.set('termo', query);
            const area = areaSelect.value.trim();
            if (area) {
                params.set('area', area);
            }

            if (itens.length > 0) {
                const itensJaAdicionados = itens.map(item => `${item.descricao}|${item.unidade}`);
                itensJaAdicionados.forEach(item => params.append('itensJaAdicionados', item));
            }

            try {
                const response = await fetch('/api/itens/sugestoes?' + params.toString());
                const data = await response.json();
                const dataWithIds = data.map((item, index) => ({
                    ...item,
                    id: `${item.descricao}|${item.unidade}`
                }));
                callback(dataWithIds);
            } catch (error) {
                console.error('Erro ao buscar sugestões:', error);
                callback();
            }
        },
        render: {
            option: function(item, escape) {
                let html = '<div class="py-2">';
                if (modoMultiplaAtivo) {
                    html += '<div class="form-check js-multiple-option-control">';
                    html += '<input class="form-check-input" type="checkbox" data-action="toggle-item" data-descricao="' + escape(item.descricao) + '" data-unidade="' + escape(item.unidade) + '" data-quantidade="' + escape(item.quantidadeDisponivel || 0) + '">';
                    html += '<label class="form-check-label js-multiple-option-control">';
                }
                html += '<div class="fw-semibold">' + escape(item.descricao) + '</div>';
                html += '<div class="text-body-secondary small">';
                html += escape(item.unidade);
                if (item.quantidadeDisponivel != null) {
                    html += ' • Disponível: ' + escape(item.quantidadeDisponivel);
                }
                html += '</div>';
                if (modoMultiplaAtivo) {
                    html += '</label>';
                    html += '</div>';
                }
                html += '</div>';
                return html;
            },
            item: function(item, escape) {
                return '<div>' + escape(item.descricao) + '</div>';
            }
        },
        onChange: function(value) {
            if (!modoMultiplaAtivo && value) {
                const partes = value.split('|');
                if (partes.length === 2) {
                    document.getElementById('novaItemUnidade').value = partes[1];
                    descricaoInput.value = partes[0];
                }
            }
        },
        onDropdownOpen: function() {
            const dropdown = this.dropdown_content;
            const resultsCount = this.options.length;
            if (resultsCount > 0) {
                const countDiv = document.createElement('div');
                countDiv.className = 'px-3 py-2 text-body-secondary small border-bottom';
                countDiv.textContent = `${resultsCount} item(s) encontrado(s)`;
                dropdown.insertBefore(countDiv, dropdown.firstChild);
            }
        }
    });

    const dropdown = descricaoSelect.dropdown_content;
    dropdown.addEventListener('click', function(event) {
        if (event.target.closest('.js-multiple-option-control')) {
            event.stopPropagation();
        }
    }, true);
    dropdown.addEventListener('change', function(event) {
        const checkbox = event.target.closest('[data-action="toggle-item"]');
        if (checkbox) {
            toggleItemSelecionado(checkbox);
        }
    }, true);
});

document.getElementById('novaItemQuantidade').addEventListener('keydown', function(event) {
    if (event.key === 'Enter') {
        event.preventDefault();
        document.getElementById('botaoAdicionarItem').click();
    }
});

function atualizarItens() {
    itensLista.innerHTML = '';
    hiddenItensInputs.innerHTML = '';

    if (itens.length === 0) {
        itensCount.textContent = 'Nenhum item adicionado';
        analisarBtn.disabled = true;
        return;
    }

    itensCount.textContent = `${itens.length} item(s) adicionados`;
    analisarBtn.disabled = false;

    itens.forEach((item, index) => {
        const itemElement = document.createElement('div');
        itemElement.className = 'list-group-item d-flex justify-content-between align-items-center gap-3';

        const infoDiv = document.createElement('div');

        const descricaoDiv = document.createElement('div');
        descricaoDiv.className = 'fw-semibold text-success';
        descricaoDiv.textContent = '✓ ' + item.descricao;
        infoDiv.appendChild(descricaoDiv);

        const unidadeDiv = document.createElement('div');
        unidadeDiv.className = 'text-body-secondary mt-1';
        unidadeDiv.textContent = item.quantidade + ' ' + item.unidade;
        infoDiv.appendChild(unidadeDiv);

        itemElement.appendChild(infoDiv);

        const removeButton = document.createElement('button');
        removeButton.type = 'button';
        removeButton.className = 'btn btn-sm btn-outline-danger';
        removeButton.textContent = 'Remover';
        removeButton.addEventListener('click', () => {
            itens.splice(index, 1);
            atualizarItens();
        });
        itemElement.appendChild(removeButton);
        itensLista.appendChild(itemElement);

        const descricaoInputHidden = document.createElement('input');
        descricaoInputHidden.type = 'hidden';
        descricaoInputHidden.name = `itens[${index}].descricao`;
        descricaoInputHidden.value = item.descricao;
        hiddenItensInputs.appendChild(descricaoInputHidden);

        const quantidadeInput = document.createElement('input');
        quantidadeInput.type = 'hidden';
        quantidadeInput.name = `itens[${index}].quantidade`;
        quantidadeInput.value = item.quantidadeNormalized;
        hiddenItensInputs.appendChild(quantidadeInput);

        const unidadeInput = document.createElement('input');
        unidadeInput.type = 'hidden';
        unidadeInput.name = `itens[${index}].unidade`;
        unidadeInput.value = item.unidade;
        hiddenItensInputs.appendChild(unidadeInput);
    });
}

document.getElementById('botaoAdicionarItem').addEventListener('click', () => {
    novoItemErro.textContent = '';

    const descricao = descricaoInput.value.trim();
    const quantidadeRaw = document.getElementById('novaItemQuantidade').value.trim();
    const unidade = document.getElementById('novaItemUnidade').value.trim();
    const selectedValue = descricaoSelect.getValue();
    if (!selectedValue || !descricaoSelect.options[selectedValue]) {
        novoItemErro.textContent = 'Selecione um item da lista de sugestões.';
        return;
    }

    if (!quantidadeRaw) {
        novoItemErro.textContent = 'Informe a quantidade do item.';
        return;
    }

    const quantidade = parseFloat(quantidadeRaw.replace(',', '.'));
    if (Number.isNaN(quantidade) || quantidade <= 0) {
        novoItemErro.textContent = 'Informe uma quantidade maior que zero.';
        return;
    }

    if (!unidade) {
        novoItemErro.textContent = 'Informe a unidade do item.';
        return;
    }

    itens.push({
        descricao,
        quantidade: quantidadeRaw,
        quantidadeNormalized: quantidade.toString(),
        unidade
    });

    descricaoSelect.clear();
    descricaoSelect.focus();
    document.getElementById('novaItemQuantidade').value = '';
    document.getElementById('novaItemUnidade').value = '';
    atualizarItens();
});

carregarItensRecentes();
atualizarItens();
