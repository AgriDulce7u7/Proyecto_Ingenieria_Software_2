-- =====================================================================
-- FUNCIONES DE CALENDARIO LABORAL COLOMBIANO - PROYECTO EPQ (SIGCA-EPQ)
-- Se ejecuta antes que los demás scripts (orden alfabético de docker-entrypoint).
--
-- Replica en SQL la misma lógica de la clase Java FestivosColombia /
-- CalendarioLaboralColombia, para que los datos semilla calculen los plazos
-- de PQR en DÍAS HÁBILES (lunes a viernes, sin festivos), igual que el backend:
--   * Festivos fijos.
--   * Festivos trasladables al lunes siguiente (Ley 51 de 1983 - Ley Emiliani).
--   * Festivos que dependen del Domingo de Pascua.
-- =====================================================================

-- Domingo de Pascua (algoritmo de Meeus/Jones/Butcher, calendario gregoriano)
CREATE OR REPLACE FUNCTION epq_domingo_pascua(p_anio INTEGER)
RETURNS DATE LANGUAGE plpgsql IMMUTABLE AS $$
DECLARE
    a INTEGER := p_anio % 19;
    b INTEGER := p_anio / 100;
    c INTEGER := p_anio % 100;
    d INTEGER; e INTEGER; f INTEGER; g INTEGER; h INTEGER;
    i INTEGER; k INTEGER; l INTEGER; m INTEGER;
BEGIN
    d := b / 4;
    e := b % 4;
    f := (b + 8) / 25;
    g := (b - f + 1) / 3;
    h := (19 * a + b - d - g + 15) % 30;
    i := c / 4;
    k := c % 4;
    l := (32 + 2 * e + 2 * i - h - k) % 7;
    m := (a + 11 * h + 22 * l) / 451;
    RETURN make_date(p_anio, (h + l - 7 * m + 114) / 31, ((h + l - 7 * m + 114) % 31) + 1);
END;
$$;

-- Lunes siguiente (o el mismo día si ya es lunes)
CREATE OR REPLACE FUNCTION epq_siguiente_lunes(p_fecha DATE)
RETURNS DATE LANGUAGE sql IMMUTABLE AS $$
    SELECT p_fecha + ((8 - EXTRACT(ISODOW FROM p_fecha)::INTEGER) % 7);
$$;

-- Festivos nacionales de un año
CREATE OR REPLACE FUNCTION epq_festivos(p_anio INTEGER)
RETURNS SETOF DATE LANGUAGE sql IMMUTABLE AS $$
    WITH pascua AS (SELECT epq_domingo_pascua(p_anio) AS p)
    -- Fijos
    SELECT make_date(p_anio, x.mes, x.dia)
    FROM (VALUES (1, 1), (5, 1), (7, 20), (8, 7), (12, 8), (12, 25)) AS x(mes, dia)
    UNION
    -- Trasladables al lunes (Ley Emiliani)
    SELECT epq_siguiente_lunes(make_date(p_anio, x.mes, x.dia))
    FROM (VALUES (1, 6), (3, 19), (6, 29), (8, 15), (10, 12), (11, 1), (11, 11)) AS x(mes, dia)
    UNION
    -- Dependientes de la Pascua
    SELECT p - 3 FROM pascua                              -- Jueves Santo
    UNION SELECT p - 2 FROM pascua                        -- Viernes Santo
    UNION SELECT epq_siguiente_lunes(p + 39) FROM pascua  -- Ascensión del Señor
    UNION SELECT epq_siguiente_lunes(p + 60) FROM pascua  -- Corpus Christi
    UNION SELECT epq_siguiente_lunes(p + 68) FROM pascua; -- Sagrado Corazón
$$;

-- ¿Es día hábil? (lunes a viernes y no festivo)
CREATE OR REPLACE FUNCTION epq_es_dia_habil(p_fecha DATE)
RETURNS BOOLEAN LANGUAGE sql IMMUTABLE AS $$
    SELECT EXTRACT(ISODOW FROM p_fecha) < 6
       AND NOT EXISTS (SELECT 1 FROM epq_festivos(EXTRACT(YEAR FROM p_fecha)::INTEGER) f WHERE f = p_fecha);
$$;

-- Suma días hábiles. El conteo inicia el día siguiente a p_fecha (términos legales).
CREATE OR REPLACE FUNCTION epq_sumar_dias_habiles(p_fecha DATE, p_dias INTEGER)
RETURNS DATE LANGUAGE plpgsql IMMUTABLE AS $$
DECLARE
    v_fecha DATE := p_fecha;
    v_contados INTEGER := 0;
BEGIN
    WHILE v_contados < p_dias LOOP
        v_fecha := v_fecha + 1;
        IF epq_es_dia_habil(v_fecha) THEN
            v_contados := v_contados + 1;
        END IF;
    END LOOP;
    RETURN v_fecha;
END;
$$;

-- Resta días hábiles (inverso de epq_sumar_dias_habiles). Útil para datos de demostración.
CREATE OR REPLACE FUNCTION epq_restar_dias_habiles(p_fecha DATE, p_dias INTEGER)
RETURNS DATE LANGUAGE plpgsql IMMUTABLE AS $$
DECLARE
    v_fecha DATE := p_fecha;
    v_contados INTEGER := 0;
BEGIN
    WHILE v_contados < p_dias LOOP
        v_fecha := v_fecha - 1;
        IF epq_es_dia_habil(v_fecha) THEN
            v_contados := v_contados + 1;
        END IF;
    END LOOP;
    RETURN v_fecha;
END;
$$;

-- Primer día hábil de un mes
CREATE OR REPLACE FUNCTION epq_primer_dia_habil(p_mes DATE)
RETURNS DATE LANGUAGE plpgsql IMMUTABLE AS $$
DECLARE
    v_fecha DATE := date_trunc('month', p_mes)::DATE;
BEGIN
    WHILE NOT epq_es_dia_habil(v_fecha) LOOP
        v_fecha := v_fecha + 1;
    END LOOP;
    RETURN v_fecha;
END;
$$;
