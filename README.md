# Proyecto-Ingenier-a-de-Software-2
Proyecto basado en el proyecto del semestre pasado de la materia Ing. Software 1 que esta realizado para la empresa EPQ

## Base de datos

Este proyecto usa **PostgreSQL 16** corriendo en un contenedor de Docker. El esquema cubre las dos funcionalidades de esta entrega: **PQR** (registro y consulta de peticiones/quejas/reclamos) y **Facturación automática** de contratos activos.

### Requisitos

- Docker Desktop instalado
- (Opcional) DBeaver o pgAdmin para explorar la base de datos visualmente

### Cómo levantarla

1. Clona el repositorio y ubícate en la raíz del proyecto.
2. Copia el archivo de variables de entorno de ejemplo:
```bash
   cp .env.example .env
```
3. Abre `.env` y define tus propios valores para `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD` y `POSTGRES_PORT`.
4. Levanta el contenedor:
```bash
   docker compose up -d
```
5. Verifica que esté corriendo:
```bash
   docker ps
```
Deberías ver un contenedor llamado `epq_db` con estado `Up`.

Al crearse el contenedor por primera vez, Postgres ejecuta automáticamente `database/init/01_schema.sql` y crea todas las tablas.

### Conectarte con DBeaver

- Host: `localhost`
- Puerto: el que definiste en `POSTGRES_PORT` (por defecto `5432`)
- Base de datos: la que definiste en `POSTGRES_DB`
- Usuario/contraseña: los de tu `.env`

### Estructura relevante

```
database/
└── init/
    └── 01_schema.sql   # Esquema completo: PQR + Facturación
docker-compose.yml       # Definición del contenedor de Postgres
.env.example              # Plantilla de variables de entorno
```

### Módulos del esquema

- **PQR**: `tipo_solicitud`, `canal_atencion`, `estado_pqr`, `ciudadano`, `gestor`, `pqr`, `historial_pqr`, `notificacion`
- **Facturación**: `cliente`, `servicio`, `tarifa`, `contrato`, `medidor`, `lectura_medidor`, `lote_facturacion`, `factura`, `sincronizacion_erp`