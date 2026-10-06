document.addEventListener('DOMContentLoaded', function() {
    const globalSearchInput = document.getElementById('globalSearchInput');
    const modalSearchInput = document.getElementById('modalSearchInput');
    const searchResults = document.getElementById('searchResults');
    const modal = new bootstrap.Modal(document.getElementById('globalSearchModal'));
    
    let searchTimeout = null;
    let selectedIndex = -1;
    let currentResults = [];

    // Abrir modal ao clicar no input da navbar
    globalSearchInput.addEventListener('click', function() {
        modal.show();
        setTimeout(() => modalSearchInput.focus(), 100);
    });

    // Atalho Ctrl+K
    document.addEventListener('keydown', function(e) {
        if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
            e.preventDefault();
            modal.show();
            setTimeout(() => modalSearchInput.focus(), 100);
        }
    });

    // Fechar com ESC
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape' && modal._isShown) {
            modal.hide();
        }
    });

    // Fechar ao clicar fora
    document.getElementById('globalSearchModal').addEventListener('click', function(e) {
        if (e.target === this) {
            modal.hide();
        }
    });

    // Pesquisa com debounce
    modalSearchInput.addEventListener('input', function() {
        const termo = this.value.trim();
        
        clearTimeout(searchTimeout);
        
        if (termo.length === 0) {
            searchResults.innerHTML = `
                <div class="text-center text-muted py-5">
                    <p class="mb-0">Digite para pesquisar...</p>
                </div>
            `;
            currentResults = [];
            selectedIndex = -1;
            return;
        }
        
        searchTimeout = setTimeout(() => {
            realizarPesquisa(termo);
        }, 300);
    });

    // Navegação por teclado
    modalSearchInput.addEventListener('keydown', function(e) {
        if (e.key === 'ArrowDown') {
            e.preventDefault();
            selectedIndex = Math.min(selectedIndex + 1, currentResults.length - 1);
            atualizarSelecao();
        } else if (e.key === 'ArrowUp') {
            e.preventDefault();
            selectedIndex = Math.max(selectedIndex - 1, 0);
            atualizarSelecao();
        } else if (e.key === 'Enter' && selectedIndex >= 0) {
            e.preventDefault();
            if (currentResults[selectedIndex]) {
                currentResults[selectedIndex].element.click();
            }
        }
    });

    function atualizarSelecao() {
        const items = searchResults.querySelectorAll('.search-result-item');
        items.forEach((item, index) => {
            if (index === selectedIndex) {
                item.classList.add('active');
                item.scrollIntoView({ block: 'nearest' });
            } else {
                item.classList.remove('active');
            }
        });
    }

    async function realizarPesquisa(termo) {
        searchResults.innerHTML = `
            <div class="text-center py-5">
                <div class="spinner-border text-primary" role="status">
                    <span class="visually-hidden">Carregando...</span>
                </div>
            </div>
        `;

        try {
            const response = await fetch(`/api/pesquisa?q=${encodeURIComponent(termo)}`);
            const data = await response.json();
            
            renderizarResultados(data);
        } catch (error) {
            console.error('Erro na pesquisa:', error);
            searchResults.innerHTML = `
                <div class="text-center text-danger py-5">
                    <p class="mb-0">Erro ao realizar pesquisa. Tente novamente.</p>
                </div>
            `;
        }
    }

    function renderizarResultados(data) {
        currentResults = [];
        selectedIndex = -1;
        
        if (data.engenheiros.length === 0 && data.cats.length === 0 && data.itens.length === 0) {
            searchResults.innerHTML = `
                <div class="text-center text-muted py-5">
                    <p class="mb-0">Nenhum resultado encontrado.</p>
                </div>
            `;
            return;
        }

        // Engenheiros
        if (data.engenheiros.length > 0) {
            const engenheirosDiv = document.createElement('div');
            engenheirosDiv.className = 'mb-4';
            
            const engenheirosTitle = document.createElement('h6');
            engenheirosTitle.className = 'text-uppercase text-muted mb-3 small fw-bold';
            engenheirosTitle.textContent = '👤 Engenheiros';
            engenheirosDiv.appendChild(engenheirosTitle);
            
            data.engenheiros.forEach(eng => {
                const itemDiv = document.createElement('div');
                itemDiv.className = 'search-result-item';
                itemDiv.dataset.type = 'engenheiro';
                itemDiv.dataset.id = eng.id;
                itemDiv.onclick = function() {
                    window.location.href = '/engenheiros/' + eng.id;
                };
                
                const nomeDiv = document.createElement('div');
                nomeDiv.className = 'fw-bold';
                nomeDiv.textContent = eng.nome;
                itemDiv.appendChild(nomeDiv);
                
                const detalhesDiv = document.createElement('div');
                detalhesDiv.className = 'text-muted small';
                detalhesDiv.textContent = 'Área: ' + (eng.area || 'N/A') + ' • ' + eng.totalCats + ' CAT(s) cadastrada(s)';
                itemDiv.appendChild(detalhesDiv);
                
                engenheirosDiv.appendChild(itemDiv);
            });
            
            searchResults.appendChild(engenheirosDiv);
        }

        // CATs
        if (data.cats.length > 0) {
            const catsDiv = document.createElement('div');
            catsDiv.className = 'mb-4';
            
            const catsTitle = document.createElement('h6');
            catsTitle.className = 'text-uppercase text-muted mb-3 small fw-bold';
            catsTitle.textContent = '📁 CATs';
            catsDiv.appendChild(catsTitle);
            
            data.cats.forEach(cat => {
                const itemDiv = document.createElement('div');
                itemDiv.className = 'search-result-item';
                itemDiv.dataset.type = 'cat';
                itemDiv.dataset.id = cat.id;
                itemDiv.onclick = function() {
                    window.location.href = '/cats/' + cat.id;
                };
                
                const nomeDiv = document.createElement('div');
                nomeDiv.className = 'fw-bold';
                nomeDiv.textContent = cat.nome;
                itemDiv.appendChild(nomeDiv);
                
                const detalhesDiv = document.createElement('div');
                detalhesDiv.className = 'text-muted small';
                detalhesDiv.textContent = 'Engenheiro: ' + cat.engenheiroNome + ' • ' + cat.totalItens + ' item(ns)';
                itemDiv.appendChild(detalhesDiv);
                
                catsDiv.appendChild(itemDiv);
            });
            
            searchResults.appendChild(catsDiv);
        }

        // Itens
        if (data.itens.length > 0) {
            const itensDiv = document.createElement('div');
            itensDiv.className = 'mb-4';
            
            const itensTitle = document.createElement('h6');
            itensTitle.className = 'text-uppercase text-muted mb-3 small fw-bold';
            itensTitle.textContent = '📄 Itens';
            itensDiv.appendChild(itensTitle);
            
            data.itens.forEach(item => {
                const itemDiv = document.createElement('div');
                itemDiv.className = 'search-result-item';
                itemDiv.dataset.type = 'item';
                itemDiv.dataset.id = item.id;
                itemDiv.onclick = function() {
                    window.location.href = '/cats/' + item.catId + '/itens';
                };
                
                const descricaoDiv = document.createElement('div');
                descricaoDiv.className = 'fw-bold';
                descricaoDiv.textContent = item.descricao;
                itemDiv.appendChild(descricaoDiv);
                
                const detalhesDiv = document.createElement('div');
                detalhesDiv.className = 'text-muted small';
                
                const catSpan = document.createElement('span');
                catSpan.textContent = 'CAT: ' + item.catNome;
                detalhesDiv.appendChild(catSpan);
                
                const br = document.createElement('br');
                detalhesDiv.appendChild(br);
                
                const engenheiroSpan = document.createElement('span');
                engenheiroSpan.textContent = 'Engenheiro: ' + item.engenheiroNome + ' • Área: ' + (item.area || 'N/A');
                detalhesDiv.appendChild(engenheiroSpan);
                
                itemDiv.appendChild(detalhesDiv);
                
                itensDiv.appendChild(itemDiv);
            });
            
            searchResults.appendChild(itensDiv);
        }

        if (data.temMaisResultados) {
            const maisDiv = document.createElement('div');
            maisDiv.className = 'text-center text-muted small py-2';
            maisDiv.textContent = 'Exibindo os primeiros resultados...';
            searchResults.appendChild(maisDiv);
        }

        // Armazenar referências para navegação por teclado
        const items = searchResults.querySelectorAll('.search-result-item');
        items.forEach((item, index) => {
            currentResults.push({ element: item });
        });
    }

    // Limpar input ao fechar modal
    document.getElementById('globalSearchModal').addEventListener('hidden.bs.modal', function() {
        modalSearchInput.value = '';
        searchResults.innerHTML = `
            <div class="text-center text-muted py-5">
                <p class="mb-0">Digite para pesquisar...</p>
            </div>
        `;
        currentResults = [];
        selectedIndex = -1;
    });
});
