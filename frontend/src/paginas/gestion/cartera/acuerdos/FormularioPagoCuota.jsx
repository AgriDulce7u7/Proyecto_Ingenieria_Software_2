import { useRef, useState } from "react";
import { MensajeError } from "../../../../componentes/MensajeError";
import { enfocarPrimerError } from "../../../../componentes/enfocarPrimerError";
import { Boton } from "../../../../componentes/ui/Boton";
import { Campo } from "../../../../componentes/ui/Campo";
import { usePagarCuota } from "../../../../hooks/useCartera";
import { formatearMoneda } from "../../../../presentacion/facturacion";
import { formatearFecha } from "../../../../presentacion/fechas";
import { SelectorCanal } from "../../pagos/SelectorCanal";

const MAXIMO_REFERENCIA = 100;

/**
 * Pago de una cuota de un acuerdo (CU-08). Con `cuota` (desde el detalle del acuerdo) muestra su valor y
 * vencimiento; sin ella (pago en ventanilla del asesor) pide el número del acuerdo y de la cuota. Las cuotas
 * se pagan en orden: si se indica otra, el backend responde cuál sigue.
 */
export function FormularioPagoCuota({ numeroAcuerdo = "", cuota = null, onPagado, onCancelar, className = "" }) {
  const formulario = useRef(null);
  const pago = usePagarCuota();
  const [datos, setDatos] = useState({
    numeroAcuerdo,
    numeroCuota: cuota ? String(cuota.numeroCuota) : "",
    canal: "PRESENCIAL",
    referencia: "",
  });
  const [errores, setErrores] = useState({});
  const pideIdentificacion = !cuota;

  const cambiar = (campo, valor) => {
    setDatos((anteriores) => ({ ...anteriores, [campo]: valor }));
    setErrores((anteriores) => ({ ...anteriores, [campo]: null }));
  };

  const pagar = (evento) => {
    evento.preventDefault();
    const erroresCliente = {};
    if (!datos.numeroAcuerdo.trim()) erroresCliente.numeroAcuerdo = "Escriba el número del acuerdo.";
    if (!(Number(datos.numeroCuota) >= 1)) erroresCliente.numeroCuota = "Indique el número de la cuota.";
    if (!datos.canal) erroresCliente.canal = "Seleccione el canal del pago.";
    setErrores(erroresCliente);
    if (Object.keys(erroresCliente).length > 0) {
      enfocarPrimerError(formulario.current);
      return;
    }
    const numeroCuota = Number(datos.numeroCuota);
    pago.mutate(
      {
        numeroAcuerdo: datos.numeroAcuerdo.trim().toUpperCase(),
        numeroCuota,
        canal: datos.canal,
        referencia: datos.referencia.trim() || null,
        valor: cuota ? cuota.valor : undefined,
      },
      { onSuccess: (acuerdo) => onPagado(acuerdo, numeroCuota) },
    );
  };

  // Los errores de canal y referencia van en su campo; los demás (cuota fuera de orden, acuerdo inexistente) al final
  const enCampos = pago.error?.errores?.canal || pago.error?.errores?.referencia;
  const errorGeneral =
    pago.error && !enCampos && pideIdentificacion && pago.error.noEncontrado
      ? { detalle: `No existe el acuerdo ${datos.numeroAcuerdo.trim().toUpperCase()}.` }
      : pago.error && !enCampos
        ? pago.error
        : null;
  const textoBoton = cuota ? `Registrar pago de ${formatearMoneda(cuota.valor)}` : "Registrar pago de la cuota";

  return (
    <form ref={formulario} className={`dialogo-formulario ${className}`.trim()} onSubmit={pagar} noValidate>
      {cuota ? (
        <div className="pago-valor">
          <span>
            Cuota {cuota.numeroCuota} · vence el {formatearFecha(cuota.fechaVencimiento)}
          </span>
          <strong className="cifra">{formatearMoneda(cuota.valor)}</strong>
          <small>Las cuotas se pagan en orden y por su valor completo.</small>
        </div>
      ) : (
        <div className="cuota-identificacion">
          <Campo
            etiqueta="Número del acuerdo"
            obligatorio
            className="cifra"
            placeholder="AP-2026-0001"
            autoComplete="off"
            autoCapitalize="characters"
            value={datos.numeroAcuerdo}
            error={errores.numeroAcuerdo}
            onChange={(evento) => cambiar("numeroAcuerdo", evento.target.value)}
          />
          <Campo
            etiqueta="Número de cuota"
            obligatorio
            type="number"
            min={1}
            inputMode="numeric"
            value={datos.numeroCuota}
            error={errores.numeroCuota}
            ayuda="Está en el plan de cuotas que recibió el usuario."
            onChange={(evento) => cambiar("numeroCuota", evento.target.value)}
          />
        </div>
      )}
      <SelectorCanal
        valor={datos.canal}
        error={errores.canal ?? pago.error?.errores?.canal}
        onCambiar={(valor) => cambiar("canal", valor)}
      />
      <Campo
        etiqueta={datos.canal === "BANCO" ? "Número de la consignación" : "Número del recibo de caja"}
        maxLength={MAXIMO_REFERENCIA}
        autoComplete="off"
        ayuda="Opcional. Sirve para conciliar el pago."
        value={datos.referencia}
        error={pago.error?.errores?.referencia}
        onChange={(evento) => cambiar("referencia", evento.target.value)}
      />
      <MensajeError error={errorGeneral} />
      <div className="dialogo-acciones">
        {onCancelar && (
          <Boton variante="discreto" onClick={onCancelar}>
            Cancelar
          </Boton>
        )}
        <Boton type="submit" icono="check" disabled={pago.isPending}>
          {pago.isPending ? "Registrando…" : textoBoton}
        </Boton>
      </div>
    </form>
  );
}