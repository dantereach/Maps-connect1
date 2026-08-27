/**
 * Panel de inicio.
 * Contrato esperado de GET /inicio:
 * { perfil, materias, publicaciones, circuloActual, asesoria, empresaDestacada }
 */
document.addEventListener('DOMContentLoaded', async () => {
    Layout.init('inicio.html');
    Layout.setPageTitle('Inicio');

    const content = document.getElementById('main-content');
    if (!content) return;

    content.innerHTML = '<div class="inicio-container"><p class="loading-message">Cargando tu información académica…</p></div>';

    let dashboard = {};
    try {
        dashboard = await API.request('/inicio');
    } catch {
        // El endpoint se implementará en el backend. La UI mantiene estados vacíos.
    }

    renderDashboard(content, dashboard || {});
});

function renderDashboard(content, data) {
    const perfil = data.perfil || null;
    const materias = Array.isArray(data.materias) ? data.materias : [];
    const publicaciones = Array.isArray(data.publicaciones) ? data.publicaciones : [];
    const accesos = renderAccessCards(data);

    content.innerHTML = `
        <div class="inicio-container">
            ${perfil ? renderBanner(perfil) : ''}
            <div class="inicio-layout">
                <section class="feed-column" aria-label="Actividad reciente">
                    ${materias.length ? renderQuickQuestion(materias) : ''}
                    ${publicaciones.length ? publicaciones.map(renderPost).join('') : renderEmptyFeed()}
                </section>
                ${accesos ? `<aside class="sidebar-section" aria-label="Accesos directos">${accesos}</aside>` : ''}
            </div>
        </div>
    `;

    document.getElementById('quick-question-form')?.addEventListener('submit', event => {
        event.preventDefault();
        window.location.href = 'foro.html';
    });
}

function renderBanner(perfil) {
    const semestre = Number(perfil.semestre) || 0;
    const totalSemestres = Number(perfil.totalSemestres) || 0;
    const avance = totalSemestres ? Math.min(100, Math.round((semestre / totalSemestres) * 100)) : 0;

    return `
        <section class="card maps-banner" aria-labelledby="maps-title">
            <div class="banner-content">
                <div>
                    <h1 id="maps-title">${escapeHtml(perfil.carrera || 'Mi trayectoria MAPS')}</h1>
                    <p>${perfil.certificado ? `Certificado activo: <strong>${escapeHtml(perfil.certificado)}</strong>` : 'Sin certificado activo'}${semestre ? ` · Semestre vigente: <strong>${semestre}.º</strong>` : ''}</p>
                </div>
                <span class="badge badge-materia">Plan MAPS oficial</span>
            </div>
            ${totalSemestres ? `<div class="progress-container"><div class="progress-labels"><span>Avance de carrera</span><span>Semestre ${semestre} de ${totalSemestres} · ${avance} %</span></div><div class="progress-bar" role="progressbar" aria-label="Avance de carrera" aria-valuemin="0" aria-valuemax="100" aria-valuenow="${avance}"><div class="progress-fill" style="width: ${avance}%"></div></div></div>` : ''}
        </section>
    `;
}

function renderQuickQuestion(materias) {
    const options = materias.map(materia => `<option value="${escapeHtml(materia.id ?? '')}">${escapeHtml(materia.nombre || materia)}</option>`).join('');
    return `
        <form class="card create-post" id="quick-question-form">
            <label class="visually-hidden" for="quick-question">Nueva duda académica</label>
            <input id="quick-question" name="question" type="text" maxlength="180" placeholder="¿Tienes una duda académica? Pregunta a tu comunidad..." required>
            <div class="create-post-actions"><select aria-label="Materia de la duda"><option value="">Seleccionar materia...</option>${options}</select><button class="btn-solid" type="submit">Publicar duda</button></div>
        </form>
    `;
}

function renderPost(post) {
    const author = post.autor || {};
    const initials = (author.iniciales || author.nombre || '?').trim().split(/\s+/).map(word => word[0]).slice(0, 2).join('').toUpperCase();
    const tags = [post.materia && `<span class="badge badge-materia">${escapeHtml(post.materia)}</span>`, post.solucionAceptada && '<span class="badge badge-solved">✓ Solución aceptada</span>', post.verificadoDocente && '<span class="badge badge-verified">Verificado por docente</span>'].filter(Boolean).join('');

    return `
        <article class="card post-card">
            <div class="post-header"><div class="user-block"><div class="avatar ${post.verificadoDocente ? 'prof' : ''}">${escapeHtml(initials)}</div><div class="user-data"><strong>${escapeHtml(author.nombre || 'Usuario')}</strong><span>${escapeHtml(post.fechaRelativa || '')}</span></div></div><div class="badges-wrap">${tags}</div></div>
            <h2 class="post-title">${escapeHtml(post.titulo || '')}</h2>
            ${post.contenido ? `<p class="post-body">${escapeHtml(post.contenido)}</p>` : ''}
            <div class="post-footer"><div class="action-links"><a class="btn-action" href="foro.html">💬 ${Number(post.respuestas) || 0} respuestas</a></div><span class="post-status">${post.solucionAceptada ? 'Resuelto' : ''}</span></div>
        </article>
    `;
}

function renderAccessCards(data) {
    const cards = [];
    const circulo = data.circuloActual;
    const asesoria = data.asesoria;
    const empresa = data.empresaDestacada;

    if (circulo) cards.push(`<section class="card side-card"><span class="badge badge-materia">${escapeHtml(circulo.fecha || 'Próximamente')}</span><h3>${escapeHtml(circulo.nombre || 'Círculo de estudio')}</h3>${circulo.descripcion ? `<p>${escapeHtml(circulo.descripcion)}</p>` : ''}<a class="btn-solid" href="circulos.html">Ver círculo</a></section>`);
    if (asesoria) cards.push(`<section class="card side-card prof-card"><span class="badge badge-verified">Asesoría disponible</span><h3>${escapeHtml(asesoria.docente || 'Docente')}</h3>${asesoria.horario ? `<p>${escapeHtml(asesoria.horario)}</p>` : ''}<a class="btn-clean" href="mensajes.html">Enviar mensaje privado</a></section>`);
    if (empresa) cards.push(`<section class="card side-card"><h3>Radar Semestre Empresarial</h3><p><strong>${escapeHtml(empresa.nombre || '')}${empresa.calificacion ? ` · ${escapeHtml(empresa.calificacion)} ★` : ''}</strong>${empresa.resena ? `<br>${escapeHtml(empresa.resena)}` : ''}</p><a class="side-link" href="empresarial.html">Ver directorio de empresas →</a></section>`);

    return cards.join('');
}

function renderEmptyFeed() {
    return '<section class="card empty-state"><h2>Aún no hay actividad</h2><p>Las dudas, avisos y recursos de tu comunidad aparecerán aquí cuando estén disponibles.</p><a class="side-link" href="foro.html">Ir al foro →</a></section>';
}

function escapeHtml(value) {
    return String(value ?? '').replace(/[&<>'"]/g, character => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', "'": '&#39;', '"': '&quot;' })[character]);
}
