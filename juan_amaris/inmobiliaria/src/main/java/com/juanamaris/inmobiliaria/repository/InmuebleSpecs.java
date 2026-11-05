package com.juanamaris.inmobiliaria.repository;

import com.juanamaris.inmobiliaria.model.FiltroBusqueda;
import com.juanamaris.inmobiliaria.model.Inmueble;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import java.util.ArrayList;
import java.util.List;

/** Construye la consulta de búsqueda a partir de los filtros que el usuario llenó. */
public final class InmuebleSpecs {

    private InmuebleSpecs() {}

    public static Specification<Inmueble> buscar(FiltroBusqueda f) {
        return (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            p.add(cb.isTrue(root.get("activo")));
            if (tieneTexto(f.getCiudad())) {
                p.add(cb.equal(cb.lower(root.get("ciudad")), f.getCiudad().trim().toLowerCase()));
            }
            if (tieneTexto(f.getTexto())) {
                String like = "%" + f.getTexto().trim().toLowerCase() + "%";
                p.add(cb.or(
                        cb.like(cb.lower(root.get("titulo")), like),
                        cb.like(cb.lower(root.get("barrio")), like),
                        cb.like(cb.lower(root.get("descripcion")), like)));
            }
            if (f.getOperacion() != null) p.add(cb.equal(root.get("operacion"), f.getOperacion()));
            if (f.getTipo() != null) p.add(cb.equal(root.get("tipo"), f.getTipo()));
            if (f.getPrecioMin() != null) p.add(cb.ge(root.get("precio"), f.getPrecioMin()));
            if (f.getPrecioMax() != null) p.add(cb.le(root.get("precio"), f.getPrecioMax()));
            if (f.getHabitacionesMin() != null) p.add(cb.ge(root.get("habitaciones"), f.getHabitacionesMin()));
            if (f.getEstrato() != null) p.add(cb.equal(root.get("estrato"), f.getEstrato()));
            if (f.getUniversidad() != null) {
                p.add(cb.equal(root.get("universidadCercana").get("id"), f.getUniversidad()));
            }
            if (f.getMaxMinutos() != null) p.add(cb.le(root.get("minutosAPie"), f.getMaxMinutos()));
            if (Boolean.TRUE.equals(f.getAmoblado())) p.add(cb.isTrue(root.get("amoblado")));
            if (Boolean.TRUE.equals(f.getServiciosIncluidos())) p.add(cb.isTrue(root.get("serviciosIncluidos")));
            boolean esConteo = Long.class.equals(query.getResultType());
            if ("cercania".equals(f.getOrden()) && !esConteo) {
                query.orderBy(cb.asc(cb.coalesce(root.<Integer>get("minutosAPie"), 9999)),
                        cb.desc(root.get("fechaPublicacion")));
            }
            return cb.and(p.toArray(Predicate[]::new));
        };
    }

    private static boolean tieneTexto(String s) { return s != null && !s.isBlank(); }
}
