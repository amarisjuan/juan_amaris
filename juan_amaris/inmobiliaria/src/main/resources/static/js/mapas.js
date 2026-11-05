/* Mapas con Leaflet + OpenStreetMap (gratis, sin clave de API). */
const Mapas = (() => {
    const COLOMBIA = [5.5, -74.3];
    const PASO_METROS_MIN = 80;   // velocidad promedio caminando
    const FACTOR_CALLES = 1.3;    // las calles no van en línea recta

    function crear(id, centro, zoom) {
        const mapa = L.map(id, { scrollWheelZoom: false }).setView(centro || COLOMBIA, zoom || 6);
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            maxZoom: 19,
            attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
        }).addTo(mapa);
        mapa.on('click', () => mapa.scrollWheelZoom.enable());
        // Si el contenedor cambia de tamaño (carga de la página, girar el celular), recalcular el mapa
        if (window.ResizeObserver) new ResizeObserver(() => mapa.invalidateSize()).observe(mapa.getContainer());
        window.addEventListener('load', () => mapa.invalidateSize());
        return mapa;
    }

    const icono = (emoji, clase) => L.divIcon({
        className: 'pin ' + clase, html: emoji, iconSize: [34, 34], iconAnchor: [17, 17], popupAnchor: [0, -14]
    });
    const ICONO_UNI = icono('🎓', 'pin-uni');
    const ICONO_CASA = icono('🏠', 'pin-casa');

    /** Construye el popup con textContent para que ningún título pueda inyectar HTML. */
    function popup(p) {
        const div = document.createElement('div');
        const a = document.createElement('a');
        a.href = p.url;
        a.textContent = p.titulo;
        a.className = 'popup-titulo';
        const d = document.createElement('div');
        d.textContent = p.detalle;
        div.append(a, d);
        return div;
    }

    function marcar(mapa, p, tipo, opciones) {
        return L.marker([p.lat, p.lng], Object.assign({ icon: tipo === 'uni' ? ICONO_UNI : ICONO_CASA }, opciones))
            .bindPopup(popup(p)).addTo(mapa);
    }

    function ajustar(mapa, marcadores, zoomUno) {
        if (!marcadores.length) return;
        if (marcadores.length === 1) mapa.setView(marcadores[0].getLatLng(), zoomUno || 15);
        else mapa.fitBounds(L.featureGroup(marcadores).getBounds(), { padding: [30, 30], maxZoom: 16 });
    }

    function metros(a, b) {
        const R = 6371000, rad = x => x * Math.PI / 180;
        const dLat = rad(b.lat - a.lat), dLng = rad(b.lng - a.lng);
        const h = Math.sin(dLat / 2) ** 2 + Math.cos(rad(a.lat)) * Math.cos(rad(b.lat)) * Math.sin(dLng / 2) ** 2;
        return 2 * R * Math.asin(Math.sqrt(h));
    }

    const minutosCaminando = (a, b) => Math.max(1, Math.round(metros(a, b) * FACTOR_CALLES / PASO_METROS_MIN));

    const textoDistancia = m => m < 1000 ? Math.round(m) + ' m' : (m / 1000).toFixed(1).replace('.', ',') + ' km';

    return { crear, marcar, ajustar, metros, minutosCaminando, textoDistancia };
})();
