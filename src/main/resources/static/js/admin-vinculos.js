document.querySelectorAll('form.js-confirm-revocation').forEach(form => {
    form.addEventListener('submit', function(event) {
        if (!window.confirm(`Revogar acesso de ${this.dataset.nome}?`)) {
            event.preventDefault();
        }
    });
});

document.querySelectorAll('form.js-validate-user-selection').forEach(form => {
    form.addEventListener('submit', function(event) {
        const select = this.querySelector('select[name="usuarioId"]');
        if (!select.value) {
            event.preventDefault();
            alert('Selecione um usuário antes de vincular.');
        }
    });
});
