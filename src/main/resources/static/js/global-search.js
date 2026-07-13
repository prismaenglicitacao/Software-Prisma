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

        let html = '';

        // Engenheiros
        if (data.engenheiros.length > 0) {
            html += `
                <div class="mb-4">
                    <h6 class="text-uppercase text-muted mb-3 small fw-bold">
                        👤 Engenheiros
                    </h6>
            `;
            data.engenheiros.forEach(eng => {
                html += `
                    <div class="search-result-item p-3 border-bottom cursor-pointer hover-bg-light rounded"
                         data-type="engenheiro"
                         data-id="${eng.id}"
                         onclick="window.location.href='/engenheiros/${eng.id}'">
                        <div class="fw-bold">${eng.nome}</div>
                        <div class="text-muted small">
                            Área: ${eng.area || 'N/A'} • ${eng.totalCats} CAT(s) cadastrada(s)
                        </div>
                    </div>
                `;
            });
            html += '</div>';
        }

        // CATs
        if (data.cats.length > 0) {
            html += `
                <div class="mb-4">
                    <h6 class="text-uppercase text-muted mb-3 small fw-bold">
                        📁 CATs
                    </h6>
            `;
            data.cats.forEach(cat => {
                html += `
                    <div class="search-result-item p-3 border-bottom cursor-pointer hover-bg-light rounded"
                         data-type="cat"
                         data-id="${cat.id}"
                         onclick="window.location.href='/cats/${cat.id}'">
                        <div class="fw-bold">${cat.nome}</div>
                        <div class="text-muted small">
                            ${cat.numero} • Engenheiro: ${cat.engenheiroNome} • ${cat.totalItens} item(ns)
                        </div>
                    </div>
                `;
            });
            html += '</div>';
        }

        // Itens
        if (data.itens.length > 0) {
            html += `
                <div class="mb-4">
                    <h6 class="text-uppercase text-muted mb-3 small fw-bold">
                        📄 Itens
                    </h6>
            `;
            data.itens.forEach(item => {
                html += `
                    <div class="search-result-item p-3 border-bottom cursor-pointer hover-bg-light rounded"
                         data-type="item"
                         data-id="${item.id}"
                         onclick="window.location.href='/cats/${item.catId}/itens'">
                        <div class="fw-bold">${item.descricao}</div>
                        <div class="text-muted small">
                            CAT: ${item.catNome} (${item.catNumero})<br>
                            Engenheiro: ${item.engenheiroNome} • Área: ${item.area || 'N/A'}
                        </div>
                    </div>
                `;
            });
            html += '</div>';
        }

        if (data.temMaisResultados) {
            html += `
                <div class="text-center text-muted small py-2">
                    Exibindo os primeiros resultados...
                </div>
            `;
        }

        searchResults.innerHTML = html;

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
