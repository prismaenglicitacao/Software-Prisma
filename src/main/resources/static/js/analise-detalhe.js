const analiseMain = document.querySelector('main[data-area-analise]');

if (analiseMain) {
    const areaAnalise = analiseMain.dataset.areaAnalise;

    analiseMain.querySelectorAll('form.js-confirm-delete-item').forEach(form => {
        form.addEventListener('submit', event => {
            if (!window.confirm('Deseja realmente excluir este item?')) {
                event.preventDefault();
            }
        });
    });

    if (analiseMain.dataset.modoEdicao !== 'true') {
        const descricaoInput = document.getElementById('descricao');
        const itensRecentesSection = document.getElementById('itensRecentesSection');
        const itensRecentes = document.getElementById('itensRecentes');

        function montarUrlItens(path, termo) {
            const params = new URLSearchParams();
            if (termo) {
                params.set('termo', termo);
            }
            if (areaAnalise) {
                params.set('area', areaAnalise);
            }
            const query = params.toString();
            return query ? path + '?' + query : path;
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
                        button.textContent = item.descricao + ' (' + item.unidade + ')';
                        button.addEventListener('click', () => {
                            selecionarItemRecente(button.dataset.descricao, button.dataset.unidade);
                        });
                        itensRecentes.appendChild(button);
                    });
                } else {
                    itensRecentesSection.style.display = 'none';
                }
            } catch (error) {
                console.error('Erro ao carregar itens recentes:', error);
            }
        }

        function selecionarItemRecente(descricao, unidade) {
            const id = `${descricao}|${unidade}`;
            if (!descricaoSelect.options[id]) {
                descricaoSelect.addOption({
                    id: id,
                    descricao: descricao,
                    unidade: unidade
                });
            }
            descricaoSelect.setValue(id);
            document.getElementById('unidade').value = unidade;
            descricaoInput.value = descricao;
            document.getElementById('quantidade').focus();
        }

        descricaoSelect = new TomSelect('#descricao', {
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
                if (areaAnalise) {
                    params.set('area', areaAnalise);
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
                    html += '<div class="fw-semibold">' + escape(item.descricao) + '</div>';
                    html += '<div class="text-body-secondary small">';
                    html += escape(item.unidade);
                    if (item.quantidadeDisponivel != null) {
                        html += ' • Qtd: ' + escape(item.quantidadeDisponivel);
                    }
                    html += '</div>';
                    html += '</div>';
                    return html;
                },
                item: function(item, escape) {
                    return '<div>' + escape(item.descricao) + '</div>';
                }
            },
            onChange: function(value) {
                const selectedOption = this.options[value];
                if (selectedOption && selectedOption.unidade) {
                    document.getElementById('unidade').value = selectedOption.unidade;
                    descricaoInput.value = selectedOption.descricao;
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

        carregarItensRecentes();
    }
}
