/**
 * Directorio de Semestre Empresarial.
 * Contrato esperado de GET /empresarial:
 * { empresas: [{ id, nombre, ciudad, estado, modalidad, calificacion,
 *   totalResenas, descripcion, convenioActivo, tecnologias, experienciaDestacada }] }
 */
document.addEventListener('DOMContentLoaded', async () => {
    Layout.init('empresarial.html');
    Layout.setPageTitle('Semestre Empresarial');

    const content = document.getElementById('main-content');
    if (!content) return;

    content.innerHTML = '<div class="empresarial-container"><p class="loading-message">Cargando empresas vinculadas…</p></div>';
    await loadCompanies(content);
});

async function loadCompanies(content, filters = {}) {
    const params = new URLSearchParams();
    if (filters.busqueda) params.set('busqueda', filters.busqueda);
    if (filters.modalidad) params.set('modalidad', filters.modalidad);
    if (filters.calificacionMinima) params.set('calificacionMinima', filters.calificacionMinima);

    let response = {};
    try {
        response = await API.request(`/empresarial${params.size ? `?${params}` : ''}`);
    } catch {
        // El controlador todavía no expone datos; se conserva el estado vacío.
    }

    const companies = Array.isArray(response) ? response : (Array.isArray(response.empresas) ? response.empresas : []);
    renderCompanies(content, companies);
}

function renderCompanies(content, companies) {
    content.innerHTML = `
        <div class="empresarial-container">
            <header class="module-heading"><h1>Semestre Empresarial</h1><p>Explora empresas vinculadas y experiencias compartidas por la comunidad.</p></header>
            <form class="filter-bar" id="company-filter-form">
                <label class="visually-hidden" for="company-search">Buscar empresa</label>
                <input class="input-search" id="company-search" name="busqueda" type="search" placeholder="Buscar por empresa, tecnología o proyecto...">
                <select class="select-filter" name="modalidad" aria-label="Filtrar por modalidad"><option value="">Todas las modalidades</option><option value="HIBRIDO">Híbrido</option><option value="REMOTO">Remoto</option><option value="PRESENCIAL">Presencial</option></select>
                <select class="select-filter" name="calificacionMinima" aria-label="Filtrar por calificación"><option value="">Calificación: todas</option><option value="4.5">4.5 estrellas o más</option><option value="4.0">4.0 estrellas o más</option></select>
                <button class="btn-solid" type="submit">Buscar</button>
            </form>
            <section id="company-results" aria-live="polite">${companies.length ? companies.map(renderCompany).join('') : renderEmptyCompanies()}</section>
        </div>
    `;

    document.getElementById('company-filter-form')?.addEventListener('submit', event => {
        event.preventDefault();
        const formData = new FormData(event.currentTarget);
        loadCompanies(content, Object.fromEntries(formData.entries()));
    });
}

function renderCompany(company) {
    const rating = Number(company.calificacion);
    const hasRating = Number.isFinite(rating) && rating > 0;
    const location = [company.ciudad, company.estado].filter(Boolean).join(', ');
    const locationAndMode = [location, company.modalidad && `Modalidad: ${formatMode(company.modalidad)}`].filter(Boolean).join(' · ');
    const technologies = Array.isArray(company.tecnologias) ? company.tecnologias : [];
    const experience = company.experienciaDestacada;

    return `
        <article class="card company-card">
            <header class="company-header">
                <div><h2 class="company-name">${escapeHtml(company.nombre || 'Empresa')}</h2>${locationAndMode ? `<span class="company-location">${escapeHtml(locationAndMode)}</span>` : ''}</div>
                ${hasRating ? `<div class="rating-box"><span class="stars" aria-label="${rating} de 5 estrellas">${renderStars(rating)}</span><strong class="rating-number">${rating.toFixed(1)}</strong>${Number.isFinite(Number(company.totalResenas)) ? `<span class="rating-count">(${Number(company.totalResenas)} reseñas)</span>` : ''}</div>` : ''}
            </header>
            ${company.descripcion ? `<p class="company-desc"><strong>¿Qué hacen?</strong> ${escapeHtml(company.descripcion)}</p>` : ''}
            ${(company.convenioActivo || technologies.length) ? `<div class="tags-group">${company.convenioActivo ? '<span class="badge badge-green">Convenio oficial activo</span>' : ''}${technologies.map(item => `<span class="badge badge-blue">${escapeHtml(item)}</span>`).join('')}</div>` : ''}
            ${experience ? renderExperience(experience) : ''}
            <footer class="company-footer"><button class="btn-clean" type="button" data-company-id="${escapeHtml(company.id ?? '')}">Ver experiencias${Number.isFinite(Number(company.totalResenas)) ? ` (${Number(company.totalResenas)})` : ''}</button><button class="btn-solid" type="button" data-company-id="${escapeHtml(company.id ?? '')}">Ver requisitos de vinculación</button></footer>
        </article>
    `;
}

function renderExperience(experience) {
    const author = [experience.autor, experience.semestre && `${experience.semestre}.º semestre`].filter(Boolean).join(' · ');
    return `<div class="experience-box">${author ? `<strong class="experience-author">${escapeHtml(author)}</strong>` : ''}${experience.texto ? `<p class="experience-text"><em>“${escapeHtml(experience.texto)}”</em></p>` : ''}</div>`;
}

function renderEmptyCompanies() {
    return '<section class="card empty-companies"><h2>Aún no hay empresas disponibles</h2><p>Las empresas vinculadas y las experiencias de estudiantes aparecerán aquí cuando se registren.</p></section>';
}

function renderStars(rating) {
    const filled = Math.round(rating);
    return '★'.repeat(Math.min(5, filled)) + '☆'.repeat(Math.max(0, 5 - filled));
}

function formatMode(mode) {
    return String(mode).toLowerCase().replace(/^./, character => character.toUpperCase());
}

function escapeHtml(value) {
    return String(value ?? '').replace(/[&<>'"]/g, character => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', "'": '&#39;', '"': '&quot;' })[character]);
}
