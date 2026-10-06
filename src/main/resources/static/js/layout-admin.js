(function() {
    const shell = document.querySelector('.prisma-shell-admin');
    if (!shell) return;

    const toggle = document.getElementById('prismaSidebarToggleAdmin');
    const sidebar = document.getElementById('prismaSidebarAdmin');
    const storageKey = 'prisma-sidebar-admin-collapsed';

    const applyTooltip = () => {
        document.querySelectorAll('.prisma-sidebar-link, .prisma-sidebar-action').forEach((item) => {
            const text = item.innerText.replace(/\s+/g, ' ').trim();
            item.setAttribute('data-tooltip', text);
        });
    };

    const applyActiveState = () => {
        const path = window.location.pathname;
        document.querySelectorAll('.prisma-sidebar-link').forEach((link) => {
            const route = link.dataset.route || '';
            const matches = route === '/' ? path === '/' : path.startsWith(route);
            link.classList.toggle('active', matches);
        });
    };

    const applyCollapsedState = () => {
        const collapsed = localStorage.getItem(storageKey) === 'true';
        shell.classList.toggle('collapsed', collapsed);
        if (sidebar) {
            sidebar.classList.toggle('collapsed', collapsed);
        }
        document.body.classList.toggle('sidebar-collapsed', collapsed);
    };

    const setCollapsed = (collapsed) => {
        shell.classList.toggle('collapsed', collapsed);
        if (sidebar) {
            sidebar.classList.toggle('collapsed', collapsed);
        }
        document.body.classList.toggle('sidebar-collapsed', collapsed);
        localStorage.setItem(storageKey, String(collapsed));
    };

    applyTooltip();
    applyActiveState();
    applyCollapsedState();

    if (toggle) {
        toggle.addEventListener('click', function() {
            if (window.innerWidth < 992) {
                shell.classList.toggle('sidebar-open');
                return;
            }
            setCollapsed(!shell.classList.contains('collapsed'));
        });
    }

    document.addEventListener('click', function(event) {
        if (window.innerWidth >= 992 && shell.classList.contains('collapsed') === false) {
            const isClickInsideSidebar = sidebar?.contains(event.target);
            const isClickOnToggle = toggle?.contains(event.target);
            if (!isClickInsideSidebar && !isClickOnToggle) {
                setCollapsed(true);
            }
        }
    });

    const backdrop = document.getElementById('prismaSidebarBackdropAdmin');
    if (backdrop) {
        backdrop.addEventListener('click', function() {
            shell.classList.remove('sidebar-open');
        });
    }
})();
