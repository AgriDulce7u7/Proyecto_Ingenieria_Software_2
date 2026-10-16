import { Icono } from "../../../componentes/ui/Icono";
import { CANALES_PRESENCIALES, presentarCanal } from "../../../presentacion/pagos";
import "./pagos.css";

/**
 * Canal de un pago registrado por un funcionario, como opciones de radio: caja (PRESENCIAL) o
 * consignación (BANCO) (SWR-24). Lo usan el registro de pagos de facturas y el pago de cuotas de acuerdos.
 */
export function SelectorCanal({ valor, error, onCambiar }) {
  return (
    <fieldset className="pago-canales formulario-campo-completo" aria-invalid={error ? true : undefined}>
      <legend>
        Canal del pago <b aria-hidden="true">*</b>
      </legend>
      <div>
        {CANALES_PRESENCIALES.map((codigo) => {
          const canal = presentarCanal(codigo);
          return (
            <label key={codigo} className="pago-canal">
              <input
                type="radio"
                name="canal"
                value={codigo}
                checked={valor === codigo}
                aria-invalid={error ? true : undefined}
                onChange={(evento) => onCambiar(evento.target.value)}
              />
              <span>
                <strong>{canal.etiqueta}</strong>
                <small>{canal.detalle}</small>
              </span>
            </label>
          );
        })}
      </div>
      {error && (
        <small className="campo-error">
          <Icono nombre="alerta" tamano={14} />
          {error}
        </small>
      )}
    </fieldset>
  );
}