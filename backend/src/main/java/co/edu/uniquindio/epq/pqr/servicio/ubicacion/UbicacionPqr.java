package co.edu.uniquindio.epq.pqr.servicio.ubicacion;

import co.edu.uniquindio.epq.comercial.dominio.Contrato;
import co.edu.uniquindio.epq.comun.territorio.Municipio;

/** Municipio y contrato asociados a una PQR (ambos opcionales). */
public record UbicacionPqr(Municipio municipio, Contrato contrato) {

    public static UbicacionPqr vacia() {
        return new UbicacionPqr(null, null);
    }
}
