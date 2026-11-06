package com.juanamaris.inmobiliaria.service;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * Quita los metadatos de las fotos (EXIF, GPS, XMP, comentarios) sin volver a comprimir la imagen
 * (consulta legal 001: un celular guarda la ubicación exacta dentro de la foto).
 * JPEG: elimina segmentos APP1..APP15 y COM. PNG: elimina eXIf, tEXt, iTXt, zTXt y tIME. WEBP: elimina EXIF y XMP.
 */
public final class LimpiadorMetadatos {

    private LimpiadorMetadatos() {}

    public static byte[] limpiar(byte[] datos, String tipo) {
        return switch (tipo) {
            case "image/jpeg" -> jpeg(datos);
            case "image/png" -> png(datos);
            case "image/webp" -> webp(datos);
            default -> throw new IllegalArgumentException("Tipo de imagen no soportado: " + tipo);
        };
    }

    static byte[] jpeg(byte[] d) {
        if (d.length < 4 || (d[0] & 0xFF) != 0xFF || (d[1] & 0xFF) != 0xD8) throw invalida();
        ByteArrayOutputStream out = new ByteArrayOutputStream(d.length);
        out.write(0xFF);
        out.write(0xD8);
        int i = 2;
        while (i + 4 <= d.length) {
            if ((d[i] & 0xFF) != 0xFF) throw invalida();
            int marcador = d[i + 1] & 0xFF;
            if (marcador == 0xDA) {                     // inicio de los datos de imagen: copiar el resto tal cual
                out.write(d, i, d.length - i);
                return out.toByteArray();
            }
            int largo = ((d[i + 2] & 0xFF) << 8) | (d[i + 3] & 0xFF);
            if (largo < 2 || i + 2 + largo > d.length) throw invalida();
            boolean metadato = (marcador >= 0xE1 && marcador <= 0xEF) || marcador == 0xFE; // APP1-15, COM
            if (!metadato) out.write(d, i, 2 + largo);  // APP0 (JFIF) y segmentos de imagen se conservan
            i += 2 + largo;
        }
        throw invalida();
    }

    private static final Set<String> PNG_QUITAR = Set.of("eXIf", "tEXt", "iTXt", "zTXt", "tIME");

    static byte[] png(byte[] d) {
        if (d.length < 8 || (d[0] & 0xFF) != 0x89 || d[1] != 'P' || d[2] != 'N' || d[3] != 'G') throw invalida();
        ByteArrayOutputStream out = new ByteArrayOutputStream(d.length);
        out.write(d, 0, 8);
        int i = 8;
        while (i + 12 <= d.length) {
            int largo = ByteBuffer.wrap(d, i, 4).getInt();
            if (largo < 0 || i + 12 + largo > d.length) throw invalida();
            String tipo = new String(d, i + 4, 4, StandardCharsets.US_ASCII);
            if (!PNG_QUITAR.contains(tipo)) out.write(d, i, 12 + largo);
            i += 12 + largo;
            if ("IEND".equals(tipo)) return out.toByteArray();
        }
        throw invalida();
    }

    static byte[] webp(byte[] d) {
        if (d.length < 12 || !"RIFF".equals(new String(d, 0, 4, StandardCharsets.US_ASCII))
                || !"WEBP".equals(new String(d, 8, 4, StandardCharsets.US_ASCII))) throw invalida();
        ByteArrayOutputStream cuerpo = new ByteArrayOutputStream(d.length);
        cuerpo.write(d, 8, 4); // "WEBP"
        int i = 12;
        while (i + 8 <= d.length) {
            String tipo = new String(d, i, 4, StandardCharsets.US_ASCII);
            int largo = ByteBuffer.wrap(d, i + 4, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
            int total = 8 + largo + (largo % 2);
            if (largo < 0 || i + 8 + largo > d.length) throw invalida();
            if ("VP8X".equals(tipo)) {
                byte[] trozo = java.util.Arrays.copyOfRange(d, i, i + Math.min(total, d.length - i));
                trozo[8] &= (byte) ~0x0C; // apagar las banderas de EXIF (0x08) y XMP (0x04)
                cuerpo.write(trozo, 0, trozo.length);
            } else if (!"EXIF".equals(tipo) && !"XMP ".equals(tipo)) {
                cuerpo.write(d, i, Math.min(total, d.length - i));
            }
            i += total;
        }
        byte[] c = cuerpo.toByteArray();
        ByteBuffer out = ByteBuffer.allocate(8 + c.length).order(ByteOrder.LITTLE_ENDIAN);
        out.put("RIFF".getBytes(StandardCharsets.US_ASCII)).putInt(c.length).put(c);
        return out.array();
    }

    private static IllegalArgumentException invalida() {
        return new IllegalArgumentException("La foto está dañada o no es una imagen válida");
    }
}
