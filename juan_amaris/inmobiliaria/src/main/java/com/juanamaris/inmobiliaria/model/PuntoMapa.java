package com.juanamaris.inmobiliaria.model;

/**
 * Lo mínimo que necesita el mapa. Para inmuebles usa SOLO la zona pública aproximada:
 * las coordenadas exactas nunca deben llegar al navegador (consulta legal 001).
 * radio > 0 indica una zona aproximada (círculo) en lugar de un punto.
 */
public record PuntoMapa(Long id, String titulo, String detalle, double lat, double lng, String url, int radio,
                        Long precio, String foto) {

    public static PuntoMapa zona(Inmueble i, int radio) {
        if (!i.isZonaPublicaLista()) throw new IllegalStateException("Inmueble sin zona pública");
        String precio = String.format(java.util.Locale.GERMANY, "$%,d", i.getPrecio())
                + ("ARRIENDO".equals(i.getOperacion().name()) ? "/mes" : "");
        String foto = i.getFotoPrincipal() == null ? null : "/fotos/" + i.getFotoPrincipal();
        return new PuntoMapa(i.getId(), i.getTitulo(), precio, i.getLatitudPublica(), i.getLongitudPublica(),
                "/inmuebles/" + i.getId(), radio, i.getPrecio(), foto);
    }

    public static PuntoMapa de(Universidad u) {
        String lugar = u.getCiudad() + (u.getDepartamento() != null ? ", " + u.getDepartamento() : "");
        return new PuntoMapa(u.getId(), u.getNombre(), lugar, u.getLatitud(), u.getLongitud(),
                "/?universidad=" + u.getId() + "&operacion=ARRIENDO&orden=cercania", 0, null, null);
    }
}
