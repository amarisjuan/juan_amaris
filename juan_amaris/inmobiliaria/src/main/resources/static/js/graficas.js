/* Gráfica de columnas de una sola serie (SVG), con tooltip. Sin librerías externas. */
const Graficas = (() => {
    const NS = 'http://www.w3.org/2000/svg';
    const el = (tag, attrs) => { const e = document.createElementNS(NS, tag); for (const k in attrs) e.setAttribute(k, attrs[k]); return e; };

    // Escala con pasos redondos (1, 2, 5 x 10^n) y como máximo 5 divisiones: 0, 5, 10... nunca 6, 13, 19
    function escala(max) {
        if (max <= 0) return { paso: 1, divisiones: 4 };
        for (let p = Math.pow(10, Math.floor(Math.log10(max)) - 1); ; p *= 10) {
            for (const m of [1, 2, 5]) {
                const paso = m * p, divisiones = Math.ceil(max / paso);
                if (paso >= 1 && divisiones <= 5) return { paso, divisiones: Math.max(divisiones, 1) };
            }
        }
    }
    const fmt = (n) => n.toLocaleString('es-CO');

    function columnas(id, datos, unidad) {
        const caja = document.getElementById(id);
        const ancho = caja.clientWidth || 600, alto = 220;
        const m = { arriba: 16, derecha: 8, abajo: 26, izquierda: 40 };
        const w = ancho - m.izquierda - m.derecha, h = alto - m.arriba - m.abajo;
        const { paso, divisiones } = escala(Math.max(...datos.map((d) => d.valor), 0));
        const max = paso * divisiones;
        const svg = el('svg', { width: ancho, height: alto, role: 'img', 'aria-label': caja.dataset.titulo || '' });

        // Cuadrícula y eje Y
        for (let k = 0; k <= divisiones; k++) {
            const y = m.arriba + h - (h * k) / divisiones;
            svg.append(el('line', { x1: m.izquierda, x2: ancho - m.derecha, y1: y, y2: y, class: 'g-rejilla' }));
            const t = el('text', { x: m.izquierda - 6, y: y + 4, 'text-anchor': 'end', class: 'g-eje' });
            t.textContent = fmt(paso * k);
            svg.append(t);
        }

        const banda = w / datos.length;
        const barra = Math.min(20, Math.max(4, banda - 4));
        const tip = document.createElement('div');
        tip.className = 'g-tooltip';
        caja.append(tip);

        datos.forEach((d, i) => {
            const x = m.izquierda + i * banda + (banda - barra) / 2;
            const bh = max ? (h * d.valor) / max : 0;
            const y = m.arriba + h - bh;
            const r = Math.min(4, bh, barra / 2);
            if (bh > 0) {
                // Extremo superior redondeado de 4 px, base recta
                svg.append(el('path', { class: 'g-barra', d:
                    `M${x},${y + bh} V${y + r} Q${x},${y} ${x + r},${y} H${x + barra - r} Q${x + barra},${y} ${x + barra},${y + r} V${y + bh} Z` }));
            }
            // Zona de hover más grande que la barra
            const zona = el('rect', { x: m.izquierda + i * banda, y: m.arriba, width: banda, height: h, fill: 'transparent' });
            zona.addEventListener('mouseenter', () => {
                tip.textContent = `${d.etiqueta}: ${fmt(d.valor)} ${unidad}`;
                tip.style.left = `${m.izquierda + i * banda + banda / 2}px`;
                tip.style.top = `${Math.max(0, y - 34)}px`;
                tip.hidden = false;
            });
            zona.addEventListener('mouseleave', () => { tip.hidden = true; });
            svg.append(zona);
            // Etiquetas del eje X: una de cada 5 días y el último
            if (i % 5 === 0 || i === datos.length - 1) {
                const t = el('text', { x: x + barra / 2, y: alto - 8, 'text-anchor': 'middle', class: 'g-eje' });
                t.textContent = d.etiqueta;
                svg.append(t);
            }
        });
        tip.hidden = true;
        caja.prepend(svg);
    }
    return { columnas };
})();
