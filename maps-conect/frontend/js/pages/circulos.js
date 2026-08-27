/**
 * Círculos de estudio.
 * Contrato esperado de GET /circulos:
 * { sesiones, misGrupos } con sesiones [{ id, titulo, materia, fecha, enVivo,
 * moderador, plataforma, descripcion, asistentes }] y misGrupos [{ id, nombre,
 * descripcion, miembros }].
 */
document.addEventListener('DOMContentLoaded', async () => {
    Layout.init('circulos.html');
    Layout.setPageTitle('Círculos de Estudio');

    const content = document.getElementById('main-content');
    if (!content) return;
    content.innerHTML = '<div class="circulos-container"><p class="loading-message">Cargando círculos de estudio…</p></div>';
    await loadCircles(content);
});

async function loadCircles(content, search = '') {
    let response = {};
    try {
        response = await API.request(`/circulos${search ? `?busqueda=${encodeURIComponent(search)}` : ''}`);
    } catch {
        // El endpoint se conectará cuando el módulo esté disponible en backend.
    }

    const sessions = Array.isArray(response.sesiones) ? response.sesiones : [];
    const groups = Array.isArray(response.misGrupos) ? response.misGrupos : [];
    renderCircles(content, sessions, groups);
}

function renderCircles(content, sessions, groups) {
    content.innerHTML = `
        <div class="circulos-container">
            <header class="module-heading"><h1>Círculos de estudio</h1><p>Encuentra sesiones de repaso y comunidades para aprender en conjunto.</p></header>
            <form class="top-action-bar" id="circle-search-form">
                <label class="visually-hidden" for="circle-search">Buscar círculo o sesión</label>
                <input class="search-input" id="circle-search" name="busqueda" type="search" placeholder="Buscar círculo o sesión por materia...">
                <button class="btn-solid" type="button" id="create-circle">+ Crear nuevo círculo</button>
            </form>
            <div class="circulos-grid">
                <section><h2 class="section-title">Próximas sesiones de repaso</h2>${sessions.length ? sessions.map(renderSession).join('') : renderEmptySessions()}</section>
                <aside><h2 class="section-title">Tus grupos permanentes</h2>${groups.length ? `<section class="card">${groups.map(renderGroup).join('')}</section>` : ''}<section class="card rules-card"><h2>Normas del círculo</h2><p>Los círculos son espacios colaborativos. Comparte material libre de plagio y mantén el respeto en las salas de videollamada.</p></section></aside>
            </div>
        </div>
    `;

    document.getElementById('circle-search-form')?.addEventListener('submit', event => {
        event.preventDefault();
        loadCircles(content, new FormData(event.currentTarget).get('busqueda')?.trim());
    });
    document.getElementById('create-circle')?.addEventListener('click', () => {
        // La pantalla de creación se conectará cuando esté disponible el endpoint POST /circulos.
    });
}

function renderSession(session) {
    const live = Boolean(session.enVivo);
    return `
        <article class="card session-card ${live ? 'live-session' : ''}">
            <div class="session-header"><div class="badges-wrap">${live ? '<span class="badge badge-live">● En vivo ahora</span>' : session.fecha ? `<span class="badge badge-time">${escapeHtml(session.fecha)}</span>` : ''}${session.materia ? `<span class="badge badge-materia">${escapeHtml(session.materia)}</span>` : ''}</div>${session.sala ? `<span class="attendees-count">${escapeHtml(session.sala)}</span>` : ''}</div>
            <h2 class="session-title">${escapeHtml(session.titulo || 'Sesión de repaso')}</h2>
            ${(session.moderador || session.plataforma) ? `<p class="session-info">${session.moderador ? `Moderador: <strong>${escapeHtml(session.moderador)}</strong>` : ''}${session.moderador && session.plataforma ? ' · ' : ''}${session.plataforma ? `Plataforma: <strong>${escapeHtml(session.plataforma)}</strong>` : ''}</p>` : ''}
            ${session.descripcion ? `<p class="session-desc">${escapeHtml(session.descripcion)}</p>` : ''}
            <div class="session-footer">${Number.isFinite(Number(session.asistentes)) ? `<span class="attendees-count">👥 ${Number(session.asistentes)} ${live ? 'estudiantes conectados' : 'confirmados'}</span>` : ''}<div class="session-actions">${session.materialDisponible ? '<button class="btn-clean" type="button">Ver material de apoyo</button>' : ''}<button class="btn-solid" type="button">${live ? 'Entrar a sala' : 'Confirmar asistencia'}</button></div></div>
        </article>
    `;
}

function renderGroup(group) {
    return `<article class="group-item"><span class="group-name">${escapeHtml(group.nombre || 'Grupo de estudio')}</span>${group.descripcion ? `<span class="group-description">${escapeHtml(group.descripcion)}</span>` : ''}<div class="group-meta">${Number.isFinite(Number(group.miembros)) ? `<span>${Number(group.miembros)} miembros</span>` : '<span></span>'}<button class="group-link" type="button">Ver grupo →</button></div></article>`;
}

function renderEmptySessions() {
    return '<section class="card empty-circles"><h2>Aún no hay sesiones programadas</h2><p>Las sesiones de repaso y los círculos disponibles aparecerán aquí cuando sean creados.</p></section>';
}

function escapeHtml(value) {
    return String(value ?? '').replace(/[&<>'"]/g, character => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', "'": '&#39;', '"': '&quot;' })[character]);
}
