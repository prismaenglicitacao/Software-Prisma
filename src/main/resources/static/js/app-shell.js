(function() {
    const shell = document.querySelector('.prisma-shell');
    if (!shell) return;

    const toggle = document.getElementById('prismaSidebarToggle');
    const sidebar = document.getElementById('prismaSidebar');
    const storageKey = 'prisma-sidebar-collapsed';

    const applyTooltip = () => {
        document.querySelectorAll('.prisma-sidebar-link, .prisma-sidebar-parent, .prisma-sidebar-action, .prisma-submenu-link').forEach((item) => {
            const text = item.innerText.replace(/\s+/g, ' ').trim();
            item.setAttribute('data-tooltip', text);
        });
    };

    const applyActiveState = () => {
        const path = window.location.pathname;
        const analysesExpanded = path.startsWith('/analises') || path.startsWith('/historico');

        document.querySelectorAll('.prisma-sidebar-link, .prisma-submenu-link').forEach((link) => {
            const route = link.dataset.route || '';
            const matches = route === '/' ? path === '/' : path.startsWith(route);
            link.classList.toggle('active', matches);
        });

        document.querySelectorAll('.prisma-sidebar-group').forEach((group) => {
            const parent = group.querySelector('.prisma-sidebar-parent');
            if (!parent) return;
            const shouldExpand = analysesExpanded || group.classList.contains('expanded');
            group.classList.toggle('expanded', shouldExpand);
            parent.setAttribute('aria-expanded', String(shouldExpand));
        });

        const analysesGroup = document.querySelector('.prisma-sidebar-group[data-group="analises"]');
        if (analysesGroup) {
            const parent = analysesGroup.querySelector('.prisma-sidebar-parent');
            const isOnAnalysesPage = path.startsWith('/analises') || path.startsWith('/historico');
            analysesGroup.classList.toggle('expanded', isOnAnalysesPage);
            if (parent) {
                parent.setAttribute('aria-expanded', String(isOnAnalysesPage));
            }
        }
    };

    document.querySelectorAll('.prisma-sidebar-parent').forEach((parent) => {
        parent.addEventListener('click', function() {
            const group = parent.closest('.prisma-sidebar-group');
            if (!group) return;
            const isExpanded = group.classList.contains('expanded');
            const shouldOpen = !isExpanded;
            group.classList.toggle('expanded', shouldOpen);
            parent.setAttribute('aria-expanded', String(shouldOpen));
        });
    });

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

    const backdrop = document.getElementById('prismaSidebarBackdrop');
    if (backdrop) {
        backdrop.addEventListener('click', function() {
            shell.classList.remove('sidebar-open');
        });
    }

    if (window.innerWidth < 992) {
        toggle?.addEventListener('click', function() {
            shell.classList.toggle('sidebar-open');
        });
    }
})();

(function() {
    const scrollPositionKey = 'scrollPosition';
    const scrollUrlKey = 'scrollUrl';

    document.addEventListener('click', function(event) {
        const link = event.target.closest('a.page-link');
        if (link && link.href) {
            sessionStorage.setItem(scrollPositionKey, window.scrollY);
            sessionStorage.setItem(scrollUrlKey, window.location.pathname);
        }
    });

    window.addEventListener('load', function() {
        const savedPosition = sessionStorage.getItem(scrollPositionKey);
        const savedUrl = sessionStorage.getItem(scrollUrlKey);

        if (savedPosition && savedUrl === window.location.pathname) {
            window.scrollTo(0, parseInt(savedPosition, 10));
            sessionStorage.removeItem(scrollPositionKey);
            sessionStorage.removeItem(scrollUrlKey);
        }
    });
})();
