package com.juanamaris.inmobiliaria.repository;

import com.juanamaris.inmobiliaria.model.Companero;
import com.juanamaris.inmobiliaria.model.FiltroCompanero;
import com.juanamaris.inmobiliaria.model.Usuario;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.ArrayList;
import java.util.List;

public interface CompaneroRepository extends JpaRepository<Companero, Long>, JpaSpecificationExecutor<Companero> {

    List<Companero> findByAutorOrderByFechaPublicacionDesc(Usuario autor);

    long countByActivoTrue();

    static Specification<Companero> buscar(FiltroCompanero f) {
        return (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            p.add(cb.isTrue(root.get("activo")));
            if (f.getCiudad() != null && !f.getCiudad().isBlank()) {
                p.add(cb.equal(cb.lower(root.get("universidad").get("ciudad")), f.getCiudad().trim().toLowerCase()));
            }
            if (f.getUniversidad() != null) p.add(cb.equal(root.get("universidad").get("id"), f.getUniversidad()));
            if (f.getTipo() != null) p.add(cb.equal(root.get("tipo"), f.getTipo()));
            if (f.getSoy() != null) {
                p.add(cb.or(cb.isNull(root.get("buscoGenero")), cb.equal(root.get("buscoGenero"), f.getSoy())));
            }
            if (f.getPresupuestoMax() != null) p.add(cb.le(root.get("presupuesto"), f.getPresupuestoMax()));
            return cb.and(p.toArray(Predicate[]::new));
        };
    }
}
