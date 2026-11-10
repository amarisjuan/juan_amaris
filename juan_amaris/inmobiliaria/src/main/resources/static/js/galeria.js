/* Galería del detalle: abre un visor a pantalla completa con flechas, teclado y deslizamiento en celular. */
(() => {
    const galeria = document.querySelector('.galeria[data-total]');
    const visor = document.querySelector('.visor');
    if (!galeria || !visor || typeof visor.showModal !== 'function') return;
    const fotos = [...galeria.querySelector('template.galeria-lista').content.querySelectorAll('img')].map((i) => i.getAttribute('src'));
    const img = visor.querySelector('.visor-img'), cuenta = visor.querySelector('.visor-cuenta');
    const ant = visor.querySelector('.visor-ant'), sig = visor.querySelector('.visor-sig');
    let actual = 0;

    function mostrar(k) {
        actual = (k + fotos.length) % fotos.length;
        img.src = fotos[actual];
        img.alt = 'Foto ' + (actual + 1) + ' de ' + fotos.length;
        cuenta.textContent = (actual + 1) + ' / ' + fotos.length;
        // Precarga la siguiente para que pase sin espera.
        new Image().src = fotos[(actual + 1) % fotos.length];
    }
    const unica = fotos.length < 2;
    ant.hidden = sig.hidden = unica;

    galeria.addEventListener('click', (e) => {
        const b = e.target.closest('[data-indice]');
        if (!b) return;
        mostrar(Number(b.dataset.indice));
        visor.showModal();
    });
    ant.addEventListener('click', () => mostrar(actual - 1));
    sig.addEventListener('click', () => mostrar(actual + 1));
    visor.querySelector('.visor-cerrar').addEventListener('click', () => visor.close());
    visor.addEventListener('click', (e) => { if (e.target === visor) visor.close(); });
    document.addEventListener('keydown', (e) => {
        if (!visor.open || unica) return;
        if (e.key === 'ArrowLeft') mostrar(actual - 1);
        if (e.key === 'ArrowRight') mostrar(actual + 1);
    });
    let x0 = null;
    visor.addEventListener('touchstart', (e) => { x0 = e.touches[0].clientX; }, { passive: true });
    visor.addEventListener('touchend', (e) => {
        if (x0 === null || unica) return;
        const dx = e.changedTouches[0].clientX - x0;
        if (Math.abs(dx) > 50) mostrar(actual + (dx < 0 ? 1 : -1));
        x0 = null;
    });
})();
