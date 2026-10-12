package co.edu.uniquindio.epq.seguridad.web.dto;

import java.util.List;

public record RolResponse(String rol, String descripcion, boolean interno, boolean requiereGestorPqr, List<String> permisos) {
}
