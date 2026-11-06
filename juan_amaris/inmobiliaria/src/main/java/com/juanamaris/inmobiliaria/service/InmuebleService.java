package com.juanamaris.inmobiliaria.service;

import com.juanamaris.inmobiliaria.model.FiltroBusqueda;
import com.juanamaris.inmobiliaria.model.Inmueble;
import com.juanamaris.inmobiliaria.model.Usuario;
import com.juanamaris.inmobiliaria.repository.InmuebleRepository;
import com.juanamaris.inmobiliaria.repository.InmuebleSpecs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.List;

@Service
public class InmuebleService {

    public static final int POR_PAGINA = 12;

    private final InmuebleRepository repo;
    private final AlmacenFotosService fotos;
    private final UbicacionPrivadaService ubicacion;
    private final int maxFotos;

    public InmuebleService(InmuebleRepository repo, AlmacenFotosService fotos, UbicacionPrivadaService ubicacion,
                           @Value("${app.uploads.max-fotos}") int maxFotos) {
        this.repo = repo;
        this.ubicacion = ubicacion;
        this.fotos = fotos;
        this.maxFotos = maxFotos;
    }

    @Transactional(readOnly = true)
    public Page<Inmueble> buscar(FiltroBusqueda filtro, int pagina) {
        Sort orden = switch (filtro.getOrden() == null ? "" : filtro.getOrden()) {
            case "precio_asc" -> Sort.by("precio").ascending();
            case "precio_desc" -> Sort.by("precio").descending();
            case "cercania" -> Sort.unsorted(); // lo ordena InmuebleSpecs (sin minutos van al final)
            default -> Sort.by("fechaPublicacion").descending();
        };
        Page<Inmueble> page = repo.findAll(InmuebleSpecs.buscar(filtro),
                PageRequest.of(Math.max(pagina, 0), POR_PAGINA, orden));
        page.forEach(i -> i.getFotos().size()); // cargar foto principal para la tarjeta
        return page;
    }

    @Transactional(readOnly = true)
    public List<String> ciudades() { return repo.ciudadesConInmuebles(); }

    @Transactional(readOnly = true)
    public Inmueble detalle(Long id) {
        return repo.findDetalleById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inmueble no encontrado"));
    }

    @Transactional(readOnly = true)
    public Inmueble detallePublico(Long id) {
        Inmueble i = detalle(id);
        if (!i.isActivo()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Inmueble no disponible");
        return i;
    }

    @Transactional(readOnly = true)
    public List<Inmueble> deUsuario(Usuario u) {
        List<Inmueble> lista = repo.findByPropietarioOrderByFechaPublicacionDesc(u);
        lista.forEach(i -> i.getFotos().size());
        return lista;
    }

    @Transactional(readOnly = true)
    public Inmueble paraEditar(Long id, Usuario u) {
        Inmueble i = detalle(id);
        verificarDueno(i, u);
        return i;
    }

    @Transactional
    public Inmueble publicar(Inmueble datos, List<MultipartFile> nuevasFotos, Usuario u) {
        datos.setId(null);
        datos.setPropietario(u);
        if (datos.getUniversidadCercana() == null) datos.setMinutosAPie(null);
        ubicacion.asignarZona(datos);
        agregarFotos(datos, nuevasFotos);
        return repo.save(datos);
    }

    @Transactional
    public Inmueble actualizar(Long id, Inmueble datos, List<MultipartFile> nuevasFotos,
                               List<String> fotosABorrar, Usuario u) {
        Inmueble i = paraEditar(id, u);
        i.setTitulo(datos.getTitulo());
        i.setDescripcion(datos.getDescripcion());
        i.setTipo(datos.getTipo());
        i.setOperacion(datos.getOperacion());
        i.setPrecio(datos.getPrecio());
        i.setCiudad(datos.getCiudad());
        i.setBarrio(datos.getBarrio());
        i.setEstrato(datos.getEstrato());
        i.setAreaM2(datos.getAreaM2());
        i.setHabitaciones(datos.getHabitaciones());
        i.setBanos(datos.getBanos());
        i.setParqueaderos(datos.getParqueaderos());
        Double latAnterior = i.getLatitud(), lngAnterior = i.getLongitud();
        i.setLatitud(datos.getLatitud());
        i.setLongitud(datos.getLongitud());
        ubicacion.actualizarSiCambio(i, latAnterior, lngAnterior);
        i.setUniversidadCercana(datos.getUniversidadCercana());
        i.setMinutosAPie(datos.getUniversidadCercana() == null ? null : datos.getMinutosAPie());
        i.setAmoblado(datos.isAmoblado());
        i.setConvivencia(datos.getConvivencia());
        i.setPermiteMascotas(datos.isPermiteMascotas());
        i.setPermiteFumar(datos.isPermiteFumar());
        i.setPermiteVisitas(datos.isPermiteVisitas());
        i.setHorarioLlegada(datos.getHorarioLlegada());
        i.setOtrasReglas(datos.getOtrasReglas());
        i.setServiciosIncluidos(datos.isServiciosIncluidos());
        if (fotosABorrar != null) {
            for (String f : fotosABorrar) {
                if (i.getFotos().remove(f)) fotos.eliminar(f);
            }
        }
        agregarFotos(i, nuevasFotos);
        return i;
    }

    @Transactional
    public void cambiarEstado(Long id, Usuario u) {
        Inmueble i = paraEditar(id, u);
        i.setActivo(!i.isActivo());
    }

    @Transactional
    public void eliminar(Long id, Usuario u) {
        Inmueble i = paraEditar(id, u);
        i.getFotos().forEach(fotos::eliminar);
        repo.delete(i);
    }

    private void agregarFotos(Inmueble i, List<MultipartFile> nuevas) {
        if (nuevas == null) return;
        for (MultipartFile f : nuevas) {
            if (f == null || f.isEmpty()) continue;
            if (i.getFotos().size() >= maxFotos) {
                throw new IllegalArgumentException("Máximo " + maxFotos + " fotos por inmueble");
            }
            i.getFotos().add(fotos.guardar(f));
        }
    }

    private void verificarDueno(Inmueble i, Usuario u) {
        boolean admin = "ADMIN".equals(u.getRol());
        if (!admin && !i.getPropietario().getId().equals(u.getId())) {
            throw new AccessDeniedException("Este inmueble no es tuyo");
        }
    }
}
