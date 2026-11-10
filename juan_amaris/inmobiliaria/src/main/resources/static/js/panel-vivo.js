/* Panel del administrador en vivo: cada 15 s vuelve a pedir la página y reemplaza las cifras,
   la tabla y las gráficas sin recargar. Se pausa si la pestaña no está a la vista. */
(() => {
    const INTERVALO = 15000;
    const caja = document.getElementById('panel-vivo');
    const texto = document.getElementById('texto-vivo');
    const hora = document.getElementById('hora-vivo');
    const estado = document.getElementById('estado-vivo');
    if (!caja) return;

    const ahora = () => new Date().toLocaleTimeString('es-CO', { hour: 'numeric', minute: '2-digit', second: '2-digit' });

    function graficas() {
        const datos = caja.querySelector('script.datos-graficas');
        if (!datos) return;
        const d = JSON.parse(datos.textContent);
        Graficas.columnas('g-visitas', d.visitas, 'visitas');
        Graficas.columnas('g-contactos', d.contactos, 'contactos');
    }

    const fmt = (n) => n.toLocaleString('es-CO');
    const numero = (t) => /^[\d.]+$/.test(t) ? Number(t.replace(/\./g, '')) : null;
    const quieto = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

    // La cifra sube (o baja) contando, en menos de un segundo.
    function contar(e, desde, hasta) {
        if (quieto) { e.textContent = fmt(hasta); return; }
        const inicio = performance.now(), dur = 700;
        const paso = (t) => {
            const k = Math.min(1, (t - inicio) / dur), suave = 1 - Math.pow(1 - k, 3);
            e.textContent = fmt(Math.round(desde + (hasta - desde) * suave));
            if (k < 1) requestAnimationFrame(paso);
        };
        e.textContent = fmt(desde);
        requestAnimationFrame(paso);
    }

    // Cifras actuales, para saber cuáles cambiaron.
    function cifras(raiz) { return [...raiz.querySelectorAll('.tile strong, .tabla td')].map((e) => e.textContent.trim()); }

    let ocupado = false, temporizador = null;
    async function actualizar() {
        if (ocupado || document.hidden) return;
        ocupado = true;
        try {
            const r = await fetch(location.pathname, { cache: 'no-store', credentials: 'same-origin' });
            if (r.redirected || !r.ok) throw new Error(r.redirected ? 'sesion' : 'error');
            const doc = new DOMParser().parseFromString(await r.text(), 'text/html');
            const nueva = doc.getElementById('panel-vivo');
            if (!nueva) throw new Error('sesion');
            const antes = cifras(caja);
            const abiertos = [...caja.querySelectorAll('details')].map((d) => d.open);
            caja.innerHTML = nueva.innerHTML;
            caja.querySelectorAll('details').forEach((d, i) => { d.open = !!abiertos[i]; });
            const despues = [...caja.querySelectorAll('.tile strong, .tabla td')];
            despues.forEach((e, i) => {
                const viejo = antes[i], actual = e.textContent.trim();
                if (viejo === undefined || viejo === actual) return;
                const a = numero(viejo), b = numero(actual);
                if (e.tagName === 'STRONG' && a !== null && b !== null) {
                    contar(e, a, b);
                    const d = document.createElement('span');
                    d.className = 'delta ' + (b > a ? 'sube' : 'baja');
                    d.textContent = (b > a ? '+' : '\u2212') + fmt(Math.abs(b - a));
                    d.title = 'Cambio desde la última actualización';
                    e.after(d);
                    setTimeout(() => d.classList.add('sale'), 5000);
                    setTimeout(() => d.remove(), 5600);
                } else {
                    e.classList.add('cambio');
                    setTimeout(() => e.classList.remove('cambio'), 1800);
                }
            });
            graficas();
            hora.textContent = ahora();
            texto.textContent = 'En vivo';
            estado.classList.remove('sin-conexion');
        } catch (e) {
            estado.classList.add('sin-conexion');
            if (e.message === 'sesion') {
                texto.textContent = 'Tu sesión se cerró. Vuelve a entrar para ver las cifras en vivo';
                clearInterval(temporizador);
            } else {
                texto.textContent = 'Sin conexión, reintentando';
            }
        } finally {
            ocupado = false;
        }
    }

    graficas();
    hora.textContent = ahora();
    temporizador = setInterval(actualizar, INTERVALO);
    document.addEventListener('visibilitychange', () => { if (!document.hidden) actualizar(); });
})();
