/*
 * Selectores encadenados: primero la ciudad, después la universidad y el barrio de esa ciudad.
 * Los datos vienen del servidor (mapas ciudad -> universidades y ciudad -> barrios). Sin librerías.
 */
const Geo = (() => {
    const opcion = (valor, texto, seleccionada) => {
        const o = document.createElement('option');
        o.value = valor; o.textContent = texto; o.selected = !!seleccionada;
        return o;
    };

    function llenarUniversidades(sel, universidades, ciudad, textoVacio) {
        const actual = sel.value || sel.dataset.seleccionada || '';
        sel.innerHTML = '';
        if (!ciudad) {
            sel.append(opcion('', 'Primero elige la ciudad'));
            sel.disabled = true;
            return;
        }
        const lista = (universidades && universidades[ciudad]) || [];
        sel.append(opcion('', lista.length ? textoVacio : 'No hay universidades registradas en ' + ciudad));
        lista.forEach((u) => sel.append(opcion(u.id, u.nombre, String(u.id) === actual)));
        sel.disabled = lista.length === 0;
    }

    function llenarBarrios(lista, barrios, ciudad) {
        if (!lista) return;
        lista.innerHTML = '';
        ((barrios && barrios[ciudad]) || []).forEach((b) => lista.append(opcion(b, b)));
    }

    /**
     * cfg: { ciudad, universidad, barrio?, listaBarrios?, universidades, barrios, textoVacio }
     */
    function enlazar(cfg) {
        const ciudad = cfg.ciudad, uni = cfg.universidad;
        const cambiar = () => {
            llenarUniversidades(uni, cfg.universidades, ciudad.value, cfg.textoVacio || 'Cualquier universidad');
            llenarBarrios(cfg.listaBarrios, cfg.barrios, ciudad.value);
        };
        ciudad.addEventListener('change', cambiar);
        if (!ciudad.value) { uni.disabled = true; }
        cambiar();
        return { cambiar };
    }

    return { enlazar };
})();
