package com.juanamaris.inmobiliaria.model;

/** Lo mínimo que necesita el mapa para dibujar un marcador. */
public record PuntoMapa(Long id, String titulo, String detalle, double lat, double lng, String url) {

    public static PuntoMapa de(Inmueble i) {
        String precio = String.format(java.util.Locale.GERMANY, "$%,d", i.getPrecio())
                + ("ARRIENDO".equals(i.getOperacion().name()) ? "/mes" : "");
        return new PuntoMapa(i.getId(), i.getTitulo(), precio, i.getLatitud(), i.getLongitud(), "/inmuebles/" + i.getId());
    }

    public static PuntoMapa de(Universidad u) {
        return new PuntoMapa(u.getId(), u.getNombre(), u.getCiudad(), u.getLatitud(), u.getLongitud(),
                "/?universidad=" + u.getId() + "&operacion=ARRIENDO&orden=cercania");
    }
}
