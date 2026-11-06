package com.juanamaris.inmobiliaria.service;

import java.text.Normalizer;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Detecta direcciones exactas escritas en textos públicos (consulta legal 001).
 * Busca nomenclatura colombiana con número de placa: "Calle 45 # 23-10", "Cra 54 No. 68-80", "# 23-10",
 * o unidades con número ("apto 302", "torre 2", "interior 4"). Una referencia sin placa
 * ("a dos cuadras de la Calle 26") sí se permite.
 */
public final class DetectorDirecciones {

    private static final String VIA = "(calle|cll|cl|carrera|cra|kra|kr|cr|avenida|av|diagonal|dg|transversal|tv|tr|circular|cir)";
    private static final String NUM = "\\d{1,3}\\s*[a-z]?(\\s*bis)?";
    private static final List<Pattern> PATRONES = List.of(
            // Vía + número + (#|No.|N°|Nro) + número - número
            Pattern.compile("\\b" + VIA + "\\.?\\s*" + NUM + "\\s*(#|no\\.?|n°|nro\\.?|numero)\\s*" + NUM + "\\s*-\\s*\\d{1,3}"),
            // Solo la placa: "# 23-10"
            Pattern.compile("#\\s*\\d{1,3}\\s*[a-z]?\\s*-\\s*\\d{1,3}\\b"),
            // Unidad con número
            Pattern.compile("\\b(apto|apartamento|apt|torre|interior|int|bloque|manzana|mz)\\.?\\s*\\d{1,4}\\b"));

    private DetectorDirecciones() {}

    /** Devuelve el fragmento que parece una dirección, si lo hay. */
    public static Optional<String> buscar(String texto) {
        if (texto == null || texto.isBlank()) return Optional.empty();
        String t = Normalizer.normalize(texto.toLowerCase(), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        for (Pattern p : PATRONES) {
            Matcher m = p.matcher(t);
            if (m.find()) return Optional.of(m.group());
        }
        return Optional.empty();
    }

    public static String mensaje(String fragmento) {
        return "Por seguridad no escribas la dirección exacta (\"" + fragmento + "\"). "
                + "El portal muestra solo el barrio y una zona aproximada; la dirección se la das a quien te contacte.";
    }
}
