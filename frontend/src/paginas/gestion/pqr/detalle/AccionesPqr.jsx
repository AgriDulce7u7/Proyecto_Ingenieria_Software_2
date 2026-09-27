import { useState } from "react";
import { Boton } from "../../../../componentes/ui/Boton";
import { Dialogo } from "../../../../componentes/ui/Dialogo";
import { useCerrarPqr, useReasignarPqr, useResponderPqr, useTomarPqr } from "../../../../hooks/usePqr";
import { esPendiente } from "../../../../presentacion/estadosPqr";
import { useSesion } from "../../../../sesion/useSesion";
import { DialogoCerrar } from "./DialogoCerrar";
import { DialogoReasignar } from "./DialogoReasignar";
import { DialogoResponder } from "./DialogoResponder";

/**
 * Acciones del ciclo de vida de la PQR (DE-02). Los botones salen de `accionesDisponibles`,
 * que calcula el backend según el estado: la interfaz no duplica la máquina de estados.
 */
export function AccionesPqr({ pqr, onResultado }) {
  const { gestor, usuario } = useSesion();
  const [dialogo, setDialogo] = useState(null);
  const acciones = {
    tomar: useTomarPqr(pqr.radicado),
    reasignar: useReasignarPqr(pqr.radicado),
    responder: useResponderPqr(pqr.radicado),
    cerrar: useCerrarPqr(pqr.radicado),
  };

  const puede = {
    tomar: pqr.accionesDisponibles.includes("EN_TRAMITE"),
    responder: pqr.accionesDisponibles.includes("RESUELTO"),
    cerrar: pqr.accionesDisponibles.includes("CERRADO"),
    reasignar: esPendiente(pqr.estado),
  };

  if (!Object.values(puede).some(Boolean)) return null;

  const abrir = (nombre) => {
    acciones[nombre].reset();
    setDialogo(nombre);
  };
  const cerrarDialogo = () => setDialogo(null);
  const terminar = (mensaje) => {
    cerrarDialogo();
    onResultado({ tipo: "exito", mensaje });
  };

  const tomar = () =>
    acciones.tomar.mutate(
      { gestorId: gestor.id },
      {
        onSuccess: () => onResultado({ tipo: "exito", mensaje: "Tomó la solicitud: ahora está en trámite y a su cargo." }),
        onError: (error) => onResultado({ tipo: "error", error }),
      },
    );

  const sinGestor = !gestor;

  return (
    <div className="detalle-acciones">
      {puede.reasignar && (
        <Boton variante="secundario" onClick={() => abrir("reasignar")} disabled={sinGestor}>
          Reasignar
        </Boton>
      )}
      {puede.tomar && (
        <Boton onClick={tomar} disabled={sinGestor || acciones.tomar.isPending}>
          {acciones.tomar.isPending ? "Tomando…" : "Tomar solicitud"}
        </Boton>
      )}
      {puede.responder && (
        <Boton onClick={() => abrir("responder")} disabled={sinGestor}>
          Responder solicitud
        </Boton>
      )}
      {puede.cerrar && (
        <Boton onClick={() => abrir("cerrar")} disabled={sinGestor}>
          Cerrar solicitud
        </Boton>
      )}
      {sinGestor && (
        <p className="detalle-acciones-nota">Seleccione su nombre en Gestor actual para gestionar la solicitud.</p>
      )}

      <Dialogo
        abierto={dialogo === "reasignar"}
        onCerrar={cerrarDialogo}
        antetitulo="Cambio de responsable"
        titulo="Reasignar PQR"
      >
        <DialogoReasignar
          pqr={pqr}
          usuario={usuario}
          accion={acciones.reasignar}
          onListo={terminar}
          onCancelar={cerrarDialogo}
        />
      </Dialogo>
      <Dialogo
        abierto={dialogo === "responder"}
        onCerrar={cerrarDialogo}
        antetitulo={pqr.radicado}
        titulo="Responder solicitud"
      >
        {gestor && (
          <DialogoResponder
            pqr={pqr}
            gestor={gestor}
            accion={acciones.responder}
            onListo={terminar}
            onCancelar={cerrarDialogo}
          />
        )}
      </Dialogo>
      <Dialogo abierto={dialogo === "cerrar"} onCerrar={cerrarDialogo} antetitulo={pqr.radicado} titulo="Cerrar PQR">
        <DialogoCerrar usuario={usuario} accion={acciones.cerrar} onListo={terminar} onCancelar={cerrarDialogo} />
      </Dialogo>
    </div>
  );
}