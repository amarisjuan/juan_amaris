package com.juanamaris.inmobiliaria.service;

import com.juanamaris.inmobiliaria.model.Inmueble;
import com.juanamaris.inmobiliaria.model.Universidad;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.security.SecureRandom;
import java.util.Objects;

/**
 * Protege la ubicación exacta de los inmuebles (decisión del 2026-11-06, consulta legal 001).
 * Al público solo se muestra un círculo de {@code radio} metros cuyo centro está desplazado
 * entre {@code min} y {@code max} metros del punto real, en una dirección aleatoria.
 * El desplazamiento se guarda: recalcularlo en cada visita permitiría promediar y encontrar el punto.
 */
@Service
public class UbicacionPrivadaService {

    private static final double METROS_POR_GRADO = 111_320d;
    private final SecureRandom azar = new SecureRandom();
    private final int radio, min, max;

    public UbicacionPrivadaService(@Value("${app.privacidad.radio-zona-m}") int radio,
                                   @Value("${app.privacidad.desplazamiento-min-m}") int min,
                                   @Value("${app.privacidad.desplazamiento-max-m}") int max) {
        if (!(0 < min && min < max && max < radio)) {
            throw new IllegalArgumentException("Debe cumplirse 0 < desplazamiento-min < desplazamiento-max < radio-zona");
        }
        this.radio = radio;
        this.min = min;
        this.max = max;
    }

    public int getRadio() { return radio; }

    /** Calcula y asigna una zona pública nueva. Si no hay ubicación exacta, la borra. */
    public void asignarZona(Inmueble i) {
        if (!i.isUbicado()) {
            i.setZonaPublica(null, null);
            return;
        }
        double distancia = min + azar.nextDouble() * (max - min);
        double angulo = azar.nextDouble() * 2 * Math.PI;
        double dLat = distancia * Math.cos(angulo) / METROS_POR_GRADO;
        double dLng = distancia * Math.sin(angulo) / (METROS_POR_GRADO * Math.cos(Math.toRadians(i.getLatitud())));
        // 5 decimales (~1 m): suficiente para el mapa sin aparentar precisión
        i.setZonaPublica(redondear(i.getLatitud() + dLat), redondear(i.getLongitud() + dLng));
    }

    /**
     * true si la zona guardada sigue siendo válida con la configuración actual: el punto real queda dentro
     * del círculo y no en su centro. Si se reduce el radio, las zonas antiguas pueden dejar de serlo.
     */
    public boolean zonaValida(Inmueble i) {
        if (!i.isUbicado() || !i.isZonaPublicaLista()) return false;
        double d = metros(i.getLatitud(), i.getLongitud(), i.getLatitudPublica(), i.getLongitudPublica());
        return d >= min * 0.9 && d <= max * 1.05;
    }

    /** Recalcula la zona solo si la ubicación exacta cambió (o si falta). */
    public void actualizarSiCambio(Inmueble i, Double latAnterior, Double lngAnterior) {
        boolean cambio = !Objects.equals(latAnterior, i.getLatitud()) || !Objects.equals(lngAnterior, i.getLongitud());
        if (cambio || (i.isUbicado() && !i.isZonaPublicaLista())) asignarZona(i);
    }

    /** Distancia real (con la ubicación exacta) redondeada a 100 m, como texto. Nulo si falta algún punto. */
    public String distanciaAproximada(Inmueble i, Universidad u) {
        if (!i.isUbicado() || u == null || !u.isUbicada()) return null;
        double m = metros(i.getLatitud(), i.getLongitud(), u.getLatitud(), u.getLongitud());
        long redondeada = Math.max(100, Math.round(m / 100) * 100);
        return redondeada < 1000 ? "unos " + redondeada + " m"
                : "unos " + String.format(java.util.Locale.GERMANY, "%.1f", redondeada / 1000.0) + " km";
    }

    static double metros(double lat1, double lng1, double lat2, double lng2) {
        double r = 6_371_000, dLat = Math.toRadians(lat2 - lat1), dLng = Math.toRadians(lng2 - lng1);
        double h = Math.pow(Math.sin(dLat / 2), 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.pow(Math.sin(dLng / 2), 2);
        return 2 * r * Math.asin(Math.sqrt(h));
    }

    private static double redondear(double v) { return Math.round(v * 100_000d) / 100_000d; }
}
