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

    // Resalta un instante las cifras que cambiaron.
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
                if (antes[i] !== undefined && antes[i] !== e.textContent.trim()) {
                    e.classList.add('cambio');
                    setTimeout(() => e.classList.remove('cambio'), 2500);
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
