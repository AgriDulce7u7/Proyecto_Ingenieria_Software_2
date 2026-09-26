package co.edu.uniquindio.epq.pqr.web;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import co.edu.uniquindio.epq.pqr.servicio.CatalogoPqrService;
import co.edu.uniquindio.epq.pqr.web.dto.ElementoCatalogoResponse;
import co.edu.uniquindio.epq.pqr.web.dto.GestorResponse;
import co.edu.uniquindio.epq.pqr.web.dto.NotificacionResponse;

@WebMvcTest(CatalogoPqrController.class)
@DisplayName("CatalogoPqrController - catálogos para formularios")
class CatalogoPqrControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private CatalogoPqrService catalogoService;

    @Test
    @DisplayName("GET /api/pqr/catalogos/tipos-solicitud: código para la API y nombre para mostrar")
    void tiposSolicitud() throws Exception {
        when(catalogoService.tiposSolicitud()).thenReturn(List.of(
                new ElementoCatalogoResponse(1, "PETICION", "Petición"),
                new ElementoCatalogoResponse(2, "QUEJA", "Queja"),
                new ElementoCatalogoResponse(3, "RECLAMO", "Reclamo")));

        mvc.perform(get("/api/pqr/catalogos/tipos-solicitud"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].codigo").value("PETICION"))
                .andExpect(jsonPath("$[0].nombre").value("Petición"));
    }

    @Test
    @DisplayName("GET /api/pqr/catalogos/canales y /estados: 200 con sus listas")
    void canalesYEstados() throws Exception {
        when(catalogoService.canales()).thenReturn(List.of(new ElementoCatalogoResponse(1, "WEB", "Web")));
        when(catalogoService.estados()).thenReturn(List.of(new ElementoCatalogoResponse(1, "RADICADO", "Radicado")));

        mvc.perform(get("/api/pqr/catalogos/canales"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value("WEB"));
        mvc.perform(get("/api/pqr/catalogos/estados"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value("RADICADO"));
    }

    @Test
    @DisplayName("GET /api/gestores: 200 con los gestores activos")
    void gestores() throws Exception {
        when(catalogoService.gestoresActivos()).thenReturn(List.of(
                new GestorResponse(1, "Laura Giraldo Ríos", "lgiraldo@epq.com.co", "Oficina de PQR", true)));

        mvc.perform(get("/api/gestores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Laura Giraldo Ríos"))
                .andExpect(jsonPath("$[0].activo").value(true));
    }

    @Test
    @DisplayName("GET /api/gestores/{id}/notificaciones: 200 con la bandeja del gestor")
    void notificacionesDeGestor() throws Exception {
        when(catalogoService.notificacionesDeGestor(1)).thenReturn(List.of(new NotificacionResponse(
                5L, "202609-0001", "ALERTA_VENCIMIENTO", "Laura Giraldo Ríos", "Vence en 48 h",
                LocalDateTime.of(2026, 9, 20, 10, 0, 30), true)));

        mvc.perform(get("/api/gestores/{id}/notificaciones", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].radicado").value("202609-0001"))
                .andExpect(jsonPath("$[0].enviado").value(true));
    }

    @Test
    @DisplayName("GET /api/gestores/{id}/notificaciones con id no numérico: 400 sin llamar al servicio")
    void gestorIdInvalido() throws Exception {
        mvc.perform(get("/api/gestores/{id}/notificaciones", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Solicitud inválida"));

        verify(catalogoService, never()).notificacionesDeGestor(any());
    }
}