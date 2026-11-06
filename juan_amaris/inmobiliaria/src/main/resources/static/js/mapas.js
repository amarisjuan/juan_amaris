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

    // Íconos de línea de la marca (mismos trazos que /img/iconos.svg)
    const SVG = {
        casa: '<path d="M3 10.5 12 3l9 7.5"/><path d="M5 9.5V21h14V9.5"/><path d="M10 21v-6h4v6"/>',
        gorro: '<path d="M2 9l10-5 10 5-10 5z"/><path d="M6 11v5c0 1.5 2.7 3 6 3s6-1.5 6-3v-5"/><path d="M22 9v6"/>'
    };
    const icono = (nombre, clase) => L.divIcon({
        className: 'pin ' + clase, iconSize: [34, 34], iconAnchor: [17, 17], popupAnchor: [0, -14],
        html: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" '
            + 'stroke-linejoin="round" aria-hidden="true">' + SVG[nombre] + '</svg>'
    });
    const ICONO_UNI = icono('gorro', 'pin-uni');
    // Pin pequeño para mapas con muchas universidades (página de estudiantes)
    const ICONO_UNI_PEQUENO = L.divIcon({
        className: 'pin pin-uni pin-pequeno', iconSize: [22, 22], iconAnchor: [11, 11], popupAnchor: [0, -10],
        html: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" '
            + 'stroke-linejoin="round" aria-hidden="true">' + SVG.gorro + '</svg>'
    });
    const ICONO_CASA = icono('casa', 'pin-casa');

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
        const icono = tipo === 'uni-pequeno' ? ICONO_UNI_PEQUENO : tipo === 'uni' ? ICONO_UNI : ICONO_CASA;
        return L.marker([p.lat, p.lng], Object.assign({ icon: icono }, opciones))
            .bindPopup(popup(p)).addTo(mapa);
    }

    /** Zona aproximada de un inmueble: círculo de p.radio metros (nunca el punto exacto). */
    function zona(mapa, p) {
        return L.circle([p.lat, p.lng], {
            radius: p.radio, color: '#0B4A6F', weight: 2, fillColor: '#0B4A6F', fillOpacity: 0.15
        }).bindPopup(popup(p)).addTo(mapa);
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

    return { crear, marcar, zona, ajustar, ICONO_UNI_PEQUENO, metros, minutosCaminando, textoDistancia, ICONO_CASA, ICONO_UNI };
})();
