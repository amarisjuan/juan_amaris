package com.juanamaris.inmobiliaria.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Map;
import java.util.UUID;

/** Guarda las fotos en disco con un nombre aleatorio para que nadie pueda sobrescribir archivos. */
@Service
public class AlmacenFotosService {

    private static final Map<String, String> EXTENSIONES = Map.of(
            "image/jpeg", ".jpg", "image/png", ".png", "image/webp", ".webp");

    private final Path carpeta;

    public AlmacenFotosService(@Value("${app.uploads.dir}") String dir) throws IOException {
        this.carpeta = Paths.get(dir).toAbsolutePath().normalize();
        Files.createDirectories(carpeta);
    }

    public Path getCarpeta() { return carpeta; }

    public String guardar(MultipartFile archivo) {
        String ext = EXTENSIONES.get(archivo.getContentType());
        if (ext == null) {
            throw new IllegalArgumentException("Solo se permiten fotos JPG, PNG o WEBP");
        }
        String nombre = UUID.randomUUID() + ext;
        try (InputStream in = archivo.getInputStream()) {
            Files.copy(in, carpeta.resolve(nombre));
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo guardar la foto", e);
        }
        return nombre;
    }

    public void eliminar(String nombre) {
        try {
            Path archivo = carpeta.resolve(nombre).normalize();
            if (archivo.startsWith(carpeta)) Files.deleteIfExists(archivo);
        } catch (IOException ignored) {
            // si no se puede borrar el archivo no bloqueamos al usuario
        }
    }
}
