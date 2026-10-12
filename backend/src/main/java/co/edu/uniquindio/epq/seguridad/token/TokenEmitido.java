package co.edu.uniquindio.epq.seguridad.token;

import java.time.Instant;

public record TokenEmitido(String token, Instant expiraEn) {
}
