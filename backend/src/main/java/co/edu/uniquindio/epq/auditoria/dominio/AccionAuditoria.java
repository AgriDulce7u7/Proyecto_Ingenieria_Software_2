package co.edu.uniquindio.epq.auditoria.dominio;

/**
 * Operaciones críticas auditadas (RF-15): inicio/cierre de sesión, creación/modificación de contratos,
 * generación y anulación de facturas, registro y respuesta de PQR y cambios en roles de usuario,
 * además de las operaciones de usuarios del servicio, pagos y cartera.
 */
public enum AccionAuditoria {

    // Seguridad (RF-14)
    INICIO_SESION(ModuloAuditoria.SEGURIDAD),
    INICIO_SESION_FALLIDO(ModuloAuditoria.SEGURIDAD),
    CUENTA_BLOQUEADA(ModuloAuditoria.SEGURIDAD),
    CIERRE_SESION(ModuloAuditoria.SEGURIDAD),
    ACTIVAR_CUENTA_PORTAL(ModuloAuditoria.SEGURIDAD),
    CREAR_FUNCIONARIO(ModuloAuditoria.SEGURIDAD),
    MODIFICAR_FUNCIONARIO(ModuloAuditoria.SEGURIDAD),
    CAMBIAR_ROL(ModuloAuditoria.SEGURIDAD),
    CAMBIAR_ESTADO_FUNCIONARIO(ModuloAuditoria.SEGURIDAD),
    RESTABLECER_CONTRASENA(ModuloAuditoria.SEGURIDAD),
    // F-03 Usuarios del servicio
    REGISTRAR_USUARIO(ModuloAuditoria.USUARIOS),
    MODIFICAR_USUARIO(ModuloAuditoria.USUARIOS),
    DESACTIVAR_USUARIO(ModuloAuditoria.USUARIOS),
    VALIDAR_IDENTIDAD_RNEC(ModuloAuditoria.USUARIOS),
    REGISTRAR_PREDIO(ModuloAuditoria.USUARIOS),
    // F-04 Contratos
    CREAR_CONTRATO(ModuloAuditoria.CONTRATOS),
    CAMBIAR_ESTADO_CONTRATO(ModuloAuditoria.CONTRATOS),
    ASIGNAR_MEDIDOR(ModuloAuditoria.CONTRATOS),
    // F-01 Facturación
    GENERAR_LOTE_FACTURAS(ModuloAuditoria.FACTURACION),
    ANULAR_FACTURA(ModuloAuditoria.FACTURACION),
    VENCER_FACTURAS(ModuloAuditoria.FACTURACION),
    // F-05 Pagos
    REGISTRAR_PAGO(ModuloAuditoria.PAGOS),
    PAGO_EN_LINEA_RECHAZADO(ModuloAuditoria.PAGOS),
    // F-02 PQR
    REGISTRAR_PQR(ModuloAuditoria.PQR),
    ASIGNAR_PQR(ModuloAuditoria.PQR),
    CAMBIAR_ESTADO_PQR(ModuloAuditoria.PQR),
    RESPONDER_PQR(ModuloAuditoria.PQR),
    VENCER_PQR(ModuloAuditoria.PQR),
    ADJUNTAR_EVIDENCIA_PQR(ModuloAuditoria.PQR),
    // F-07 Cartera
    REGISTRAR_ACUERDO_PAGO(ModuloAuditoria.CARTERA),
    PAGAR_CUOTA_ACUERDO(ModuloAuditoria.CARTERA),
    INCUMPLIR_ACUERDO_PAGO(ModuloAuditoria.CARTERA),
    CUMPLIR_ACUERDO_PAGO(ModuloAuditoria.CARTERA),
    REACTIVAR_COBRO_COERCITIVO(ModuloAuditoria.CARTERA),
    MARCAR_CANDIDATO_SUSPENSION(ModuloAuditoria.CARTERA),
    EXPORTAR_REPORTE_CARTERA(ModuloAuditoria.CARTERA);

    private final ModuloAuditoria modulo;

    AccionAuditoria(ModuloAuditoria modulo) {
        this.modulo = modulo;
    }

    public ModuloAuditoria modulo() {
        return modulo;
    }
}
