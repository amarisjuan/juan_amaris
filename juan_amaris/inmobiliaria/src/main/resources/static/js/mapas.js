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

    /** Precio corto para la etiqueta del mapa: $900 mil, $2,8 M. */
    function precioCorto(n) {
        if (n == null) return '';
        if (n >= 1e6) return '$' + (Math.round(n / 1e5) / 10).toString().replace('.', ',').replace(/,0$/, '') + ' M';
        return '$' + Math.round(n / 1000) + ' mil';
    }

    /** Tarjeta del inmueble: foto, título, precio y botón a la publicación (sin HTML del usuario). */
    function tarjeta(p) {
        const div = document.createElement('div');
        div.className = 'tarjeta-mapa';
        if (p.foto) {
            const img = document.createElement('img');
            img.src = p.foto; img.alt = ''; img.loading = 'lazy';
            div.append(img);
        }
        const t = document.createElement('strong'); t.textContent = p.titulo;
        const pr = document.createElement('span'); pr.className = 'tarjeta-mapa-precio'; pr.textContent = p.detalle;
        const z = document.createElement('small'); z.textContent = 'Zona aproximada';
        const a = document.createElement('a'); a.href = p.url; a.className = 'boton'; a.textContent = 'Ver publicación';
        div.append(t, pr, z, a);
        return div;
    }

    /** Inmueble en el mapa de resultados: zona aproximada + etiqueta con el precio que abre la tarjeta. */
    function inmueble(mapa, p) {
        const circulo = L.circle([p.lat, p.lng], { radius: p.radio, color: '#0B4A6F', weight: 1, fillColor: '#0B4A6F',
            fillOpacity: 0.10, interactive: false }).addTo(mapa);
        const etiqueta = document.createElement('span');
        etiqueta.textContent = precioCorto(p.precio);
        const icono = L.divIcon({ className: 'marcador-precio', html: etiqueta, iconSize: null, iconAnchor: [0, 0] });
        const marca = L.marker([p.lat, p.lng], { icon: icono, title: p.titulo, riseOnHover: true })
            .bindPopup(tarjeta(p), { minWidth: 220, maxWidth: 260 }).addTo(mapa);
        marca.zona = circulo; // para ocultar o mostrar la zona junto con la etiqueta
        return marca;
    }

    /** Botón "Mi ubicación": muestra un punto azul donde está la persona. La ubicación no sale del navegador. */
    // Ubicación en dos pasos: primero una posición rápida (red o la última conocida, casi instantánea)
    // y después se afina con el GPS sin volver a mover el mapa. Así el usuario no espera 10-20 s.
    function botonMiUbicacion(mapa) {
        let boton = null, punto = null, precision = null, vigilancia = null, corte = null, centrado = false;
        const Control = L.Control.extend({
            options: { position: 'topleft' },
            onAdd() {
                const b = L.DomUtil.create('button', 'mapa-boton-ubicacion');
                b.type = 'button';
                b.title = 'Mostrar mi ubicación';
                b.setAttribute('aria-label', 'Mostrar mi ubicación');
                b.innerHTML = '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" aria-hidden="true">'
                    + '<circle cx="12" cy="12" r="4"/><path d="M12 2v3M12 19v3M2 12h3M19 12h3"/></svg>';
                L.DomEvent.disableClickPropagation(b);
                L.DomEvent.on(b, 'click', buscar);
                boton = b;
                return b;
            }
        });
        new Control().addTo(mapa);

        function dibujar(pos) {
            const ll = L.latLng(pos.coords.latitude, pos.coords.longitude), acc = pos.coords.accuracy || 50;
            if (punto) { punto.setLatLng(ll); precision.setLatLng(ll).setRadius(acc); }
            else {
                precision = L.circle(ll, { radius: acc, color: '#1A73E8', weight: 1, fillColor: '#1A73E8',
                    fillOpacity: 0.12, interactive: false }).addTo(mapa);
                punto = L.circleMarker(ll, { radius: 8, color: '#FFFFFF', weight: 3, fillColor: '#1A73E8', fillOpacity: 1 })
                    .bindTooltip('Estás aquí').addTo(mapa);
            }
            if (!centrado) { mapa.setView(ll, Math.max(mapa.getZoom(), 15)); centrado = true; }
            boton.classList.remove('buscando');
            mapa.fire('locationfound', { latlng: ll, accuracy: acc });
        }

        function parar() {
            if (vigilancia !== null) navigator.geolocation.clearWatch(vigilancia);
            clearTimeout(corte); vigilancia = null;
        }

        function buscar() {
            if (!navigator.geolocation) { alert('Tu navegador no permite obtener la ubicación.'); return; }
            parar(); centrado = false;
            if (punto) { mapa.setView(punto.getLatLng(), Math.max(mapa.getZoom(), 15)); centrado = true; }
            boton.classList.add('buscando');
            // Paso 1: rápido (acepta una posición de hasta 5 minutos o la de la red).
            navigator.geolocation.getCurrentPosition(dibujar, () => {}, { enableHighAccuracy: false, maximumAge: 300000, timeout: 4000 });
            // Paso 2: precisa con GPS; se actualiza el punto azul y se apaga a los 20 s o al llegar a 30 m.
            vigilancia = navigator.geolocation.watchPosition((pos) => {
                dibujar(pos);
                if (pos.coords.accuracy <= 30) parar();
            }, (err) => {
                boton.classList.remove('buscando');
                if (!punto) alert(err.code === 1
                    ? 'Para mostrar tu ubicación, permite el acceso a la ubicación en el navegador.'
                    : 'No pudimos obtener tu ubicación. Inténtalo de nuevo en unos segundos.');
                parar();
            }, { enableHighAccuracy: true, maximumAge: 0, timeout: 20000 });
            corte = setTimeout(() => { parar(); boton.classList.remove('buscando'); }, 20000);
        }
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

    return { crear, marcar, zona, inmueble, botonMiUbicacion, ajustar, ICONO_UNI_PEQUENO, metros, minutosCaminando, textoDistancia, ICONO_CASA, ICONO_UNI };
})();
