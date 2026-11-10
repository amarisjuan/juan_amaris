/* Botón "Volver": si la persona llegó desde otra página del portal, regresa a esa misma página
   (conserva filtros, orden y página de resultados). Si entró directo (enlace compartido), usa el enlace normal. */
document.addEventListener('click', (e) => {
    const a = e.target.closest('a[data-volver]');
    if (!a || e.ctrlKey || e.metaKey || e.shiftKey || e.button !== 0) return;
    let previa = null;
    try { previa = document.referrer ? new URL(document.referrer) : null; } catch (_) { previa = null; }
    if (previa && previa.origin === location.origin && previa.href !== location.href && history.length > 1) {
        e.preventDefault();
        history.back();
    }
});
