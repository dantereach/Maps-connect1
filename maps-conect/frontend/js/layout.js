/**
 * MAPS Connect - Layout compartido (menú lateral plegable)
 */

const NAV_ITEMS = [
    { href: 'inicio.html', label: 'Inicio' },
    { href: 'perfil.html', label: 'Mi Perfil' },
    { href: 'foro.html', label: 'Foro de Dudas' },
    { href: 'tips.html', label: 'Tips Académicos' },
    { href: 'recursos.html', label: 'Apuntes' },
    { href: 'mensajes.html', label: 'Mensajes' },
    { href: 'empresarial.html', label: 'Semestre Empresarial' },
    { href: 'circulos.html', label: 'Círculos de Estudio' }
];

const Layout = {
    init(currentPage) {
        if (!Auth.requireAuth()) return;

        this.renderSidebar(currentPage);
        this.renderTopbar();
        this.bindToggle();
    },

    renderSidebar(currentPage) {
        const sidebar = document.getElementById('sidebar');
        if (!sidebar) return;

        const user = Auth.getUser();
        const navLinks = NAV_ITEMS.map(item => {
            const active = item.href === currentPage ? 'active' : '';
            return `<li><a href="${item.href}" class="${active}">${item.label}</a></li>`;
        }).join('');

        sidebar.innerHTML = `
            <div class="sidebar-header">
                <h1>MAPS Connect</h1>
                <small>${user?.nombre || 'Usuario'}</small>
            </div>
            <ul class="sidebar-nav">${navLinks}</ul>
            <div class="sidebar-footer">
                <button class="btn btn-sm btn-outline" id="btn-logout">Cerrar sesión</button>
            </div>
        `;

        document.getElementById('btn-logout')?.addEventListener('click', () => Auth.logout());
    },

    renderTopbar() {
        const topbar = document.getElementById('topbar');
        if (!topbar) return;

        topbar.innerHTML = `
            <button class="menu-toggle" id="sidebar-toggle" type="button"
                aria-label="Abrir menú" aria-controls="sidebar" aria-expanded="false">
                <span aria-hidden="true">☰</span>
            </button>
            <span id="topbar-title" class="visually-hidden"></span>
        `;
    },

    bindToggle() {
        const toggle = document.getElementById('sidebar-toggle');
        const sidebar = document.getElementById('sidebar');

        toggle?.addEventListener('click', () => {
            const isOpen = sidebar?.classList.toggle('open') ?? false;
            toggle.setAttribute('aria-expanded', String(isOpen));
            toggle.setAttribute('aria-label', isOpen ? 'Cerrar menú' : 'Abrir menú');
        });
    },

    setPageTitle(title) {
        const el = document.getElementById('topbar-title');
        if (el) el.textContent = title;
        document.title = `${title} | MAPS Connect`;
    }
};
