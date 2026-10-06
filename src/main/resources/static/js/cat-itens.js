document.addEventListener('DOMContentLoaded', function() {
    const catItemsMain = document.querySelector('main[data-modo-edicao]');
    if (!catItemsMain) return;

    if (catItemsMain.dataset.mostrarModalLote === 'true') {
        const modalLote = new bootstrap.Modal(document.getElementById('modalCadastroLote'));
        modalLote.show();
    }

    if (catItemsMain.dataset.modoEdicao === 'true') return;

    const descricaoSelect = new TomSelect('#descricao', {
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

            try {
                const response = await fetch('/api/itens/sugestoes?termo=' + encodeURIComponent(query));
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
            const partes = value.split('|');
            if (partes.length === 2) {
                document.getElementById('unidade').value = partes[1];
                document.getElementById('descricao').value = partes[0];
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
});
