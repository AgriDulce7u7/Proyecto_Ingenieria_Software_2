-- =====================================================================
-- DATOS SEMILLA - ENTREGA 2 (SIGCA-EPQ)
-- Se ejecuta después de 03_schema_entrega2.sql. Todas las fechas son relativas
-- a la fecha de creación de la base de datos para que la demostración funcione
-- en cualquier momento.
--
-- Credenciales de demostración (ver backend/README.md):
--   Funcionarios: usuario = admin | asesor | lgiraldo | aocampo | mcastano | phenao | cartera | gerente | directivo
--                 contraseña = Epq2026*
--   Ciudadanos (portal): número de documento del cliente, contraseña = Ciudadano2026*
-- =====================================================================

CREATE EXTENSION IF NOT EXISTS pgcrypto;   -- crypt()/gen_salt('bf'): hashes BCrypt compatibles con Spring Security

-- ---------------------------------------------------------------------
-- Municipios atendidos por EPQ (CO-03)
-- ---------------------------------------------------------------------
INSERT INTO municipio (nombre, codigo_dane) VALUES
    ('Buenavista', '63111'), ('Circasia', '63190'), ('Córdoba', '63212'), ('Filandia', '63272'),
    ('Génova', '63302'), ('La Tebaida', '63401'), ('Montenegro', '63470'), ('Pijao', '63548'),
    ('Quimbaya', '63594'), ('Salento', '63690');

-- ---------------------------------------------------------------------
-- Oficina de PQR: jefe y gestores por municipio (SyR-02b, SWR-11)
-- ---------------------------------------------------------------------
INSERT INTO gestor (nombre, correo, area, activo, es_jefe)
VALUES ('Patricia Henao Mejía', 'phenao@epq.com.co', 'Oficina de PQR - Jefatura', TRUE, TRUE);

UPDATE gestor g SET municipio_id = m.id, jefe_id = (SELECT id FROM gestor WHERE es_jefe)
FROM (VALUES ('lgiraldo@epq.com.co', 'Montenegro'), ('aocampo@epq.com.co', 'Salento'),
             ('mcastano@epq.com.co', 'Filandia'), ('jarango@epq.com.co', 'Quimbaya')) AS x(correo, municipio)
JOIN municipio m ON m.nombre = x.municipio
WHERE g.correo = x.correo;

-- ---------------------------------------------------------------------
-- RF-14: cuentas de funcionarios (contraseña de demostración: Epq2026*)
-- ---------------------------------------------------------------------
INSERT INTO funcionario (usuario, nombre_completo, correo, password_hash, rol, municipio_id, gestor_id)
SELECT x.usuario, x.nombre, x.correo, crypt('Epq2026*', gen_salt('bf', 10)), x.rol,
       (SELECT id FROM municipio WHERE nombre = x.municipio),
       (SELECT id FROM gestor WHERE correo = x.correo)
FROM (VALUES
    ('admin',     'Administrador del Sistema',   'admin@epq.com.co',     'ADMINISTRADOR',    NULL),
    ('asesor',    'Daniela Torres Arias',        'dtorres@epq.com.co',   'ASESOR_COMERCIAL', 'Montenegro'),
    ('lgiraldo',  'Laura Giraldo Ríos',          'lgiraldo@epq.com.co',  'GESTOR_PQR',       'Montenegro'),
    ('aocampo',   'Andrés Ocampo Salazar',       'aocampo@epq.com.co',   'GESTOR_PQR',       'Salento'),
    ('mcastano',  'Marcela Castaño Pérez',       'mcastano@epq.com.co',  'GESTOR_PQR',       'Filandia'),
    ('phenao',    'Patricia Henao Mejía',        'phenao@epq.com.co',    'JEFE_PQR',         NULL),
    ('cartera',   'Julián Ramírez Botero',       'jramirez@epq.com.co',  'GESTOR_CARTERA',   NULL),
    ('gerente',   'Gerente General EPQ',         'gerencia@epq.com.co',  'GERENTE',          NULL),
    ('directivo', 'Subgerente Comercial',        'subcomercial@epq.com.co', 'DIRECTIVO',     NULL)
) AS x(usuario, nombre, correo, rol, municipio);

-- ---------------------------------------------------------------------
-- F-03: completar los datos de los usuarios del servicio existentes
-- (contraseña del portal de autogestión: Ciudadano2026*)
-- ---------------------------------------------------------------------
UPDATE cliente c
SET codigo_interno         = 'USR-' || lpad(c.id::TEXT, 6, '0'),
    tipo_persona           = CASE WHEN c.tipo_documento = 'NIT' THEN 'JURIDICA' ELSE 'NATURAL' END,
    municipio_id           = (SELECT id FROM municipio WHERE nombre = x.municipio),
    direccion_notificacion = x.direccion,
    estado_validacion      = 'validado',
    detalle_validacion     = 'Identificación verificada contra la RNEC (carga inicial)',
    fecha_validacion       = NOW() - INTERVAL '400 days',
    fecha_registro         = NOW() - INTERVAL '400 days',
    registrado_por         = 'migracion',
    password_hash          = crypt('Ciudadano2026*', gen_salt('bf', 10))
FROM (VALUES ('1094950001', 'Montenegro', 'Cra 14 # 20-35, Montenegro'),
             ('41912345',   'Salento',    'Calle 5 # 3-12, Salento'),
             ('7548123',    'Circasia',   'Calle 10 # 8-40, Circasia'),
             ('900123456',  'Filandia',   'Parque principal, Filandia'),
             ('1097034567', 'La Tebaida', 'Mz 4 Casa 12, La Tebaida')) AS x(documento, municipio, direccion)
WHERE c.numero_documento = x.documento;

-- Usuario registrado con validación RNEC pendiente (curso alterno CU-01 4b)
INSERT INTO cliente (tipo_documento, numero_documento, nombre_completo, correo, telefono, codigo_interno, tipo_persona,
                     municipio_id, direccion_notificacion, estado_validacion, detalle_validacion, registrado_por)
VALUES ('CC', '1098765432', 'Sebastián Morales Cardona', 'smorales@correo.com', '3125550101', 'USR-000006', 'NATURAL',
        (SELECT id FROM municipio WHERE nombre = 'Quimbaya'), 'Cra 6 # 12-20, Quimbaya', 'pendiente',
        'Servicio de la RNEC no disponible al momento del registro', 'asesor');

-- La dirección de entrega 1 decía "Armenia", municipio que no atiende EPQ: se corrige a Montenegro
UPDATE contrato SET direccion_servicio = 'Cra 14 # 20-35, Montenegro' WHERE direccion_servicio LIKE '%Armenia';
UPDATE ciudadano SET direccion = 'Cra 14 # 20-35, Montenegro' WHERE direccion LIKE '%Armenia';

-- ---------------------------------------------------------------------
-- Predios (dirección, municipio, estrato y uso)
-- ---------------------------------------------------------------------
INSERT INTO predio (codigo_predio, direccion, municipio_id, estrato, uso, cliente_id, fecha_registro)
SELECT 'PRD-' || lpad(x.n::TEXT, 6, '0'), x.direccion, m.id, x.estrato, x.uso,
       (SELECT id FROM cliente WHERE numero_documento = x.documento), NOW() - INTERVAL '400 days'
FROM (VALUES (1, 'Cra 14 # 20-35, Montenegro',   'Montenegro', 3, 'RESIDENCIAL', '1094950001'),
             (2, 'Calle 5 # 3-12, Salento',      'Salento',    2, 'RESIDENCIAL', '41912345'),
             (3, 'Calle 10 # 8-40, Circasia',    'Circasia',   1, 'RESIDENCIAL', '7548123'),
             (4, 'Parque principal, Filandia',   'Filandia',   4, 'COMERCIAL',   '900123456'),
             (5, 'Mz 4 Casa 12, La Tebaida',     'La Tebaida', 2, 'RESIDENCIAL', '1097034567'),
             (6, 'Finca La Esperanza, Quimbaya', 'Quimbaya',   1, 'RESIDENCIAL', '1097034567'),
             (7, 'Cra 6 # 12-20, Quimbaya',      'Quimbaya',   3, 'RESIDENCIAL', '1098765432'))
     AS x(n, direccion, municipio, estrato, uso, documento)
JOIN municipio m ON m.nombre = x.municipio;

UPDATE contrato c SET predio_id = p.id, registrado_por = 'migracion', fecha_registro = c.fecha_inicio
FROM predio p WHERE p.direccion = c.direccion_servicio;

-- ---------------------------------------------------------------------
-- Tarifas generales de años anteriores (necesarias para el histórico de 24 meses)
INSERT INTO tarifa (servicio_id, nombre, valor_por_unidad, cargo_fijo, fecha_vigencia_inicio, fecha_vigencia_fin)
SELECT s.id, x.nombre, x.valor, x.cargo, x.inicio::DATE, x.fin::DATE
FROM (VALUES ('Acueducto',      'Acueducto residencial 2024',      2790.00, 11050.00, '2024-01-01', '2024-12-31'),
             ('Alcantarillado', 'Alcantarillado residencial 2024', 2490.00,  8540.00, '2024-01-01', '2024-12-31'),
             ('Alcantarillado', 'Alcantarillado residencial 2025', 2660.00,  9130.00, '2025-01-01', '2025-12-31'),
             ('Gas',            'Gas natural residencial 2024',    2105.00,  3940.00, '2024-01-01', '2024-12-31'),
             ('Gas',            'Gas natural residencial 2025',    2250.00,  4210.00, '2025-01-01', '2025-12-31'))
     AS x(servicio, nombre, valor, cargo, inicio, fin)
JOIN servicio s ON s.nombre = x.servicio;

-- ---------------------------------------------------------------------
-- Tarifas 2026 por estrato (y una específica por municipio) - RN-08 / SWR-18
-- Las tarifas generales de la entrega 1 (sin estrato) quedan como respaldo.
-- ---------------------------------------------------------------------
INSERT INTO tarifa (servicio_id, nombre, valor_por_unidad, cargo_fijo, fecha_vigencia_inicio, fecha_vigencia_fin, estrato, municipio_id)
SELECT s.id, x.nombre, x.valor, x.cargo, DATE '2026-01-01', NULL, x.estrato,
       (SELECT id FROM municipio WHERE nombre = x.municipio)
FROM (VALUES
    ('Acueducto',      'Acueducto residencial estrato 1 - 2026',      2250.00,  8860.00, 1, NULL),
    ('Acueducto',      'Acueducto residencial estrato 2 - 2026',      2730.00, 10750.00, 2, NULL),
    ('Acueducto',      'Acueducto residencial estrato 3 - 2026',      3050.00, 12020.00, 3, NULL),
    ('Acueducto',      'Acueducto estrato 5 - 2026',                  3860.00, 15180.00, 5, NULL),
    ('Acueducto',      'Acueducto estrato 6 - 2026',                  4180.00, 16450.00, 6, NULL),
    ('Acueducto',      'Acueducto Salento estrato 2 - 2026',          2810.00, 10980.00, 2, 'Salento'),
    ('Alcantarillado', 'Alcantarillado residencial estrato 1 - 2026', 2010.00,  6890.00, 1, NULL),
    ('Alcantarillado', 'Alcantarillado residencial estrato 2 - 2026', 2440.00,  8360.00, 2, NULL),
    ('Alcantarillado', 'Alcantarillado residencial estrato 3 - 2026', 2725.00,  9350.00, 3, NULL),
    ('Gas',            'Gas natural estratos 1 y 2 - 2026 (estr. 1)', 1930.00,  3610.00, 1, NULL),
    ('Gas',            'Gas natural estratos 1 y 2 - 2026 (estr. 2)', 2050.00,  3840.00, 2, NULL)
) AS x(servicio, nombre, valor, cargo, estrato, municipio)
JOIN servicio s ON s.nombre = x.servicio;

-- Tarifa asignada a cada contrato: la vigente más específica para su servicio, municipio y estrato
UPDATE contrato c SET tarifa_id = (
    SELECT t.id FROM tarifa t JOIN predio p ON p.id = c.predio_id
    WHERE t.servicio_id = c.servicio_id
      AND t.fecha_vigencia_inicio <= CURRENT_DATE
      AND (t.fecha_vigencia_fin IS NULL OR t.fecha_vigencia_fin >= CURRENT_DATE)
      AND (t.municipio_id IS NULL OR t.municipio_id = p.municipio_id)
      AND (t.estrato IS NULL OR t.estrato = p.estrato)
    ORDER BY (t.municipio_id IS NOT NULL)::INT * 2 + (t.estrato IS NOT NULL)::INT DESC,   -- especificidad
             t.fecha_vigencia_inicio DESC
    LIMIT 1);

-- Historial inicial de estados de los contratos (SWR-20)
INSERT INTO historial_contrato (contrato_id, estado_anterior, estado_nuevo, motivo, usuario, fecha)
SELECT c.id, NULL, 'activo', 'Contrato migrado desde el sistema anterior', 'migracion', c.fecha_inicio
FROM contrato c;

INSERT INTO historial_contrato (contrato_id, estado_anterior, estado_nuevo, motivo, usuario, fecha)
SELECT c.id, 'activo', c.estado,
       CASE c.estado WHEN 'suspendido' THEN 'Suspensión por mora de dos períodos consecutivos (RN-03)'
                     ELSE 'Cancelación solicitada por el usuario (venta del predio)' END,
       'asesor', NOW() - INTERVAL '20 days'
FROM contrato c WHERE c.estado IN ('suspendido', 'cancelado');

-- Predio 7 sin contrato: sirve para demostrar F-04 (registro de contrato, RN-01 y SWR-19)

-- =====================================================================
-- HISTÓRICO DE 24 MESES (F-06 historial de consumos, F-07 cartera, F-08 tendencias)
-- k = meses hacia atrás. La entrega 1 ya cargó lecturas de k = 2 y k = 1;
-- aquí se agregan k = 3..24 (k = 3..14 para CT-ACU-0005).
-- El período k = 1 (mes anterior) queda SIN facturar para demostrar F-01.
-- =====================================================================
WITH base(numero_contrato, l0, c, meses) AS (
    VALUES ('CT-ACU-0001', 1520.00, 18.00, 24), ('CT-ALC-0001', 1480.00, 17.00, 24),
           ('CT-ACU-0002',  865.00, 12.00, 24), ('CT-GAS-0001',  402.00,  9.50, 24),
           ('CT-ACU-0003', 2210.00, 15.00, 24), ('CT-ACU-0004', 5230.00, 96.00, 24),
           ('CT-GAS-0002', 1890.00, 48.00, 24), ('CT-ACU-0005',  300.00, 14.00, 14)
), serie AS (
    SELECT b.numero_contrato, b.l0, k,
           round(b.c * (0.82 + 0.36 * (((k * 37 + length(b.numero_contrato) * 11 + b.l0::INT) % 17) / 16.0)), 2) AS consumo
    FROM base b CROSS JOIN LATERAL generate_series(3, b.meses) AS k
), acumulado AS (
    SELECT s.*, sum(s.consumo) OVER (PARTITION BY s.numero_contrato ORDER BY s.k) AS suma
    FROM serie s
)
INSERT INTO lectura_medidor (medidor_id, periodo, lectura_anterior, lectura_actual, consumo, fecha_lectura)
SELECT m.id,
       to_char(date_trunc('month', CURRENT_DATE) - make_interval(months => a.k), 'YYYY-MM'),
       a.l0 - a.suma, a.l0 - a.suma + a.consumo, a.consumo,
       date_trunc('month', CURRENT_DATE) - make_interval(months => a.k - 1) - INTERVAL '2 days' + TIME '08:00'
FROM acumulado a
JOIN contrato c ON c.numero_contrato = a.numero_contrato
JOIN medidor m  ON m.contrato_id = c.id;

-- Lotes de facturación históricos (k = 2..24), ejecutados el primer día hábil del mes siguiente
INSERT INTO lote_facturacion (periodo, fecha_inicio_proceso, fecha_fin_proceso, estado, total_facturas_generadas)
SELECT to_char(mes, 'YYYY-MM'),
       epq_primer_dia_habil((mes + INTERVAL '1 month')::DATE) + TIME '01:00:00',
       epq_primer_dia_habil((mes + INTERVAL '1 month')::DATE) + TIME '01:02:47',
       'completado', 0
FROM generate_series(date_trunc('month', CURRENT_DATE) - INTERVAL '24 month',
                     date_trunc('month', CURRENT_DATE) - INTERVAL '2 month', INTERVAL '1 month') AS mes;

-- Facturas históricas: tarifa vigente más específica al cierre de cada período (SWR-03 / RN-08)
INSERT INTO factura (numero_factura, lote_id, contrato_id, lectura_id, tarifa_id, periodo, consumo, valor_consumo,
                     cargo_fijo, subtotal, total, fecha_generacion, fecha_vencimiento, estado, estado_cobro)
SELECT 'FAC-' || replace(l.periodo, '-', '') || '-' || lpad(c.id::TEXT, 6, '0'),
       lo.id, c.id, l.id, t.id, l.periodo, l.consumo,
       round(l.consumo * t.valor_por_unidad, 2), t.cargo_fijo,
       round(l.consumo * t.valor_por_unidad, 2) + t.cargo_fijo,
       round(l.consumo * t.valor_por_unidad, 2) + t.cargo_fijo,
       lo.fecha_inicio_proceso + make_interval(secs => c.id * 7),
       lo.fecha_inicio_proceso::DATE + 15,
       'sincronizada', 'pagada'
FROM lectura_medidor l
JOIN medidor m           ON m.id = l.medidor_id
JOIN contrato c          ON c.id = m.contrato_id
JOIN predio p            ON p.id = c.predio_id
JOIN lote_facturacion lo ON lo.periodo = l.periodo
CROSS JOIN LATERAL (
    SELECT t.* FROM tarifa t
    WHERE t.servicio_id = c.servicio_id
      AND t.fecha_vigencia_inicio <= (to_date(l.periodo, 'YYYY-MM') + INTERVAL '1 month - 1 day')::DATE
      AND (t.fecha_vigencia_fin IS NULL OR t.fecha_vigencia_fin >= (to_date(l.periodo, 'YYYY-MM') + INTERVAL '1 month - 1 day')::DATE)
      AND (t.municipio_id IS NULL OR t.municipio_id = p.municipio_id)
      AND (t.estrato IS NULL OR t.estrato = p.estrato)
    ORDER BY (t.municipio_id IS NOT NULL)::INT * 2 + (t.estrato IS NOT NULL)::INT DESC, t.fecha_vigencia_inicio DESC
    LIMIT 1) AS t;

UPDATE lote_facturacion lo SET total_facturas_generadas = (SELECT count(*) FROM factura f WHERE f.lote_id = lo.id);

INSERT INTO sincronizacion_erp (factura_id, fecha_sincronizacion, estado, respuesta_erp, intentos)
SELECT f.id, f.fecha_generacion + INTERVAL '5 minutes', 'exitoso',
       'Referencia ERP: ERP-' || upper(substr(md5(f.numero_factura), 1, 8)) || '. Factura registrada (histórico)', 1
FROM factura f;

-- ---------------------------------------------------------------------
-- Escenarios de cartera (F-07). Función auxiliar: período k meses atrás.
-- ---------------------------------------------------------------------
CREATE TEMP TABLE escenario_mora (numero_contrato VARCHAR(30), k INTEGER, tipo VARCHAR(20));
INSERT INTO escenario_mora VALUES
    -- Moroso con 3 períodos consecutivos sin acuerdo: candidato a suspensión (RN-03 / SWR-32)
    ('CT-ACU-0004', 4, 'vencida'), ('CT-ACU-0004', 3, 'vencida'), ('CT-ACU-0004', 2, 'vencida'),
    -- Moroso con un solo período vencido
    ('CT-GAS-0002', 2, 'vencida'),
    -- Contrato ya suspendido por mora
    ('CT-ACU-0003', 3, 'vencida'), ('CT-ACU-0003', 2, 'vencida'),
    -- Deuda cubierta por un acuerdo de pago VIGENTE al día (RN-07: cobro coercitivo suspendido)
    ('CT-ACU-0002', 4, 'acuerdo'), ('CT-ACU-0002', 3, 'acuerdo'),
    -- Deuda en un acuerdo cuya primera cuota ya venció sin pago: el monitoreo lo marcará INCUMPLIDO (SWR-31)
    ('CT-GAS-0001', 4, 'acuerdo'), ('CT-GAS-0001', 3, 'acuerdo');

UPDATE factura f SET estado_cobro = CASE e.tipo WHEN 'vencida' THEN 'vencida' ELSE 'pendiente' END
FROM escenario_mora e JOIN contrato c ON c.numero_contrato = e.numero_contrato
WHERE f.contrato_id = c.id
  AND f.periodo = to_char(date_trunc('month', CURRENT_DATE) - make_interval(months => e.k), 'YYYY-MM');

-- Acuerdos de pago (F-07)
INSERT INTO acuerdo_pago (numero_acuerdo, contrato_id, fecha_acuerdo, total_deuda, numero_cuotas, estado, observacion, registrado_por)
SELECT 'AP-' || to_char(NOW(), 'YYYY') || '-' || lpad(x.n::TEXT, 4, '0'), c.id, NOW() - make_interval(days => x.dias),
       (SELECT sum(f.total) FROM factura f WHERE f.contrato_id = c.id AND f.estado_cobro = 'pendiente'),
       x.cuotas, 'vigente', x.obs, 'cartera'
FROM (VALUES (1, 'CT-ACU-0002', 25, 4, 'Acuerdo al día: primera cuota pagada'),
             (2, 'CT-GAS-0001', 40, 3, 'Primera cuota vencida sin pago: se incumplirá en el próximo monitoreo'))
     AS x(n, numero_contrato, dias, cuotas, obs)
JOIN contrato c ON c.numero_contrato = x.numero_contrato;

UPDATE factura f SET acuerdo_pago_id = a.id
FROM acuerdo_pago a WHERE a.contrato_id = f.contrato_id AND f.estado_cobro = 'pendiente';

-- Cuotas: valor igual, la última absorbe el redondeo; vencimientos mensuales (SWR-30)
INSERT INTO cuota_acuerdo (acuerdo_id, numero_cuota, valor, fecha_vencimiento, estado, fecha_pago, canal_pago, referencia_pago)
SELECT a.id, n,
       CASE WHEN n < a.numero_cuotas THEN round(a.total_deuda / a.numero_cuotas, 2)
            ELSE a.total_deuda - round(a.total_deuda / a.numero_cuotas, 2) * (a.numero_cuotas - 1) END,
       (a.fecha_acuerdo::DATE + 15 + make_interval(months => n - 1))::DATE,
       CASE WHEN a.numero_acuerdo LIKE '%-0001' AND n = 1 THEN 'pagada' ELSE 'pendiente' END,
       CASE WHEN a.numero_acuerdo LIKE '%-0001' AND n = 1 THEN a.fecha_acuerdo + INTERVAL '10 days' END,
       CASE WHEN a.numero_acuerdo LIKE '%-0001' AND n = 1 THEN 'presencial' END,
       CASE WHEN a.numero_acuerdo LIKE '%-0001' AND n = 1 THEN 'RC-' || to_char(NOW(), 'YYYY') || '-00871' END
FROM acuerdo_pago a CROSS JOIN LATERAL generate_series(1, a.numero_cuotas) AS n;

-- Proceso de cobro coercitivo por contrato en mora (RN-07)
INSERT INTO proceso_cobro (contrato_id, estado, fecha_inicio, fecha_actualizacion, motivo)
SELECT c.id,
       CASE WHEN EXISTS (SELECT 1 FROM acuerdo_pago a WHERE a.contrato_id = c.id AND a.estado = 'vigente')
            THEN 'suspendido' ELSE 'activo' END,
       (SELECT min(f.fecha_vencimiento) + 1 FROM factura f WHERE f.contrato_id = c.id AND f.estado_cobro <> 'pagada'),
       NOW() - INTERVAL '20 days',
       CASE WHEN EXISTS (SELECT 1 FROM acuerdo_pago a WHERE a.contrato_id = c.id AND a.estado = 'vigente')
            THEN 'Cobro suspendido por acuerdo de pago vigente (RN-07)'
            ELSE 'Facturas vencidas sin pago' END
FROM contrato c
WHERE EXISTS (SELECT 1 FROM factura f WHERE f.contrato_id = c.id AND f.estado_cobro IN ('vencida', 'pendiente')
              AND f.fecha_vencimiento < CURRENT_DATE);

-- Pagos de las facturas históricas pagadas (SWR-24: canal presencial, en línea o banco)
INSERT INTO pago (codigo_pago, factura_id, valor, canal, fecha_pago, codigo_transaccion, referencia, numero_comprobante, registrado_por)
SELECT 'PAG-' || lpad(nextval('seq_codigo_pago')::TEXT, 9, '0'), f.id, f.total, x.canal,
       f.fecha_vencimiento - make_interval(days => (f.id % 9)::INT) + TIME '10:15:00',
       CASE WHEN x.canal = 'en_linea' THEN 'TX-' || upper(substr(md5('tx' || f.numero_factura), 1, 12)) END,
       CASE WHEN x.canal = 'banco' THEN 'BCO-' || upper(substr(md5('bco' || f.numero_factura), 1, 10))
            WHEN x.canal = 'presencial' THEN 'RC-' || f.id END,
       'CP-' || lpad(nextval('seq_numero_comprobante')::TEXT, 9, '0'),
       CASE WHEN x.canal = 'en_linea' THEN 'ciudadano:' || cl.numero_documento ELSE 'asesor' END
FROM factura f
JOIN contrato c ON c.id = f.contrato_id
JOIN cliente cl ON cl.id = c.cliente_id
CROSS JOIN LATERAL (SELECT (ARRAY['presencial', 'en_linea', 'banco'])[1 + ((f.id + c.id) % 3)::INT] AS canal) AS x
WHERE f.estado_cobro = 'pagada'
ORDER BY f.id;

UPDATE factura f SET fecha_pago = p.fecha_pago FROM pago p WHERE p.factura_id = f.id;

-- =====================================================================
-- F-02: municipio, contrato y cumplimiento del plazo de las PQR
-- =====================================================================
UPDATE pqr p SET municipio_id = (SELECT id FROM municipio WHERE nombre = 'Montenegro'),
                 contrato_id  = (SELECT id FROM contrato WHERE numero_contrato = 'CT-ACU-0001')
WHERE p.ciudadano_id = 1;
UPDATE pqr p SET municipio_id = (SELECT id FROM municipio WHERE nombre = 'Salento'),
                 contrato_id  = (SELECT id FROM contrato WHERE numero_contrato = 'CT-ACU-0002')
WHERE p.ciudadano_id = 2;
UPDATE pqr SET respondida_en_plazo = (fecha_resolucion <= fecha_limite_respuesta) WHERE fecha_resolucion IS NOT NULL;

-- PQR históricas de los 11 meses anteriores (indicadores y tendencias del dashboard, SyR-02f)
INSERT INTO pqr (radicado, ciudadano_id, tipo_solicitud_id, canal_id, estado_id, gestor_id, asunto, descripcion,
                 fecha_recepcion, fecha_limite_respuesta, fecha_estimada_respuesta, fecha_resolucion, respuesta,
                 notificado_vencimiento, municipio_id, respondida_en_plazo)
SELECT to_char(x.recepcion, 'YYYYMM') || '-' || lpad(x.n::TEXT, 4, '0'),
       1 + (x.k + x.n) % 2,
       (SELECT id FROM tipo_solicitud WHERE nombre = x.tipo),
       (SELECT id FROM canal_atencion WHERE nombre = (ARRAY['Web', 'Presencial', 'Telefónico', 'Web'])[1 + (x.k + x.n) % 4]),
       (SELECT id FROM estado_pqr WHERE nombre = CASE WHEN x.en_plazo THEN 'Cerrado' ELSE 'Resuelto' END),
       g.id,
       (ARRAY['Cobro elevado en la factura', 'Daño en la acometida', 'Solicitud de copia de factura',
              'Baja presión del agua', 'Revisión de lectura del medidor'])[1 + (x.k * 3 + x.n) % 5],
       'PQR histórica cargada para los indicadores del dashboard gerencial.',
       x.recepcion, x.limite + TIME '23:59:59', x.limite,
       CASE WHEN x.en_plazo THEN epq_restar_dias_habiles(x.limite, 1 + (x.k + x.n) % 6) + TIME '16:20:00'
            ELSE epq_sumar_dias_habiles(x.limite, 2) + TIME '11:05:00' END,
       'Respuesta formal registrada por el gestor (histórico).',
       TRUE, g.municipio_id, x.en_plazo
FROM (
    SELECT k, n, tipo,
           (date_trunc('month', CURRENT_DATE) - make_interval(months => k) + make_interval(days => 2 + n * 6))::DATE
               + TIME '10:00:00' AS recepcion,
           epq_sumar_dias_habiles((date_trunc('month', CURRENT_DATE) - make_interval(months => k)
                                   + make_interval(days => 2 + n * 6))::DATE,
                                  CASE WHEN tipo = 'Petición' THEN 10 ELSE 15 END) AS limite,
           NOT ((k + n * 2) % 7 = 0) AS en_plazo
    FROM generate_series(1, 11) AS k
    CROSS JOIN generate_series(1, 4) AS n
    CROSS JOIN LATERAL (SELECT (ARRAY['Petición', 'Queja', 'Reclamo'])[1 + (k + n) % 3] AS tipo) AS t
) AS x
JOIN gestor g ON g.id = 1 + (x.k + x.n) % 3;

INSERT INTO historial_pqr (pqr_id, estado_id, fecha_cambio, comentario, usuario_cambio)
SELECT p.id, (SELECT id FROM estado_pqr WHERE nombre = 'Radicado'), p.fecha_recepcion, 'PQR registrada (histórico)', 'SISTEMA'
FROM pqr p WHERE p.descripcion LIKE 'PQR histórica%';
INSERT INTO historial_pqr (pqr_id, estado_id, fecha_cambio, comentario, usuario_cambio)
SELECT p.id, p.estado_id, p.fecha_resolucion,
       CASE WHEN p.respondida_en_plazo THEN 'Respuesta formal registrada dentro del plazo (histórico)'
            ELSE 'Respuesta extemporánea registrada (histórico)' END, g.correo
FROM pqr p JOIN gestor g ON g.id = p.gestor_id WHERE p.descripcion LIKE 'PQR histórica%';

-- =====================================================================
-- Auditoría y secuencias
-- =====================================================================
INSERT INTO log_auditoria (usuario, rol, modulo, accion, entidad, detalle)
VALUES ('SISTEMA', NULL, 'SISTEMA', 'CARGA_INICIAL', NULL,
        'Carga de datos de demostración de la entrega 2 (04_seed_entrega2.sql)');

SELECT setval('seq_codigo_usuario', (SELECT max(substr(codigo_interno, 5)::INT) FROM cliente));
SELECT setval('seq_codigo_predio',  (SELECT max(substr(codigo_predio, 5)::INT) FROM predio));
SELECT setval('seq_numero_contrato', (SELECT max(substr(numero_contrato, 8)::INT) FROM contrato));
SELECT setval('seq_numero_acuerdo', (SELECT count(*) FROM acuerdo_pago));
