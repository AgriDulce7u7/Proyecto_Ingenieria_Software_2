package co.edu.uniquindio.epq.pqr.dominio;

import static co.edu.uniquindio.epq.pqr.DatosPruebaPqr.FECHA_RECEPCION;
import static co.edu.uniquindio.epq.pqr.DatosPruebaPqr.RADICADO;
import static co.edu.uniquindio.epq.pqr.DatosPruebaPqr.canalWeb;
import static co.edu.uniquindio.epq.pqr.DatosPruebaPqr.ciudadano;
import static co.edu.uniquindio.epq.pqr.DatosPruebaPqr.estado;
import static co.edu.uniquindio.epq.pqr.DatosPruebaPqr.gestor;
import static co.edu.uniquindio.epq.pqr.DatosPruebaPqr.pqrEn;
import static co.edu.uniquindio.epq.pqr.DatosPruebaPqr.pqrRadicada;
import static co.edu.uniquindio.epq.pqr.DatosPruebaPqr.tipoSolicitud;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import co.edu.uniquindio.epq.comun.excepcion.ReglaNegocioException;
import co.edu.uniquindio.epq.comun.excepcion.TransicionEstadoInvalidaException;

@DisplayName("Pqr - reglas del agregado")
class PqrTest {

    private static final PlazoRespuesta PLAZO =
            new PlazoRespuesta(LocalDateTime.of(2026, 9, 22, 23, 59), LocalDate.of(2026, 9, 22));
    private static final LocalDateTime FECHA_RESPUESTA = LocalDateTime.of(2026, 9, 10, 14, 30);
    private static final String RESPUESTA = "Se revisó la lectura del medidor y se ajustará la factura.";

    private final Gestor laura = gestor(1, "Laura Giraldo", true);
    private final Gestor andres = gestor(2, "Andres Ocampo", true);

    // ------------------------------------------------------------------ radicar

    @Nested
    @DisplayName("radicar")
    class Radicar {

        @Test
        @DisplayName("Crea la PQR en estado Radicado con plazo calculado, sin gestor ni respuesta")
        void creaPqrRadicada() {
            Pqr pqr = Pqr.radicar(RADICADO, ciudadano(), tipoSolicitud("Reclamo"), canalWeb(),
                    estado(EstadoPqrTipo.RADICADO), "Cobro elevado", "Descripción del reclamo.", FECHA_RECEPCION, PLAZO);

            assertAll(
                    () -> assertEquals(RADICADO, pqr.getRadicado()),
                    () -> assertEquals(EstadoPqrTipo.RADICADO, pqr.getEstadoTipo()),
                    () -> assertEquals(FECHA_RECEPCION, pqr.getFechaRecepcion()),
                    () -> assertEquals(PLAZO.fechaLimite(), pqr.getFechaLimiteRespuesta()),
                    () -> assertEquals(PLAZO.fechaEstimada(), pqr.getFechaEstimadaRespuesta()),
                    () -> assertNull(pqr.getGestor()),
                    () -> assertNull(pqr.getRespuesta()),
                    () -> assertNull(pqr.getFechaResolucion()),
                    () -> assertFalse(pqr.isNotificadoVencimiento()));
        }

        @Test
        @DisplayName("El canal es opcional")
        void canalOpcional() {
            Pqr pqr = Pqr.radicar(RADICADO, ciudadano(), tipoSolicitud("Queja"), null,
                    estado(EstadoPqrTipo.RADICADO), "Asunto", "Descripción.", FECHA_RECEPCION, PLAZO);

            assertNull(pqr.getCanal());
        }

        @ParameterizedTest(name = "Estado inicial {0} → IllegalArgumentException")
        @EnumSource(value = EstadoPqrTipo.class, names = "RADICADO", mode = EnumSource.Mode.EXCLUDE)
        @DisplayName("Rechaza un estado inicial distinto de Radicado")
        void rechazaEstadoInicialDistinto(EstadoPqrTipo tipo) {
            assertThrows(IllegalArgumentException.class, () -> Pqr.radicar(RADICADO, ciudadano(),
                    tipoSolicitud("Reclamo"), canalWeb(), estado(tipo), "Asunto", "Descripción.", FECHA_RECEPCION, PLAZO));
        }

        @Test
        @DisplayName("Rechaza datos obligatorios nulos")
        void rechazaDatosObligatoriosNulos() {
            EstadoPqr radicado = estado(EstadoPqrTipo.RADICADO);
            assertAll(
                    () -> assertThrows(NullPointerException.class, () -> Pqr.radicar(null, ciudadano(),
                            tipoSolicitud("Reclamo"), null, radicado, "Asunto", "Descripción.", FECHA_RECEPCION, PLAZO)),
                    () -> assertThrows(NullPointerException.class, () -> Pqr.radicar(RADICADO, null,
                            tipoSolicitud("Reclamo"), null, radicado, "Asunto", "Descripción.", FECHA_RECEPCION, PLAZO)),
                    () -> assertThrows(NullPointerException.class, () -> Pqr.radicar(RADICADO, ciudadano(),
                            null, null, radicado, "Asunto", "Descripción.", FECHA_RECEPCION, PLAZO)),
                    () -> assertThrows(NullPointerException.class, () -> Pqr.radicar(RADICADO, ciudadano(),
                            tipoSolicitud("Reclamo"), null, radicado, null, "Descripción.", FECHA_RECEPCION, PLAZO)),
                    () -> assertThrows(NullPointerException.class, () -> Pqr.radicar(RADICADO, ciudadano(),
                            tipoSolicitud("Reclamo"), null, radicado, "Asunto", null, FECHA_RECEPCION, PLAZO)),
                    () -> assertThrows(NullPointerException.class, () -> Pqr.radicar(RADICADO, ciudadano(),
                            tipoSolicitud("Reclamo"), null, radicado, "Asunto", "Descripción.", null, PLAZO)));
        }
    }

    // ------------------------------------------------------------------ asignarA

    @Nested
    @DisplayName("asignarA")
    class AsignarA {

        @ParameterizedTest(name = "Estado {0} → se asigna")
        @EnumSource(value = EstadoPqrTipo.class, names = {"RADICADO", "EN_TRAMITE", "VENCIDO"})
        @DisplayName("Asigna el gestor mientras la PQR está pendiente de respuesta")
        void asignaEnEstadosPendientes(EstadoPqrTipo tipo) {
            Pqr pqr = pqrEn(tipo, laura);

            pqr.asignarA(andres);

            assertSame(andres, pqr.getGestor());
        }

        @ParameterizedTest(name = "Estado {0} → ReglaNegocioException")
        @EnumSource(value = EstadoPqrTipo.class, names = {"RESUELTO", "CERRADO"})
        @DisplayName("No permite asignar una PQR ya resuelta o cerrada")
        void rechazaEstadosFinales(EstadoPqrTipo tipo) {
            Pqr pqr = pqrEn(tipo, laura);

            assertThrows(ReglaNegocioException.class, () -> pqr.asignarA(andres));
            assertSame(laura, pqr.getGestor());
        }

        @Test
        @DisplayName("No permite asignar un gestor inactivo")
        void rechazaGestorInactivo() {
            Pqr pqr = pqrRadicada();

            assertThrows(ReglaNegocioException.class, () -> pqr.asignarA(gestor(4, "Jorge Arango", false)));
            assertNull(pqr.getGestor());
        }

        @Test
        @DisplayName("Rechaza un gestor nulo")
        void rechazaGestorNulo() {
            assertThrows(NullPointerException.class, () -> pqrRadicada().asignarA(null));
        }
    }

    // ------------------------------------------------------------------ iniciarTramite

    @Nested
    @DisplayName("iniciarTramite")
    class IniciarTramite {

        @ParameterizedTest(name = "Desde {0} → En trámite")
        @EnumSource(value = EstadoPqrTipo.class, names = {"RADICADO", "VENCIDO"})
        @DisplayName("Pasa a En trámite y asigna al gestor responsable")
        void iniciaTramite(EstadoPqrTipo tipo) {
            Pqr pqr = pqrEn(tipo, null);

            pqr.iniciarTramite(estado(EstadoPqrTipo.EN_TRAMITE), laura);

            assertEquals(EstadoPqrTipo.EN_TRAMITE, pqr.getEstadoTipo());
            assertSame(laura, pqr.getGestor());
        }

        @ParameterizedTest(name = "Desde {0} → TransicionEstadoInvalidaException")
        @EnumSource(value = EstadoPqrTipo.class, names = {"EN_TRAMITE", "RESUELTO", "CERRADO"})
        @DisplayName("Rechaza iniciar trámite desde estados que no lo permiten")
        void rechazaTransicionInvalida(EstadoPqrTipo tipo) {
            Pqr pqr = pqrEn(tipo, laura);

            assertThrows(TransicionEstadoInvalidaException.class,
                    () -> pqr.iniciarTramite(estado(EstadoPqrTipo.EN_TRAMITE), andres));
            assertEquals(tipo, pqr.getEstadoTipo());
            assertSame(laura, pqr.getGestor());
        }

        @Test
        @DisplayName("Gestor inactivo: lanza ReglaNegocioException y la PQR no cambia de estado")
        void rechazaGestorInactivoSinCambiarEstado() {
            Pqr pqr = pqrEn(EstadoPqrTipo.RADICADO, null);

            assertThrows(ReglaNegocioException.class,
                    () -> pqr.iniciarTramite(estado(EstadoPqrTipo.EN_TRAMITE), gestor(4, "Jorge Arango", false)));
            assertEquals(EstadoPqrTipo.RADICADO, pqr.getEstadoTipo());
            assertNull(pqr.getGestor());
        }

        @Test
        @DisplayName("Gestor nulo: lanza NullPointerException y la PQR no cambia de estado")
        void rechazaGestorNuloSinCambiarEstado() {
            Pqr pqr = pqrEn(EstadoPqrTipo.RADICADO, null);

            assertThrows(NullPointerException.class,
                    () -> pqr.iniciarTramite(estado(EstadoPqrTipo.EN_TRAMITE), null));
            assertEquals(EstadoPqrTipo.RADICADO, pqr.getEstadoTipo());
        }
    }

    // ------------------------------------------------------------------ registrarRespuesta

    @Nested
    @DisplayName("registrarRespuesta")
    class RegistrarRespuesta {

        @ParameterizedTest(name = "Desde {0} → Resuelto")
        @EnumSource(value = EstadoPqrTipo.class, names = {"EN_TRAMITE", "VENCIDO"})
        @DisplayName("Resuelve la PQR guardando la respuesta recortada y la fecha")
        void registraRespuesta(EstadoPqrTipo tipo) {
            Pqr pqr = pqrEn(tipo, laura);

            pqr.registrarRespuesta("  " + RESPUESTA + "  ", estado(EstadoPqrTipo.RESUELTO), FECHA_RESPUESTA);

            assertAll(
                    () -> assertEquals(EstadoPqrTipo.RESUELTO, pqr.getEstadoTipo()),
                    () -> assertEquals(RESPUESTA, pqr.getRespuesta()),
                    () -> assertEquals(FECHA_RESPUESTA, pqr.getFechaResolucion()));
        }

        @ParameterizedTest(name = "Respuesta \"{0}\" → ReglaNegocioException")
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t\n"})
        @DisplayName("RN-06: exige una respuesta formal no vacía")
        void exigeRespuesta(String texto) {
            Pqr pqr = pqrEn(EstadoPqrTipo.EN_TRAMITE, laura);

            assertThrows(ReglaNegocioException.class,
                    () -> pqr.registrarRespuesta(texto, estado(EstadoPqrTipo.RESUELTO), FECHA_RESPUESTA));
            assertEquals(EstadoPqrTipo.EN_TRAMITE, pqr.getEstadoTipo());
            assertNull(pqr.getFechaResolucion());
        }

        @ParameterizedTest(name = "Desde {0} → TransicionEstadoInvalidaException")
        @EnumSource(value = EstadoPqrTipo.class, names = {"RADICADO", "RESUELTO", "CERRADO"})
        @DisplayName("Rechaza responder desde estados que no admiten respuesta")
        void rechazaEstadosSinRespuesta(EstadoPqrTipo tipo) {
            Pqr pqr = pqrEn(tipo, laura);

            assertThrows(TransicionEstadoInvalidaException.class,
                    () -> pqr.registrarRespuesta(RESPUESTA, estado(EstadoPqrTipo.RESUELTO), FECHA_RESPUESTA));
            assertEquals(tipo, pqr.getEstadoTipo());
            assertNull(pqr.getRespuesta());
        }
    }

    // ------------------------------------------------------------------ cerrar y marcarVencida

    @Nested
    @DisplayName("cerrar y marcarVencida")
    class CerrarYVencer {

        @Test
        @DisplayName("Una PQR resuelta se puede cerrar")
        void cierraPqrResuelta() {
            Pqr pqr = pqrEn(EstadoPqrTipo.RESUELTO, laura);

            pqr.cerrar(estado(EstadoPqrTipo.CERRADO));

            assertEquals(EstadoPqrTipo.CERRADO, pqr.getEstadoTipo());
        }

        @ParameterizedTest(name = "Cerrar desde {0} → TransicionEstadoInvalidaException")
        @EnumSource(value = EstadoPqrTipo.class, names = "RESUELTO", mode = EnumSource.Mode.EXCLUDE)
        @DisplayName("Solo se puede cerrar desde Resuelto")
        void rechazaCierreSinResolver(EstadoPqrTipo tipo) {
            Pqr pqr = pqrEn(tipo, laura);

            assertThrows(TransicionEstadoInvalidaException.class, () -> pqr.cerrar(estado(EstadoPqrTipo.CERRADO)));
            assertEquals(tipo, pqr.getEstadoTipo());
        }

        @ParameterizedTest(name = "Vencer desde {0} → Vencido")
        @EnumSource(value = EstadoPqrTipo.class, names = {"RADICADO", "EN_TRAMITE"})
        @DisplayName("Se vence mientras el plazo está en curso")
        void marcaVencida(EstadoPqrTipo tipo) {
            Pqr pqr = pqrEn(tipo, laura);

            pqr.marcarVencida(estado(EstadoPqrTipo.VENCIDO));

            assertEquals(EstadoPqrTipo.VENCIDO, pqr.getEstadoTipo());
        }

        @ParameterizedTest(name = "Vencer desde {0} → TransicionEstadoInvalidaException")
        @EnumSource(value = EstadoPqrTipo.class, names = {"VENCIDO", "RESUELTO", "CERRADO"})
        @DisplayName("No se vence una PQR ya vencida, resuelta o cerrada")
        void rechazaVencimiento(EstadoPqrTipo tipo) {
            Pqr pqr = pqrEn(tipo, laura);

            assertThrows(TransicionEstadoInvalidaException.class,
                    () -> pqr.marcarVencida(estado(EstadoPqrTipo.VENCIDO)));
        }

        @Test
        @DisplayName("Rechaza un estado destino nulo")
        void rechazaEstadoNulo() {
            assertThrows(NullPointerException.class, () -> pqrEn(EstadoPqrTipo.RESUELTO, laura).cerrar(null));
        }
    }

    // ------------------------------------------------------------------ otros

    @Nested
    @DisplayName("alerta y responsable")
    class AlertaYResponsable {

        @Test
        @DisplayName("Marca la alerta de vencimiento como enviada")
        void marcaAlerta() {
            Pqr pqr = pqrRadicada();

            pqr.marcarAlertaVencimientoEnviada();

            assertTrue(pqr.isNotificadoVencimiento());
        }

        @Test
        @DisplayName("esGestionadaPor compara por id, no por instancia")
        void comparaPorId() {
            Pqr pqr = pqrEn(EstadoPqrTipo.EN_TRAMITE, laura);

            assertTrue(pqr.esGestionadaPor(gestor(1, "Laura Giraldo", true)));
            assertFalse(pqr.esGestionadaPor(andres));
        }

        @Test
        @DisplayName("esGestionadaPor es falso si no hay gestor o el candidato es nulo")
        void sinGestorOCandidatoNulo() {
            assertFalse(pqrRadicada().esGestionadaPor(laura));
            assertFalse(pqrEn(EstadoPqrTipo.EN_TRAMITE, laura).esGestionadaPor(null));
        }
    }
}