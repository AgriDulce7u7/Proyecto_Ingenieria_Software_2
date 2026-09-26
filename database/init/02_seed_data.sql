-- =====================================================================
-- DATOS SEMILLA - PROYECTO EPQ (SIGCA-EPQ)
-- Catálogos obligatorios para que el backend funcione + datos de prueba.
-- Se ejecuta automáticamente después de 01_schema.sql al crear el contenedor.
--
-- IMPORTANTE: los nombres de los catálogos (estado_pqr, tipo_solicitud,
-- canal_atencion) son usados por el backend. No los cambie sin actualizar
-- los enums EstadoPqrTipo, TipoSolicitudTipo y CanalAtencionTipo.
-- =====================================================================

-- ---------------------------------------------------------------------
-- MÓDULO PQR - Catálogos
-- ---------------------------------------------------------------------
INSERT INTO tipo_solicitud (nombre) VALUES
    ('Petición'), ('Queja'), ('Reclamo');

INSERT INTO canal_atencion (nombre) VALUES
    ('Web'), ('Presencial'), ('Telefónico'), ('Correo electrónico');

INSERT INTO estado_pqr (nombre, orden) VALUES
    ('Radicado',   1),
    ('En trámite', 2),
    ('Resuelto',   3),
    ('Cerrado',    4),
    ('Vencido',    5);

INSERT INTO gestor (nombre, correo, area, activo) VALUES
    ('Laura Giraldo Ríos',     'lgiraldo@epq.com.co',  'Oficina de PQR', TRUE),
    ('Andrés Ocampo Salazar',  'aocampo@epq.com.co',   'Oficina de PQR', TRUE),
    ('Marcela Castaño Pérez',  'mcastano@epq.com.co',  'Oficina de PQR', TRUE),
    ('Jorge Arango Londoño',   'jarango@epq.com.co',   'Oficina de PQR', FALSE);

-- ---------------------------------------------------------------------
-- MÓDULO PQR - Datos de demostración
-- ---------------------------------------------------------------------
INSERT INTO ciudadano (tipo_documento, numero_documento, nombre_completo, correo, telefono, direccion) VALUES
    ('CC', '1094950001', 'Carlos Mario Restrepo', 'carlos.restrepo@correo.com', '3104567890', 'Cra 14 # 20-35, Armenia'),
    ('CC', '41912345',   'Gloria Inés Villegas',  'gloria.villegas@correo.com', '3157894561', 'Calle 5 # 3-12, Salento');

-- PQR 1: recién radicada
INSERT INTO pqr (radicado, ciudadano_id, tipo_solicitud_id, canal_id, estado_id, gestor_id, asunto, descripcion,
                 fecha_recepcion, fecha_limite_respuesta, fecha_estimada_respuesta)
VALUES (to_char(NOW(), 'YYYYMM') || '-0001',
        1, (SELECT id FROM tipo_solicitud WHERE nombre = 'Reclamo'),
        (SELECT id FROM canal_atencion WHERE nombre = 'Web'),
        (SELECT id FROM estado_pqr WHERE nombre = 'Radicado'), 1,
        'Cobro elevado en la factura', 'La factura del último mes presenta un consumo muy superior al habitual.',
        NOW() - INTERVAL '1 day', (NOW() + INTERVAL '20 days')::date + TIME '23:59:59', (NOW() + INTERVAL '20 days')::date);

-- PQR 2: en trámite y a menos de 48 horas del vencimiento (sirve para demostrar SWR-07)
INSERT INTO pqr (radicado, ciudadano_id, tipo_solicitud_id, canal_id, estado_id, gestor_id, asunto, descripcion,
                 fecha_recepcion, fecha_limite_respuesta, fecha_estimada_respuesta)
VALUES (to_char(NOW(), 'YYYYMM') || '-0002',
        2, (SELECT id FROM tipo_solicitud WHERE nombre = 'Petición'),
        (SELECT id FROM canal_atencion WHERE nombre = 'Presencial'),
        (SELECT id FROM estado_pqr WHERE nombre = 'En trámite'), 2,
        'Solicitud de revisión de medidor', 'Solicito la visita técnica para revisar el medidor de acueducto.',
        NOW() - INTERVAL '13 days', NOW() + INTERVAL '30 hours', (NOW() + INTERVAL '30 hours')::date);

-- PQR 3: resuelta
INSERT INTO pqr (radicado, ciudadano_id, tipo_solicitud_id, canal_id, estado_id, gestor_id, asunto, descripcion,
                 fecha_recepcion, fecha_limite_respuesta, fecha_estimada_respuesta, fecha_resolucion, respuesta)
VALUES (to_char(NOW(), 'YYYYMM') || '-0003',
        1, (SELECT id FROM tipo_solicitud WHERE nombre = 'Queja'),
        (SELECT id FROM canal_atencion WHERE nombre = 'Telefónico'),
        (SELECT id FROM estado_pqr WHERE nombre = 'Resuelto'), 3,
        'Demora en reconexión', 'Pagué la factura y el servicio no ha sido reconectado.',
        NOW() - INTERVAL '6 days', (NOW() + INTERVAL '15 days')::date + TIME '23:59:59', (NOW() + INTERVAL '15 days')::date,
        NOW() - INTERVAL '2 days', 'Se realizó la reconexión del servicio el día de hoy. Se adjunta orden de trabajo.');

INSERT INTO historial_pqr (pqr_id, estado_id, fecha_cambio, comentario, usuario_cambio)
SELECT p.id, (SELECT id FROM estado_pqr WHERE nombre = 'Radicado'), p.fecha_recepcion, 'PQR registrada (datos semilla)', 'SISTEMA'
FROM pqr p;

INSERT INTO historial_pqr (pqr_id, estado_id, fecha_cambio, comentario, usuario_cambio)
SELECT p.id, p.estado_id, p.fecha_recepcion + INTERVAL '1 day', 'Cambio de estado (datos semilla)', g.correo
FROM pqr p JOIN gestor g ON g.id = p.gestor_id
WHERE p.estado_id <> (SELECT id FROM estado_pqr WHERE nombre = 'Radicado');

-- ---------------------------------------------------------------------
-- MÓDULO FACTURACIÓN - Catálogos
-- ---------------------------------------------------------------------
INSERT INTO servicio (nombre) VALUES
    ('Acueducto'), ('Alcantarillado'), ('Gas');

-- Tarifas históricas (vencidas) y vigentes. Valores en COP por m³.
INSERT INTO tarifa (servicio_id, nombre, valor_por_unidad, cargo_fijo, fecha_vigencia_inicio, fecha_vigencia_fin) VALUES
    ((SELECT id FROM servicio WHERE nombre = 'Acueducto'),      'Acueducto residencial 2025',      2980.00, 11800.00, '2025-01-01', '2025-12-31'),
    ((SELECT id FROM servicio WHERE nombre = 'Acueducto'),      'Acueducto residencial 2026',      3215.50, 12650.00, '2026-01-01', NULL),
    ((SELECT id FROM servicio WHERE nombre = 'Alcantarillado'), 'Alcantarillado residencial 2026', 2870.25,  9840.00, '2026-01-01', NULL),
    ((SELECT id FROM servicio WHERE nombre = 'Gas'),            'Gas natural residencial 2026',    2415.80,  4520.00, '2026-01-01', NULL);

-- ---------------------------------------------------------------------
-- MÓDULO FACTURACIÓN - Datos de demostración
-- ---------------------------------------------------------------------
INSERT INTO cliente (tipo_documento, numero_documento, nombre_completo, correo, telefono) VALUES
    ('CC',  '1094950001', 'Carlos Mario Restrepo',        'carlos.restrepo@correo.com', '3104567890'),
    ('CC',  '41912345',   'Gloria Inés Villegas',         'gloria.villegas@correo.com', '3157894561'),
    ('CC',  '7548123',    'Hernán Darío Quintero',        'hquintero@correo.com',       '3201112233'),
    ('NIT', '900123456',  'Hostal Cafetero Filandia SAS', 'admin@hostalcafetero.com',   '6067589900'),
    ('CC',  '1097034567', 'Valentina Osorio Mejía',       'vosorio@correo.com',         '3017778899');

INSERT INTO contrato (numero_contrato, cliente_id, servicio_id, direccion_servicio, fecha_inicio, estado) VALUES
    ('CT-ACU-0001', 1, (SELECT id FROM servicio WHERE nombre = 'Acueducto'),      'Cra 14 # 20-35, Armenia',          '2022-03-10', 'activo'),
    ('CT-ALC-0001', 1, (SELECT id FROM servicio WHERE nombre = 'Alcantarillado'), 'Cra 14 # 20-35, Armenia',          '2022-03-10', 'activo'),
    ('CT-ACU-0002', 2, (SELECT id FROM servicio WHERE nombre = 'Acueducto'),      'Calle 5 # 3-12, Salento',          '2021-07-01', 'activo'),
    ('CT-GAS-0001', 2, (SELECT id FROM servicio WHERE nombre = 'Gas'),            'Calle 5 # 3-12, Salento',          '2023-01-15', 'activo'),
    ('CT-ACU-0003', 3, (SELECT id FROM servicio WHERE nombre = 'Acueducto'),      'Calle 10 # 8-40, Circasia',        '2020-11-20', 'suspendido'),
    ('CT-ACU-0004', 4, (SELECT id FROM servicio WHERE nombre = 'Acueducto'),      'Parque principal, Filandia',       '2019-05-05', 'activo'),
    ('CT-GAS-0002', 4, (SELECT id FROM servicio WHERE nombre = 'Gas'),            'Parque principal, Filandia',       '2019-05-05', 'activo'),
    ('CT-ACU-0005', 5, (SELECT id FROM servicio WHERE nombre = 'Acueducto'),      'Mz 4 Casa 12, La Tebaida',         '2024-02-01', 'activo'),
    ('CT-ACU-0006', 5, (SELECT id FROM servicio WHERE nombre = 'Acueducto'),      'Finca La Esperanza, Quimbaya',     '2018-08-08', 'inactivo');

INSERT INTO medidor (contrato_id, numero_serie, fecha_instalacion, estado)
SELECT c.id, 'MED-' || c.numero_contrato, c.fecha_inicio, 'activo' FROM contrato c;

-- Lecturas de los dos meses anteriores al actual (relativas a la fecha de creación de la BD).
-- El contrato CT-ACU-0005 NO tiene lectura del mes anterior: sirve para demostrar que
-- el proceso de facturación lo omite y lo reporta.
WITH periodos AS (
    SELECT to_char(NOW() - INTERVAL '2 month', 'YYYY-MM') AS p2,
           to_char(NOW() - INTERVAL '1 month', 'YYYY-MM') AS p1
), base(numero_contrato, l0, c1, c2) AS (
    VALUES ('CT-ACU-0001', 1520.00, 18.00, 21.50),
           ('CT-ALC-0001', 1480.00, 17.00, 20.00),
           ('CT-ACU-0002',  865.00, 12.00, 11.25),
           ('CT-GAS-0001',  402.00,  9.50, 10.00),
           ('CT-ACU-0003', 2210.00, 15.00, 14.00),
           ('CT-ACU-0004', 5230.00, 96.00, 104.75),
           ('CT-GAS-0002', 1890.00, 48.00, 52.30)
)
INSERT INTO lectura_medidor (medidor_id, periodo, lectura_anterior, lectura_actual, consumo)
SELECT m.id, x.periodo, x.anterior, x.anterior + x.consumo, x.consumo
FROM base b
JOIN contrato c ON c.numero_contrato = b.numero_contrato
JOIN medidor m  ON m.contrato_id = c.id
CROSS JOIN periodos p
CROSS JOIN LATERAL (VALUES (p.p2, b.l0, b.c1),
                           (p.p1, b.l0 + b.c1, b.c2)) AS x(periodo, anterior, consumo);

-- Solo lectura de hace dos meses para CT-ACU-0005
INSERT INTO lectura_medidor (medidor_id, periodo, lectura_anterior, lectura_actual, consumo)
SELECT m.id, to_char(NOW() - INTERVAL '2 month', 'YYYY-MM'), 300.00, 314.00, 14.00
FROM medidor m JOIN contrato c ON c.id = m.contrato_id
WHERE c.numero_contrato = 'CT-ACU-0005';
