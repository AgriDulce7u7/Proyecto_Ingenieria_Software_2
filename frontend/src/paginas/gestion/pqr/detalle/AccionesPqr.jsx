import { useState } from "react";
import { Boton } from "../../../../componentes/ui/Boton";
import { Dialogo } from "../../../../componentes/ui/Dialogo";
import { useCerrarPqr, useReasignarPqr, useResponderPqr, useTomarPqr } from "../../../../hooks/usePqr";
import { esPendiente } from "../../../../presentacion/estadosPqr";
import { gestorQueResponde, gestorQueToma } from "../../../../sesion/actuacionPqr";
import { useSesion } from "../../../../sesion/useSesion";
import { DialogoCerrar } from "./DialogoCerrar";
import { DialogoReasignar } from "./DialogoReasignar";
import { DialogoResponder } from "./DialogoResponder";

/**
 * Acciones del ciclo de vida de la PQR (DE-02). Los estados posibles salen de `accionesDisponibles`,
 * que calcula el backend: la interfaz no duplica la máquina de estados. Quién puede hacer cada acción
 * sale del rol del usuario que inició sesión (RNF-05):
 * - tomar y responder: el gestor a su nombre; el jefe y el administrador, a nombre de cualquier gestor;
 * - reasignar: solo el jefe de PQR y el administrador;
 * - cerrar: cualquier usuario que atiende PQR.
 */
export function AccionesPqr({ pqr, onResultado }) {
  const { usuario, puede } = useSesion();
  const [dialogo, setDialogo] = useState(null);
  const acciones = {
    tomar: useTomarPqr(pqr.radicado),
    reasignar: useReasignarPqr(pqr.radicado),
    responder: useResponderPqr(pqr.radicado),
    cerrar: useCerrarPqr(pqr.radicado),
  };

  const idParaTomar = gestorQueToma(usuario, pqr);
  const permite = {
    tomar: pqr.accionesDisponibles.includes("EN_TRAMITE") && idParaTomar !== null,
    responder: pqr.accionesDisponibles.includes("RESUELTO"),
    cerrar: pqr.accionesDisponibles.includes("CERRADO"),
    reasignar: esPendiente(pqr.estado) && puede("PQR_SUPERVISAR"),
  };
  // El administrador no tiene registro de gestor: una PQR sin asignar primero se reasigna
  const sinGestorParaTomar = pqr.accionesDisponibles.includes("EN_TRAMITE") && idParaTomar === null;

  if (!Object.values(permite).some(Boolean)) return null;

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
      { gestorId: idParaTomar },
      {
        onSuccess: (detalle) =>
          onResultado({
            tipo: "exito",
            mensaje: `La solicitud quedó en trámite a cargo de ${detalle.gestor?.nombre ?? "usted"}.`,
          }),
        onError: (error) => onResultado({ tipo: "error", error }),
      },
    );

  return (
    <div className="detalle-acciones">
      {permite.reasignar && (
        <Boton variante="secundario" onClick={() => abrir("reasignar")}>
          Reasignar
        </Boton>
      )}
      {permite.tomar && (
        <Boton onClick={tomar} disabled={acciones.tomar.isPending}>
          {acciones.tomar.isPending ? "Tomando…" : "Tomar solicitud"}
        </Boton>
      )}
      {permite.responder && <Boton onClick={() => abrir("responder")}>Responder solicitud</Boton>}
      {permite.cerrar && <Boton onClick={() => abrir("cerrar")}>Cerrar solicitud</Boton>}
      {sinGestorParaTomar && permite.reasignar && (
        <p className="detalle-acciones-nota">Para iniciar el trámite, asigne la solicitud a un gestor.</p>
      )}

      <Dialogo
        abierto={dialogo === "reasignar"}
        onCerrar={cerrarDialogo}
        antetitulo="Cambio de responsable"
        titulo="Reasignar PQR"
      >
        <DialogoReasignar
          pqr={pqr}
          usuario={usuario.usuario}
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
        {dialogo === "responder" && (
          <DialogoResponder
            pqr={pqr}
            gestorId={gestorQueResponde(usuario, pqr)}
            accion={acciones.responder}
            onListo={terminar}
            onCancelar={cerrarDialogo}
          />
        )}
      </Dialogo>
      <Dialogo abierto={dialogo === "cerrar"} onCerrar={cerrarDialogo} antetitulo={pqr.radicado} titulo="Cerrar PQR">
        <DialogoCerrar
          usuario={usuario.usuario}
          accion={acciones.cerrar}
          onListo={terminar}
          onCancelar={cerrarDialogo}
        />
      </Dialogo>
    </div>
  );
}