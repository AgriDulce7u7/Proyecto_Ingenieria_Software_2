-- =====================================================================
-- ESQUEMA - ENTREGA 2 (SIGCA-EPQ)
-- Evoluciona el esquema de la entrega 1 (01_schema.sql) sin romper F-01/F-02
-- y agrega las tablas de F-03 a F-08, seguridad (RF-14) y auditoría (RF-15).
--
--   F-03 Registrar usuario del servicio  -> cliente (ampliada), predio, municipio
--   F-04 Gestionar contrato de servicio   -> contrato (ampliada), historial_contrato, tarifa (estrato/municipio)
--   F-05 Registrar pago de factura        -> pago, intento_pago_en_linea, factura.estado_cobro
--   F-06 Consultar factura e historial    -> (consultas sobre factura / lectura_medidor)
--   F-07 Gestionar cobro persuasivo       -> acuerdo_pago, cuota_acuerdo, proceso_cobro
--   F-08 Dashboard gerencial              -> (consultas agregadas)
--   RF-14 Roles y cuentas                 -> funcionario
--   RF-15 Log de auditoría                -> log_auditoria
--   Ajustes F-02                          -> gestor (municipio/jefe), pqr (municipio/contrato/cumplimiento), evidencia_pqr
-- =====================================================================

-- ---------------------------------------------------------------------
-- Catálogo de municipios atendidos por EPQ (CO-03)
-- ---------------------------------------------------------------------
CREATE TABLE municipio (
    id          SERIAL PRIMARY KEY,
    nombre      VARCHAR(80) NOT NULL UNIQUE,
    codigo_dane VARCHAR(10) NOT NULL UNIQUE,
    activo      BOOLEAN NOT NULL DEFAULT TRUE
);

-- ---------------------------------------------------------------------
-- RF-15: log de auditoría transversal
-- ---------------------------------------------------------------------
CREATE TABLE log_auditoria (
    id          BIGSERIAL PRIMARY KEY,
    fecha       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    usuario     VARCHAR(150) NOT NULL,           -- login del funcionario, ciudadano:<doc> o SISTEMA
    rol         VARCHAR(40),
    modulo      VARCHAR(40) NOT NULL,            -- SEGURIDAD, USUARIOS, CONTRATOS, FACTURACION, PAGOS, PQR, CARTERA
    accion      VARCHAR(60) NOT NULL,            -- INICIO_SESION, CREAR_CONTRATO, GENERAR_LOTE...
    entidad     VARCHAR(60),
    entidad_id  VARCHAR(60),
    detalle     TEXT,
    exitoso     BOOLEAN NOT NULL DEFAULT TRUE,
    ip_origen   VARCHAR(60)
);

CREATE INDEX idx_auditoria_fecha   ON log_auditoria(fecha DESC);
CREATE INDEX idx_auditoria_modulo  ON log_auditoria(modulo, accion);
CREATE INDEX idx_auditoria_usuario ON log_auditoria(usuario);
CREATE INDEX idx_auditoria_entidad ON log_auditoria(entidad, entidad_id);

-- ---------------------------------------------------------------------
-- F-02: gestores por municipio y jefe de la Oficina de PQR (SyR-02b, SWR-11)
-- ---------------------------------------------------------------------
ALTER TABLE gestor
    ADD COLUMN municipio_id INTEGER REFERENCES municipio(id),
    ADD COLUMN es_jefe      BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN jefe_id      INTEGER REFERENCES gestor(id);

-- ---------------------------------------------------------------------
-- RF-14: cuentas de funcionarios y roles (RBAC, principio de mínimo privilegio)
-- ---------------------------------------------------------------------
CREATE TABLE funcionario (
    id                  SERIAL PRIMARY KEY,
    usuario             VARCHAR(60)  NOT NULL UNIQUE,
    nombre_completo     VARCHAR(150) NOT NULL,
    correo              VARCHAR(150) NOT NULL UNIQUE,
    password_hash       VARCHAR(100) NOT NULL,
    rol                 VARCHAR(30)  NOT NULL CHECK (rol IN ('ADMINISTRADOR', 'ASESOR_COMERCIAL', 'GESTOR_PQR',
                                                            'JEFE_PQR', 'GESTOR_CARTERA', 'GERENTE', 'DIRECTIVO')),
    municipio_id        INTEGER REFERENCES municipio(id),
    gestor_id           INTEGER UNIQUE REFERENCES gestor(id),   -- vínculo con el gestor de PQR (si aplica)
    activo              BOOLEAN NOT NULL DEFAULT TRUE,
    intentos_fallidos   SMALLINT NOT NULL DEFAULT 0,
    bloqueado_hasta     TIMESTAMP,
    ultimo_acceso       TIMESTAMP,
    fecha_creacion      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP
);

-- ---------------------------------------------------------------------
-- F-03: usuario del servicio (se amplía la tabla cliente de la entrega 1)
-- ---------------------------------------------------------------------
ALTER TABLE cliente
    ADD COLUMN codigo_interno         VARCHAR(20) UNIQUE,
    ADD COLUMN tipo_persona           VARCHAR(10) NOT NULL DEFAULT 'NATURAL' CHECK (tipo_persona IN ('NATURAL', 'JURIDICA')),
    ADD COLUMN direccion_notificacion VARCHAR(200),
    ADD COLUMN municipio_id           INTEGER REFERENCES municipio(id),
    ADD COLUMN estado_validacion      VARCHAR(20) NOT NULL DEFAULT 'pendiente'
        CHECK (estado_validacion IN ('validado', 'pendiente', 'rechazado')),
    ADD COLUMN detalle_validacion     VARCHAR(300),
    ADD COLUMN fecha_validacion       TIMESTAMP,
    ADD COLUMN activo                 BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN fecha_registro         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN fecha_actualizacion    TIMESTAMP,
    ADD COLUMN registrado_por         VARCHAR(150),
    -- Credenciales del portal de autogestión (RF-07: número de identificación + contraseña)
    ADD COLUMN password_hash          VARCHAR(100),
    ADD COLUMN intentos_fallidos      SMALLINT NOT NULL DEFAULT 0,
    ADD COLUMN bloqueado_hasta        TIMESTAMP;

-- SWR-12: el número de identificación es único en el sistema
CREATE UNIQUE INDEX ux_cliente_numero_documento ON cliente(numero_documento);
-- SWR-16: búsquedas por nombre y dirección en menos de 3 segundos
CREATE INDEX idx_cliente_nombre ON cliente(lower(nombre_completo));

CREATE TABLE predio (
    id              SERIAL PRIMARY KEY,
    codigo_predio   VARCHAR(20)  NOT NULL UNIQUE,
    direccion       VARCHAR(200) NOT NULL,
    municipio_id    INTEGER NOT NULL REFERENCES municipio(id),
    estrato         SMALLINT NOT NULL CHECK (estrato BETWEEN 1 AND 6),
    uso             VARCHAR(15) NOT NULL DEFAULT 'RESIDENCIAL' CHECK (uso IN ('RESIDENCIAL', 'COMERCIAL', 'INDUSTRIAL')),
    cliente_id      INTEGER REFERENCES cliente(id),      -- usuario que registró/solicitó el predio
    fecha_registro  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_predio_direccion ON predio(lower(direccion));
CREATE INDEX idx_predio_cliente   ON predio(cliente_id);

-- ---------------------------------------------------------------------
-- F-04: tarifas por servicio, municipio y estrato (RN-08, SWR-18, SWR-03)
-- Un valor NULL significa "aplica a todos". Se elige la tarifa vigente más específica.
-- ---------------------------------------------------------------------
ALTER TABLE tarifa
    ADD COLUMN municipio_id INTEGER REFERENCES municipio(id),
    ADD COLUMN estrato      SMALLINT CHECK (estrato BETWEEN 1 AND 6);

CREATE INDEX idx_tarifa_servicio ON tarifa(servicio_id, fecha_vigencia_inicio);

-- ---------------------------------------------------------------------
-- F-04: contratos (estados del modelo de dominio: Activo, Suspendido, Cancelado)
-- 'pendiente' = registrado pero aún no activado (p. ej. falta el medidor, SWR-19)
-- ---------------------------------------------------------------------
ALTER TABLE contrato DROP CONSTRAINT IF EXISTS contrato_estado_check;
UPDATE contrato SET estado = 'cancelado' WHERE estado = 'inactivo';
ALTER TABLE contrato
    ADD CONSTRAINT contrato_estado_check CHECK (estado IN ('pendiente', 'activo', 'suspendido', 'cancelado')),
    ADD COLUMN predio_id                  INTEGER REFERENCES predio(id),
    ADD COLUMN tarifa_id                  INTEGER REFERENCES tarifa(id),
    ADD COLUMN candidato_suspension       BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN fecha_candidato_suspension TIMESTAMP,
    ADD COLUMN registrado_por             VARCHAR(150),
    ADD COLUMN fecha_registro             TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- RN-01 / SWR-17: un solo contrato ACTIVO por tipo de servicio y predio (garantía a nivel de BD)
CREATE UNIQUE INDEX ux_contrato_activo_predio_servicio
    ON contrato(predio_id, servicio_id) WHERE estado = 'activo';
CREATE INDEX idx_contrato_cliente ON contrato(cliente_id);

CREATE TABLE historial_contrato (
    id              BIGSERIAL PRIMARY KEY,
    contrato_id     INTEGER NOT NULL REFERENCES contrato(id),
    estado_anterior VARCHAR(20),
    estado_nuevo    VARCHAR(20) NOT NULL,
    motivo          VARCHAR(500) NOT NULL,
    usuario         VARCHAR(150) NOT NULL,
    fecha           TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_historial_contrato ON historial_contrato(contrato_id, fecha);

ALTER TABLE medidor
    ADD COLUMN fecha_retiro TIMESTAMP;

-- ---------------------------------------------------------------------
-- F-07: acuerdos de pago (RF-12, RN-07)
-- ---------------------------------------------------------------------
CREATE TABLE acuerdo_pago (
    id                    SERIAL PRIMARY KEY,
    numero_acuerdo        VARCHAR(30) NOT NULL UNIQUE,
    contrato_id           INTEGER NOT NULL REFERENCES contrato(id),
    fecha_acuerdo         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total_deuda           NUMERIC(14,2) NOT NULL CHECK (total_deuda > 0),
    numero_cuotas         SMALLINT NOT NULL CHECK (numero_cuotas BETWEEN 1 AND 36),
    estado                VARCHAR(20) NOT NULL DEFAULT 'vigente' CHECK (estado IN ('vigente', 'incumplido', 'cumplido')),
    fecha_cambio_estado   TIMESTAMP,
    observacion           VARCHAR(500),
    registrado_por        VARCHAR(150) NOT NULL
);

-- RN-07: un contrato tiene como máximo un acuerdo vigente
CREATE UNIQUE INDEX ux_acuerdo_vigente_contrato ON acuerdo_pago(contrato_id) WHERE estado = 'vigente';

CREATE TABLE cuota_acuerdo (
    id                  SERIAL PRIMARY KEY,
    acuerdo_id          INTEGER NOT NULL REFERENCES acuerdo_pago(id) ON DELETE CASCADE,
    numero_cuota        SMALLINT NOT NULL,
    valor               NUMERIC(14,2) NOT NULL CHECK (valor > 0),
    fecha_vencimiento   DATE NOT NULL,
    estado              VARCHAR(20) NOT NULL DEFAULT 'pendiente' CHECK (estado IN ('pendiente', 'pagada', 'vencida')),
    fecha_pago          TIMESTAMP,
    canal_pago          VARCHAR(20) CHECK (canal_pago IN ('presencial', 'en_linea', 'banco')),
    referencia_pago     VARCHAR(100),
    UNIQUE (acuerdo_id, numero_cuota)
);

CREATE INDEX idx_cuota_vencimiento ON cuota_acuerdo(fecha_vencimiento) WHERE estado = 'pendiente';

-- Proceso de cobro coercitivo por contrato (RN-07: se suspende con acuerdo vigente y se reactiva al incumplir)
CREATE TABLE proceso_cobro (
    id                  SERIAL PRIMARY KEY,
    contrato_id         INTEGER NOT NULL UNIQUE REFERENCES contrato(id),
    estado              VARCHAR(20) NOT NULL CHECK (estado IN ('activo', 'suspendido', 'cerrado')),
    fecha_inicio        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    motivo              VARCHAR(500)
);

-- ---------------------------------------------------------------------
-- F-05: estado de cobro de la factura (DE-01) separado del estado de sincronización con el ERP
--   factura.estado       -> sincronización ERP (generada, sincronizada, error_sincronizacion)  [entrega 1]
--   factura.estado_cobro -> ciclo de cobro (pendiente, pagada, vencida, anulada)              [DE-01]
-- ---------------------------------------------------------------------
ALTER TABLE factura
    ADD COLUMN estado_cobro      VARCHAR(20) NOT NULL DEFAULT 'pendiente'
        CHECK (estado_cobro IN ('pendiente', 'pagada', 'vencida', 'anulada')),
    ADD COLUMN fecha_pago        TIMESTAMP,
    ADD COLUMN motivo_anulacion  VARCHAR(500),
    ADD COLUMN fecha_anulacion   TIMESTAMP,
    ADD COLUMN anulada_por       VARCHAR(150),
    ADD COLUMN acuerdo_pago_id   INTEGER REFERENCES acuerdo_pago(id);

CREATE INDEX idx_factura_estado_cobro ON factura(estado_cobro, fecha_vencimiento);
CREATE INDEX idx_factura_periodo      ON factura(periodo);

CREATE TABLE pago (
    id                  BIGSERIAL PRIMARY KEY,
    codigo_pago         VARCHAR(30) NOT NULL UNIQUE,
    factura_id          BIGINT NOT NULL UNIQUE REFERENCES factura(id),   -- SWR-23: una factura se paga una sola vez
    valor               NUMERIC(12,2) NOT NULL CHECK (valor > 0),
    canal               VARCHAR(20) NOT NULL CHECK (canal IN ('presencial', 'en_linea', 'banco')),   -- SWR-24
    fecha_pago          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    codigo_transaccion  VARCHAR(60),       -- código de la pasarela (pagos en línea)
    referencia          VARCHAR(100),      -- recibo de caja o referencia bancaria
    numero_comprobante  VARCHAR(30) NOT NULL UNIQUE,
    registrado_por      VARCHAR(150) NOT NULL
);

CREATE INDEX idx_pago_fecha ON pago(fecha_pago);

-- Trazabilidad de los intentos de pago en línea (aprobados, rechazados o sin respuesta de la pasarela)
CREATE TABLE intento_pago_en_linea (
    id                  BIGSERIAL PRIMARY KEY,
    factura_id          BIGINT NOT NULL REFERENCES factura(id),
    fecha               TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    valor               NUMERIC(12,2) NOT NULL,
    resultado           VARCHAR(20) NOT NULL CHECK (resultado IN ('aprobado', 'rechazado', 'sin_respuesta')),
    codigo_transaccion  VARCHAR(60),
    codigo_respuesta    VARCHAR(30),
    mensaje             VARCHAR(300),
    ultimos_digitos     VARCHAR(4),        -- nunca se almacena el número completo de la tarjeta
    intentos            SMALLINT NOT NULL DEFAULT 1,
    usuario             VARCHAR(150) NOT NULL
);

CREATE INDEX idx_intento_pago_factura ON intento_pago_en_linea(factura_id);

-- ---------------------------------------------------------------------
-- F-02: municipio y contrato de la PQR, cumplimiento del plazo y evidencias (RN-06, SyR-02f)
-- ---------------------------------------------------------------------
ALTER TABLE pqr
    ADD COLUMN municipio_id      INTEGER REFERENCES municipio(id),
    ADD COLUMN contrato_id       INTEGER REFERENCES contrato(id),
    ADD COLUMN respondida_en_plazo BOOLEAN;   -- NULL = sin respuesta aún; FALSE también si venció

CREATE TABLE evidencia_pqr (
    id              BIGSERIAL PRIMARY KEY,
    pqr_id          BIGINT NOT NULL REFERENCES pqr(id) ON DELETE CASCADE,
    nombre_archivo  VARCHAR(200) NOT NULL,
    tipo_contenido  VARCHAR(100) NOT NULL,
    tamano_bytes    INTEGER NOT NULL,
    contenido       BYTEA NOT NULL,
    descripcion     VARCHAR(300),
    fecha_carga     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    usuario         VARCHAR(150) NOT NULL
);

CREATE INDEX idx_evidencia_pqr ON evidencia_pqr(pqr_id);

-- ---------------------------------------------------------------------
-- Notificaciones del sistema a funcionarios y usuarios (IS-SIS-02)
-- (las notificaciones propias de PQR siguen en la tabla notificacion)
-- ---------------------------------------------------------------------
CREATE TABLE notificacion_sistema (
    id              BIGSERIAL PRIMARY KEY,
    destinatario    VARCHAR(150),
    funcionario_id  INTEGER REFERENCES funcionario(id),
    tipo            VARCHAR(50) NOT NULL,
    asunto          VARCHAR(200) NOT NULL,
    mensaje         TEXT NOT NULL,
    referencia      VARCHAR(60),
    fecha_envio     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    enviado_ok      BOOLEAN NOT NULL DEFAULT FALSE,
    leida           BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_notificacion_sistema_funcionario ON notificacion_sistema(funcionario_id, leida);

-- ---------------------------------------------------------------------
-- Secuencias para códigos de negocio únicos (seguros ante concurrencia)
-- ---------------------------------------------------------------------
CREATE SEQUENCE seq_codigo_usuario;      -- USR-000001
CREATE SEQUENCE seq_codigo_predio;       -- PRD-000001
CREATE SEQUENCE seq_numero_contrato;     -- CT-ACU-0001
CREATE SEQUENCE seq_numero_acuerdo;      -- AP-2026-0001
CREATE SEQUENCE seq_codigo_pago;         -- PAG-000000001
CREATE SEQUENCE seq_numero_comprobante;  -- CP-000000001
