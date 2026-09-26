-- =====================================================================
-- ESQUEMA DE BASE DE DATOS - PROYECTO EPQ
-- Módulos: PQR (Peticiones/Quejas/Reclamos) y Facturación Automática
-- Motor: PostgreSQL
-- =====================================================================

-- =====================================================================
-- MÓDULO 1: PQR
-- =====================================================================

CREATE TABLE tipo_solicitud (
    id          SERIAL PRIMARY KEY,
    nombre      VARCHAR(30) NOT NULL UNIQUE  -- Petición, Queja, Reclamo
);

CREATE TABLE canal_atencion (
    id          SERIAL PRIMARY KEY,
    nombre      VARCHAR(30) NOT NULL UNIQUE  -- Web, Presencial, Telefónico, etc.
);

CREATE TABLE estado_pqr (
    id          SERIAL PRIMARY KEY,
    nombre      VARCHAR(30) NOT NULL UNIQUE, -- Radicado, En trámite, Resuelto, Cerrado, Vencido
    orden       SMALLINT NOT NULL            -- para ordenar el flujo del estado
);

CREATE TABLE ciudadano (
    id                  SERIAL PRIMARY KEY,
    tipo_documento      VARCHAR(5)  NOT NULL,   -- CC, CE, TI, NIT
    numero_documento    VARCHAR(20) NOT NULL,
    nombre_completo     VARCHAR(150) NOT NULL,
    correo              VARCHAR(150),
    telefono            VARCHAR(20),
    direccion           VARCHAR(200),
    fecha_registro      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (tipo_documento, numero_documento)
);

CREATE TABLE gestor (
    id          SERIAL PRIMARY KEY,
    nombre      VARCHAR(150) NOT NULL,
    correo      VARCHAR(150) NOT NULL UNIQUE,
    area        VARCHAR(100),
    activo      BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE pqr (
    id                          BIGSERIAL PRIMARY KEY,
    radicado                    VARCHAR(30) NOT NULL UNIQUE, -- generado por el sistema
    ciudadano_id                INTEGER NOT NULL REFERENCES ciudadano(id),
    tipo_solicitud_id           INTEGER NOT NULL REFERENCES tipo_solicitud(id),
    canal_id                    INTEGER REFERENCES canal_atencion(id),
    estado_id                   INTEGER NOT NULL REFERENCES estado_pqr(id),
    gestor_id                   INTEGER REFERENCES gestor(id),
    asunto                      VARCHAR(200) NOT NULL,
    descripcion                 TEXT NOT NULL,
    fecha_recepcion             TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_limite_respuesta      TIMESTAMP NOT NULL,      -- usado por el job de notificación 48h
    fecha_estimada_respuesta    DATE NOT NULL,
    fecha_resolucion            TIMESTAMP,
    respuesta                   TEXT,
    notificado_vencimiento      BOOLEAN NOT NULL DEFAULT FALSE -- evita notificar más de una vez
);

-- Índice para la consulta pública por radicado (sin login, debe ser rápida)
CREATE INDEX idx_pqr_radicado ON pqr(radicado);
-- Índice de apoyo para el job que busca próximos a vencer
CREATE INDEX idx_pqr_fecha_limite ON pqr(fecha_limite_respuesta) WHERE notificado_vencimiento = FALSE;

CREATE TABLE historial_pqr (
    id              BIGSERIAL PRIMARY KEY,
    pqr_id          BIGINT NOT NULL REFERENCES pqr(id) ON DELETE CASCADE,
    estado_id       INTEGER NOT NULL REFERENCES estado_pqr(id),
    fecha_cambio    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    comentario      TEXT,
    usuario_cambio  VARCHAR(150) -- gestor o sistema que hizo el cambio
);

CREATE TABLE notificacion (
    id              BIGSERIAL PRIMARY KEY,
    pqr_id          BIGINT NOT NULL REFERENCES pqr(id) ON DELETE CASCADE,
    gestor_id       INTEGER REFERENCES gestor(id),
    tipo            VARCHAR(50) NOT NULL, -- 'vencimiento_48h', 'cambio_estado', etc.
    mensaje         TEXT NOT NULL,
    fecha_envio     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    enviado_ok      BOOLEAN NOT NULL DEFAULT FALSE
);

-- =====================================================================
-- MÓDULO 2: FACTURACIÓN AUTOMÁTICA
-- =====================================================================

CREATE TABLE cliente (
    id                  SERIAL PRIMARY KEY,
    tipo_documento      VARCHAR(5)  NOT NULL,
    numero_documento    VARCHAR(20) NOT NULL,
    nombre_completo     VARCHAR(150) NOT NULL,
    correo              VARCHAR(150),
    telefono            VARCHAR(20),
    UNIQUE (tipo_documento, numero_documento)
);

CREATE TABLE servicio (
    id          SERIAL PRIMARY KEY,
    nombre      VARCHAR(50) NOT NULL UNIQUE -- Acueducto, Alcantarillado, Energía, Aseo
);

CREATE TABLE tarifa (
    id                      SERIAL PRIMARY KEY,
    servicio_id             INTEGER NOT NULL REFERENCES servicio(id),
    nombre                  VARCHAR(100) NOT NULL,
    valor_por_unidad        NUMERIC(12,2) NOT NULL,
    cargo_fijo              NUMERIC(12,2) NOT NULL DEFAULT 0,
    fecha_vigencia_inicio   DATE NOT NULL,
    fecha_vigencia_fin      DATE -- NULL = vigente
);

CREATE TABLE contrato (
    id                  SERIAL PRIMARY KEY,
    numero_contrato     VARCHAR(30) NOT NULL UNIQUE,
    cliente_id          INTEGER NOT NULL REFERENCES cliente(id),
    servicio_id         INTEGER NOT NULL REFERENCES servicio(id),
    direccion_servicio  VARCHAR(200) NOT NULL,
    fecha_inicio        DATE NOT NULL,
    estado              VARCHAR(20) NOT NULL DEFAULT 'activo' -- activo, suspendido, inactivo
        CHECK (estado IN ('activo', 'suspendido', 'inactivo'))
);

CREATE INDEX idx_contrato_estado ON contrato(estado);

CREATE TABLE medidor (
    id                  SERIAL PRIMARY KEY,
    contrato_id         INTEGER NOT NULL REFERENCES contrato(id),
    numero_serie        VARCHAR(50) NOT NULL UNIQUE,
    fecha_instalacion   DATE NOT NULL,
    estado              VARCHAR(20) NOT NULL DEFAULT 'activo'
);

CREATE TABLE lectura_medidor (
    id                  BIGSERIAL PRIMARY KEY,
    medidor_id          INTEGER NOT NULL REFERENCES medidor(id),
    periodo             CHAR(7) NOT NULL, -- formato 'YYYY-MM'
    lectura_anterior    NUMERIC(12,2) NOT NULL,
    lectura_actual      NUMERIC(12,2) NOT NULL,
    consumo             NUMERIC(12,2) NOT NULL, -- lectura_actual - lectura_anterior
    fecha_lectura       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (medidor_id, periodo)
);

CREATE TABLE lote_facturacion (
    id                      SERIAL PRIMARY KEY,
    periodo                 CHAR(7) NOT NULL UNIQUE, -- 'YYYY-MM'
    fecha_inicio_proceso    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_fin_proceso       TIMESTAMP,
    estado                  VARCHAR(20) NOT NULL DEFAULT 'en_proceso'
        CHECK (estado IN ('en_proceso', 'completado', 'error')),
    total_facturas_generadas INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE factura (
    id                  BIGSERIAL PRIMARY KEY,
    numero_factura      VARCHAR(30) NOT NULL UNIQUE,
    lote_id             INTEGER NOT NULL REFERENCES lote_facturacion(id),
    contrato_id         INTEGER NOT NULL REFERENCES contrato(id),
    lectura_id          BIGINT NOT NULL REFERENCES lectura_medidor(id),
    tarifa_id           INTEGER NOT NULL REFERENCES tarifa(id),
    periodo             CHAR(7) NOT NULL,
    consumo             NUMERIC(12,2) NOT NULL,
    valor_consumo       NUMERIC(12,2) NOT NULL,
    cargo_fijo          NUMERIC(12,2) NOT NULL,
    subtotal            NUMERIC(12,2) NOT NULL,
    total               NUMERIC(12,2) NOT NULL,
    fecha_generacion    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_vencimiento   DATE NOT NULL,
    estado              VARCHAR(20) NOT NULL DEFAULT 'generada'
        CHECK (estado IN ('generada', 'sincronizada', 'error_sincronizacion', 'pagada'))
);

CREATE INDEX idx_factura_contrato_periodo ON factura(contrato_id, periodo);

CREATE TABLE sincronizacion_erp (
    id                  BIGSERIAL PRIMARY KEY,
    factura_id          BIGINT NOT NULL REFERENCES factura(id),
    fecha_sincronizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado              VARCHAR(20) NOT NULL CHECK (estado IN ('exitoso', 'fallido')),
    respuesta_erp       TEXT,
    intentos            SMALLINT NOT NULL DEFAULT 1
);
