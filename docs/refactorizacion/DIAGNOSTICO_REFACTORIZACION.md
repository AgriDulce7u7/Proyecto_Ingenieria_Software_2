# Diagnóstico técnico — F-05 Registrar pago de factura

Taller de refactorización · Ingeniería de Software II · Universidad del Quindío

> **Convenciones.** Las rutas son relativas a `backend/src/main/java/co/edu/uniquindio/epq/`. Los números de
> línea corresponden al commit `e63671b` de la rama principal.

---

## 1. Funcionalidad seleccionada

| Campo | Valor |
|---|---|
| Equipo | Angelica María Reyes, Steven Severino, Andrés Felipe Zambrano |
| Repositorio | <https://github.com/AgriDulce7u7/Proyecto_Ingenieria_Software_2> |
| Funcionalidad | **F-05 Registrar pago de factura** (CU-04) |
| Requisitos | SWR-21 a SWR-24, RN-05 (la factura pasa a *Pagada* solo con confirmación), CO-02 (pago por el total, timeout y reintentos de la pasarela) |
| Arquitectura documentada | `ARQUITECTURA.md`: monolito modular con capas `web`, `servicio`, `dominio`, `repositorio`, `evento`, `tarea` (§5.4) |

**Por qué esta funcionalidad.** Es una transacción de negocio que involucra varias entidades (`Factura`, `Pago`,
`IntentoPagoEnLinea`) y un sistema externo (pasarela de pagos), y tiene dos flujos: pago presencial registrado
por un funcionario y pago en línea del ciudadano.

### Componentes que participan

| Capa | Componente | Responsabilidad |
|---|---|---|
| web | `pagos/web/PagoController` | `POST /api/pagos/presenciales` |
| web | `portal/web/PortalCiudadanoController` | `POST /api/portal/facturas/{numero}/pago-en-linea` |
| servicio | `pagos/servicio/impl/RegistroPagoServiceImpl` | Orquesta los dos flujos y registra los intentos en línea |
| servicio | `pagos/servicio/AplicadorPago` | En una transacción: bloquea la factura, crea el `Pago`, la marca pagada, audita y publica el evento |
| pasarela | `pagos/pasarela/ProcesadorTransaccionesPasarela` | Timeout por intento y reintentos (CO-02) |
| dominio | `facturacion/dominio/Factura` | `validarPagable()`, `registrarPago()` y máquina de estados de cobro |
| repositorio | `FacturaRepository`, `PagoRepository`, `IntentoPagoEnLineaRepository` | Persistencia (bloqueo pesimista sobre la factura) |

---

## 2. Cobertura de los hallazgos

| Aspecto pedido | H-01 | H-02 | H-03 |
|---|:---:|:---:|:---:|
| Arquitectura | ✔ | | |
| Principios SOLID | ✔ (DIP) | ✔ (SRP) | |
| Responsabilidades / separación de capas | ✔ | ✔ | ✔ |
| Código limpio / duplicación de código | | ✔ | ✔ |
| Acoplamiento | ✔ | | ✔ |

---

## 3. Hallazgos

### H-01 · El servicio de pagos depende de los DTO de la capa web

| Campo | Contenido |
|---|---|
| Tipo | Riesgo arquitectónico |
| Ubicación | `pagos/servicio/RegistroPagoService.java` (interfaz), l. 3-5, 13 y 16; `pagos/servicio/impl/RegistroPagoServiceImpl.java` l. 37-39; `pagos/servicio/PagosMapper.java` l. 128-130 |
| Criterio | Regla de dependencia entre capas (`ARQUITECTURA.md` §5.4); Principio de Inversión de Dependencias (DIP); acoplamiento |

**Evidencia** (`RegistroPagoService.java`):

```java
import co.edu.uniquindio.epq.pagos.web.dto.ComprobantePagoResponse;
import co.edu.uniquindio.epq.pagos.web.dto.PagoEnLineaRequest;
import co.edu.uniquindio.epq.pagos.web.dto.RegistrarPagoPresencialRequest;
...
ComprobantePagoResponse registrarPresencial(RegistrarPagoPresencialRequest solicitud);
ComprobantePagoResponse pagarEnLinea(String numeroFactura, PagoEnLineaRequest solicitud);
```

**Observación.** El contrato del caso de uso (la interfaz del servicio) recibe y devuelve objetos del paquete
`pagos.web.dto`. Al mismo tiempo, los controladores de `web` dependen del servicio. Resultado: `web → servicio`
y `servicio → web`, un ciclo entre capas.

**Justificación.**

1. **Contradice la arquitectura documentada.** `ARQUITECTURA.md` §5.4 dice que `web` "solo conoce `servicio`"
   y que `servicio` "depende de `dominio`, `repositorio` y puertos externos". La capa web no está en esa lista.
   Es una regla escrita por el propio equipo, no un criterio externo.
2. **Viola DIP.** El DIP pide que los módulos de alto nivel (el caso de uso "registrar pago") no dependan de
   detalles de bajo nivel (el formato HTTP/JSON de entrada y salida). Aquí la política depende del detalle:
   `PagoEnLineaRequest` y `RegistrarPagoPresencialRequest` llevan anotaciones de Bean Validation (`@NotBlank`,
   `@Pattern`, `@Digits`), que son validaciones de formato de la petición HTTP, y `ComprobantePagoResponse` es
   la forma exacta del JSON que recibe el cliente.
3. **Genera acoplamiento real, no teórico.** Cualquier cambio en el contrato JSON (renombrar un campo para el
   frontend, versionar la API) obliga a modificar la interfaz del servicio y su implementación. Si mañana el
   pago se registrara desde otro canal (una tarea programada, un archivo de recaudo bancario), ese canal tendría
   que construir objetos de la capa web para invocar el caso de uso.
4. **Es sistémico.** No es un descuido aislado: 56 archivos bajo `*/servicio` importan clases de `*.web.*`
   (auditoría, cartera, comercial, dashboard, facturación, pagos). Esto refuerza que es una decisión de diseño
   no alineada con la arquitectura, no un error puntual.

**Límite del hallazgo.** No produce fallos funcionales hoy: el sistema funciona correctamente. Es un riesgo de
mantenibilidad y evolución, por eso se clasifica como riesgo arquitectónico y no como defecto.

**Impacto y riesgo.** Medio. Afecta la capacidad de cambiar la API o reutilizar los casos de uso sin tocar la
lógica de negocio.

---

### H-02 · La regla "el pago debe ser por el total" está duplicada en dos servicios y fuera del dominio

| Campo | Contenido |
|---|---|
| Tipo | Deuda técnica |
| Ubicación | `pagos/servicio/AplicadorPago.java`, método `aplicar`, l. 56-60; `pagos/servicio/impl/RegistroPagoServiceImpl.java`, método `pagarEnLinea`, l. 95-99; comparar con `facturacion/dominio/Factura.java`, `validarPagable`, l. 163-174 |
| Criterio | Duplicación de código (DRY); responsabilidad mal ubicada (SRP / Experto en Información); regla de capas §5.4 |

**Evidencia.**

```java
// AplicadorPago.java:56-60
factura.validarPagable();   // SWR-23
if (valor.compareTo(factura.getTotal()) != 0) {
    throw new ReglaNegocioException("El valor %s no coincide con el total de la factura %s (%s)"
            .formatted(valor, factura.getNumeroFactura(), factura.getTotal()));
}
```

```java
// RegistroPagoServiceImpl.java:95-99
factura.validarPagable();                                   // SWR-23
BigDecimal monto = factura.getTotal();
if (s.valor() != null && s.valor().compareTo(monto) != 0) {  // CO-02: el monto coincide con el total
    throw new ReglaNegocioException("El valor a pagar debe ser el total de la factura: " + monto);
}
```

**Observación.** La regla de negocio CO-02 ("no se admiten pagos parciales") se implementa dos veces, en dos
clases de la capa de servicio, con dos mensajes de error diferentes. Las demás reglas que deciden si una factura
puede pagarse (pagada, anulada, incluida en un acuerdo) sí están en la entidad: `Factura.validarPagable()`.

**Justificación.**

1. **Duplicación con divergencia comprobada.** No es solo código repetido: las dos copias ya evolucionaron por
   separado (mensajes distintos). Si la política cambia (por ejemplo, aceptar abonos o una tolerancia por
   redondeo), hay que recordar cambiar los dos lugares; olvidar uno deja al pago presencial y al pago en línea
   con reglas diferentes.
2. **La regla está en la capa equivocada.** `ARQUITECTURA.md` §5.4 asigna al `dominio` "entidades, estados y
   reglas de negocio", y el Javadoc de `Factura` la describe como "entidad rica: controla sus propias
   transiciones". `Factura` tiene el dato (`total`) y ya concentra las demás reglas de cobro; según el principio
   de Experto en Información, es quien debe decidir si un valor es válido para pagarla.
3. **Responsabilidad única.** Los servicios tienen como responsabilidad orquestar (buscar la factura, llamar
   a la pasarela, persistir, auditar). Al decidir además la política de valores de una factura, asumen una razón
   de cambio que pertenece al dominio: un cambio en la política de pagos parciales obligaría a modificar dos
   orquestadores que no deberían conocerla.

**Matiz (evita sobredimensionar el hallazgo).** Que `pagarEnLinea` valide **antes** de cobrar la tarjeta es
correcto: falla temprano y evita un cobro que luego no podría aplicarse. El problema no es que se valide dos
veces, sino que la regla no tiene un único dueño.

**Impacto y riesgo.** Medio. Hoy ambas copias son consistentes en lógica; el riesgo aparece cuando la regla
cambie.

---

### H-03 · La capa de pasarela depende de la capa de servicio y se comunica con ella mediante un mapa sin tipo

| Campo | Contenido |
|---|---|
| Tipo | Oportunidad de mejora |
| Ubicación | `pagos/pasarela/ProcesadorTransaccionesPasarela.java` l. 14 y 58; `pagos/servicio/TimeoutPagoException.java`; `pagos/servicio/impl/RegistroPagoServiceImpl.java` l. 27, 36 y 112-114; `comun/excepcion/ExcepcionAplicacion.java` l. 27 y 31-38 |
| Criterio | Acoplamiento (Principio de Dependencias Acíclicas); separación de capas; código limpio (tipado explícito) |

**Evidencia.**

```java
// ProcesadorTransaccionesPasarela.java (paquete pasarela)
14  import co.edu.uniquindio.epq.pagos.servicio.TimeoutPagoException;
58  throw new TimeoutPagoException(maximo);
```

```java
// RegistroPagoServiceImpl.java (paquete servicio)
27  import co.edu.uniquindio.epq.pagos.pasarela.ProcesadorTransaccionesPasarela;
112 } catch (TimeoutPagoException ex) {
113     registrarIntento(factura, monto, ResultadoIntentoPago.SIN_RESPUESTA, null, null, ex.getMessage(),
114             tarjeta, (Integer) ex.propiedades().get("intentos"), usuario);
```

```java
// ExcepcionAplicacion.java (superclase de TimeoutPagoException)
27  public abstract HttpStatus estadoHttp();
31  /** Agrega un dato adicional que viajará en la respuesta de error (p. ej. el perfil existente). */
```

**Observación.** El paquete `pasarela` (adaptación del sistema externo) importa una excepción del paquete
`servicio`, mientras `servicio` importa `pasarela`: hay un ciclo de dependencias. Además, el número de intentos
realizados viaja desde el procesador hasta el servicio guardado en `propiedades()`, un `Map<String, Object>`
pensado para agregar datos a la respuesta HTTP de error, y se recupera con una clave de texto y un cast.

**Justificación.**

1. **Acoplamiento cíclico.** El Principio de Dependencias Acíclicas exige que el grafo de dependencias entre
   paquetes no tenga ciclos. Con el ciclo `servicio ↔ pasarela`, ninguno de los dos paquetes puede entenderse,
   probarse o reemplazarse de forma independiente. Esto debilita la decisión D3 de `ARQUITECTURA.md`
   ("integraciones detrás de interfaces"), cuyo propósito es aislar el sistema externo.
2. **La infraestructura queda acoplada a HTTP.** `TimeoutPagoException` hereda de `ExcepcionAplicacion`, que
   declara `estadoHttp()` (devuelve un `HttpStatus` de Spring Web). Por eso el procesador de la pasarela, que no
   debería saber nada de HTTP, lanza una excepción que conoce su código de respuesta (504). La traducción a
   HTTP corresponde a la capa `web` (§5.4: "códigos de error").
3. **Comunicación por un canal sin tipo (código limpio).** `(Integer) ex.propiedades().get("intentos")`
   depende de una cadena literal y de un cast que el compilador no verifica. Si alguien renombra la propiedad en
   el constructor de `TimeoutPagoException` o cambia su tipo, el error no aparece al compilar sino en tiempo de
   ejecución (`null` o `ClassCastException`), justo en la rama de timeout, que es la menos probada. Además, se
   reutiliza un mapa cuyo propósito documentado (l. 31) es la respuesta de error, no el paso de datos entre
   componentes.
4. **Dato potencialmente incorrecto.** Si el hilo se interrumpe (`InterruptedException`, l. 53-55), el ciclo
   se corta con `break` y la l. 58 lanza `TimeoutPagoException(maximo)`, reportando el máximo de intentos aunque
   se hayan hecho menos. Ese valor es el que termina guardado en `IntentoPagoEnLinea`.

**Límite del hallazgo.** El impacto funcional es bajo: el flujo normal funciona y el punto 4 solo ocurre ante una
interrupción del hilo. Por eso se clasifica como oportunidad de mejora y no como defecto.

**Impacto y riesgo.** Bajo en lo funcional, medio en mantenibilidad: el ciclo y el cast sin tipo hacen frágil
la rama de timeout.

---

## 4. Resumen

| ID | Hallazgo | Tipo | Criterio principal | Impacto |
|---|---|---|---|---|
| H-01 | El servicio depende de DTO de la capa web | Riesgo arquitectónico | Capas §5.4, DIP, acoplamiento | Medio |
| H-02 | Regla "pago por el total" duplicada y fuera del dominio | Deuda técnica | DRY, SRP / Experto en Información | Medio |
| H-03 | Ciclo `pasarela ↔ servicio` y datos por un mapa sin tipo | Oportunidad de mejora | Acoplamiento (ADP), capas, código limpio | Bajo |
