import { useState } from "react";
import { Link, useNavigate, useSearchParams } from "react-router-dom";
import { Cargando } from "../../../../componentes/Cargando";
import { EncabezadoGestion } from "../../../../componentes/EncabezadoGestion";
import { MensajeError } from "../../../../componentes/MensajeError";
import { enfocarPrimerError } from "../../../../componentes/enfocarPrimerError";
import { PieFormulario, SeccionFormulario } from "../../../../componentes/SeccionFormulario";
import { Boton } from "../../../../componentes/ui/Boton";
import { Campo } from "../../../../componentes/ui/Campo";
import { EnlaceVolver } from "../../../../componentes/ui/EnlaceVolver";
import { useAcuerdos, useRegistrarAcuerdo, useSimulacionAcuerdo } from "../../../../hooks/useCartera";
import { CUOTAS_MAXIMAS, CUOTAS_MINIMAS } from "../../../../presentacion/cartera";
import { formatearMoneda } from "../../../../presentacion/facturacion";
import { aFechaIso } from "../../../../presentacion/supervisionPqr";
import { TablaCuotas } from "./TablaCuotas";
import "../../pagos/pagos.css";
import "../cartera.css";

const MAXIMO_OBSERVACION = 500;
const OPCIONES_CUOTAS = Array.from({ length: CUOTAS_MAXIMAS - CUOTAS_MINIMAS + 1 }, (_, i) => CUOTAS_MINIMAS + i);

/** Mañana en formato ISO: la primera cuota debe vencer después de hoy. */
function manana() {
  const fecha = new Date();
  fecha.setDate(fecha.getDate() + 1);
  return aFechaIso(fecha);
}

/** Búsqueda del contrato por número. El número queda en la URL (?contrato=...). */
function BuscarContrato({ inicial, onBuscar }) {
  const [numero, setNumero] = useState(inicial);
  const [error, setError] = useState("");

  const buscar = (evento) => {
    evento.preventDefault();
    const valor = numero.trim().toUpperCase();
    if (!valor) {
      setError("Escriba el número del contrato.");
      enfocarPrimerError(evento.currentTarget);
      return;
    }
    onBuscar(valor);
  };

  return (
    <form className="pago-buscar formulario-campo-completo" onSubmit={buscar} noValidate role="search">
      <Campo
        etiqueta="Número de contrato"
        obligatorio
        className="cifra"
        placeholder="CT-ACU-0004"
        autoComplete="off"
        autoCapitalize="characters"
        spellCheck={false}
        value={numero}
        error={error}
        onChange={(evento) => {
          setNumero(evento.target.value);
          setError("");
        }}
      />
      <Boton type="submit" variante="secundario" icono="buscar">
        Buscar
      </Boton>
    </form>
  );
}

/** Deuda en mora del contrato y facturas que entran al acuerdo, según la simulación del backend. */
function DeudaDelContrato({ plan }) {
  return (
    <div className="pago-factura formulario-campo-completo">
      <div className="pago-factura-datos">
        <span className="antetitulo">Contrato {plan.numeroContrato}</span>
        <strong>Facturas vencidas que entran al acuerdo</strong>
        <ul className="acuerdo-facturas">
          {plan.facturasIncluidas.map((numero) => (
            <li key={numero}>
              <Link to={`/gestion/facturacion/facturas/${numero}`} className="pagos-enlace cifra">
                {numero}
              </Link>
            </li>
          ))}
        </ul>
      </div>
      <dl className="pago-factura-total">
        <div>
          <dt>Deuda total</dt>
          <dd className="cifra">{formatearMoneda(plan.totalDeuda)}</dd>
        </div>
      </dl>
    </div>
  );
}

/**
 * Plan de cuotas y registro. Sin elegir nada, el backend calcula el número de cuotas y la primera fecha
 * (SWR-30); si se cambian, el plan se vuelve a simular antes de registrar.
 */
function PlanAcuerdo({
  contrato,
  plan,
  cuotasAutomaticas,
  opciones,
  simulando,
  errorSimulacion,
  onCambiarOpciones,
}) {
  const navegar = useNavigate();
  const registro = useRegistrarAcuerdo();
  const [observacion, setObservacion] = useState("");

  const registrar = (evento) => {
    evento.preventDefault();
    registro.mutate(
      {
        numeroContrato: contrato,
        numeroCuotas: opciones.numeroCuotas ? Number(opciones.numeroCuotas) : undefined,
        fechaPrimeraCuota: opciones.fechaPrimeraCuota || undefined,
        observacion: observacion.trim() || undefined,
      },
      {
        onSuccess: (acuerdo) =>
          navegar(`/gestion/cartera/acuerdos/${acuerdo.numeroAcuerdo}`, { replace: true, state: { creado: true } }),
      },
    );
  };

  return (
    <form onSubmit={registrar} noValidate>
      <SeccionFormulario
        numero={2}
        titulo="Plan de cuotas"
        descripcion={`Cuotas mensuales iguales, entre ${CUOTAS_MINIMAS} y ${CUOTAS_MAXIMAS}. Si un vencimiento cae en día no hábil, pasa al siguiente.`}
      >
        <Campo
          etiqueta="Número de cuotas"
          como="select"
          value={opciones.numeroCuotas}
          ayuda="Automático: según la cuota mínima configurada en EPQ."
          onChange={(evento) => onCambiarOpciones({ numeroCuotas: evento.target.value })}
        >
          <option value="">Automático ({cuotasAutomaticas} cuotas)</option>
          {OPCIONES_CUOTAS.map((cuotas) => (
            <option key={cuotas} value={cuotas}>
              {cuotas} cuotas
            </option>
          ))}
        </Campo>
        <Campo
          etiqueta="Vencimiento de la primera cuota"
          type="date"
          min={manana()}
          value={opciones.fechaPrimeraCuota}
          ayuda="Si se deja vacía, vence a los 30 días."
          onChange={(evento) => onCambiarOpciones({ fechaPrimeraCuota: evento.target.value })}
        />
        <div className="formulario-campo-completo">
          {errorSimulacion ? (
            <MensajeError error={errorSimulacion} />
          ) : (
            <div className={simulando ? "acuerdo-plan acuerdo-plan-actualizando" : "acuerdo-plan"} aria-live="polite">
              <TablaCuotas cuotas={plan.cuotas} simulacion />
            </div>
          )}
        </div>
        <Campo
          etiqueta="Observación"
          como="textarea"
          rows={3}
          maxLength={MAXIMO_OBSERVACION}
          className="formulario-campo-completo"
          placeholder="Por ejemplo: el usuario se compromete a pagar en la oficina de Montenegro."
          contador={`${observacion.length} / ${MAXIMO_OBSERVACION}`}
          value={observacion}
          onChange={(evento) => setObservacion(evento.target.value)}
        />
      </SeccionFormulario>

      <PieFormulario>
        <MensajeError error={registro.error} />
        <div className="formulario-pie-acciones">
          <Boton a="/gestion/cartera/acuerdos" variante="discreto">
            Cancelar
          </Boton>
          <Boton type="submit" icono="check" disabled={registro.isPending || simulando || Boolean(errorSimulacion)}>
            {registro.isPending ? "Registrando…" : `Registrar acuerdo por ${formatearMoneda(plan.totalDeuda)}`}
          </Boton>
        </div>
      </PieFormulario>
    </form>
  );
}

/**
 * Estado del contrato elegido: si ya tiene un acuerdo vigente y la simulación del plan (SWR-30) con las
 * opciones que elija el gestor. Sin opciones, el backend calcula cuotas y fechas. Si las opciones no son
 * válidas (por ejemplo, una fecha pasada), se muestra el error sin perder la deuda ni el formulario.
 */
function useAcuerdoParaContrato(contrato) {
  const [opciones, setOpciones] = useState({ numeroCuotas: "", fechaPrimeraCuota: "" });
  const vigentes = useAcuerdos({ estado: "VIGENTE", contrato });
  // Simulación con los valores por defecto: dice si el contrato existe y tiene mora, y da la deuda
  const base = useSimulacionAcuerdo({ contrato });
  // Simulación con las opciones elegidas (la misma consulta que la base mientras no se elija nada)
  const simulacion = useSimulacionAcuerdo({
    contrato,
    numeroCuotas: opciones.numeroCuotas || undefined,
    fechaPrimeraCuota: opciones.fechaPrimeraCuota || undefined,
  });
  return {
    opciones,
    cambiarOpciones: (cambio) => setOpciones((anteriores) => ({ ...anteriores, ...cambio })),
    cargando: vigentes.isPending || base.isPending,
    vigente: vigentes.data?.[0] ?? null,
    base: base.data,
    errorContrato: base.error,
    plan: simulacion.data ?? base.data,
    simulando: simulacion.isFetching,
    errorPlan: simulacion.error,
  };
}

/** Lo que va en la sección 1 bajo la búsqueda: la deuda, o por qué no se puede crear el acuerdo. */
function EstadoContrato({ contrato, estado }) {
  if (estado.cargando) {
    return (
      <div className="formulario-campo-completo">
        <Cargando texto="Calculando la deuda del contrato…" />
      </div>
    );
  }
  if (estado.vigente) {
    return (
      <div className="aviso aviso-info formulario-campo-completo" role="status">
        <p>
          El contrato ya tiene el acuerdo vigente{" "}
          <Link to={`/gestion/cartera/acuerdos/${estado.vigente.numeroAcuerdo}`} className="cifra">
            {estado.vigente.numeroAcuerdo}
          </Link>
          . Solo puede haber uno a la vez.
        </p>
      </div>
    );
  }
  if (estado.errorContrato) {
    return (
      <div className="aviso aviso-error formulario-campo-completo" role="alert">
        <p>
          {estado.errorContrato.noEncontrado ? (
            <>
              No existe el contrato <strong className="cifra">{contrato}</strong>.
            </>
          ) : (
            estado.errorContrato.detalle
          )}
        </p>
      </div>
    );
  }
  return estado.base ? <DeudaDelContrato plan={estado.base} /> : null;
}

/** Secciones del formulario cuando ya hay un contrato elegido. */
function FormularioConContrato({ contrato, onBuscar }) {
  const estado = useAcuerdoParaContrato(contrato);
  const puedePlanear = !estado.cargando && !estado.vigente && !estado.errorContrato && estado.base;

  return (
    <>
      <SeccionFormulario numero={1} titulo="Contrato" descripcion="Busque el contrato con facturas vencidas.">
        <BuscarContrato inicial={contrato} onBuscar={onBuscar} />
        <EstadoContrato contrato={contrato} estado={estado} />
      </SeccionFormulario>
      {puedePlanear && (
        <PlanAcuerdo
          contrato={contrato}
          plan={estado.plan}
          cuotasAutomaticas={estado.base.numeroCuotas}
          opciones={estado.opciones}
          simulando={estado.simulando}
          errorSimulacion={estado.errorPlan}
          onCambiarOpciones={estado.cambiarOpciones}
        />
      )}
    </>
  );
}

/**
 * Registro de un acuerdo de pago (CU-08, RF-12, SWR-30, RN-07) para las facturas vencidas de un contrato.
 * Llega con ?contrato=... desde la cartera morosa o se busca aquí por número.
 */
export function RegistrarAcuerdo() {
  const [parametros, setParametros] = useSearchParams();
  const contrato = parametros.get("contrato") ?? "";

  return (
    <>
      <div className="detalle-volver-cartera">
        <EnlaceVolver a="/gestion/cartera/acuerdos" historial>
          Volver
        </EnlaceVolver>
      </div>
      <EncabezadoGestion
        antetitulo="Cobro persuasivo"
        titulo="Nuevo acuerdo de pago"
        descripcion="Incluye todas las facturas vencidas del contrato. Al registrarlo, el cobro coercitivo se suspende y el contrato deja de ser candidato a suspensión."
      />

      <div className="formulario-gestion">
        {contrato ? (
          <FormularioConContrato
            key={contrato}
            contrato={contrato}
            onBuscar={(valor) => setParametros({ contrato: valor }, { replace: true })}
          />
        ) : (
          <SeccionFormulario numero={1} titulo="Contrato" descripcion="Busque el contrato con facturas vencidas.">
            <BuscarContrato inicial="" onBuscar={(valor) => setParametros({ contrato: valor })} />
          </SeccionFormulario>
        )}
      </div>
    </>
  );
}