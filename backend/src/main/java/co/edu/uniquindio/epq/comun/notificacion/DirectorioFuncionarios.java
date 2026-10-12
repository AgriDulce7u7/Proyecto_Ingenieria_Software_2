package co.edu.uniquindio.epq.comun.notificacion;

import java.util.List;

/**
 * Puerto para obtener los funcionarios activos de un rol (DIP): el módulo de notificaciones no
 * depende del módulo de seguridad; este lo implementa.
 */
public interface DirectorioFuncionarios {

    List<DestinatarioInterno> activosConRol(String rol);
}
