import { useRef, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { Cargando } from "../../../componentes/Cargando";
import { MensajeError } from "../../../componentes/MensajeError";
import { enfocarPrimerError } from "../../../componentes/enfocarPrimerError";
import { Boton } from "../../../componentes/ui/Boton";
import { Campo } from "../../../componentes/ui/Campo";
import { EnlaceVolver } from "../../../componentes/ui/EnlaceVolver";
import { Icono } from "../../../componentes/ui/Icono";
import { useFacturaPortal, usePagarEnLinea } from "../../../hooks/usePortal";
import { useTituloPagina } from "../../../hooks/useTituloPagina";
import { formatearMoneda, partesPeriodo } from "../../../presentacion/facturacion";
import { formatearFecha } from "../../../presentacion/fechas";
import { motivoNoPagable } from "../../../presentacion/pagos";
import { aniosVencimiento, digitosTarjeta, formatearTarjeta, validarTarjeta } from "../../../presentacion/portal";

const TARJETA_VACIA = { numeroTarjeta: "", titular: "", mesVencimiento: "", anioVencimiento: "", cvv: "" };
const MESES = Array.from({ length: 12 }, (_, indice) => String(indice + 1).padStart(2, "0"));

/** Tarjetas de la pasarela simulada del prototipo (PasarelaPagosSimuladaAdapter en el backend). */
const TARJETAS_PRUEBA = [
  { numero: "4111 1111 1111 1111", resultado: "Aprobada" },
  { numero: "4000 0000 0000 0002", resultado: "Rechazada por fondos insuficientes" },
  { numero: "4000 0000 0000 0069", resultado: "Rechazada por tarjeta vencida" },
  { numero: "4000 0000 0000 0119", resultado: "La pasarela no responde" },
];

/** Mensaje de cada fallo del pago. En ninguno la factura cambia: sigue pendiente (RN-05). */
function ErrorPago({ error }) {
  if (error.estado === 402) {
    return (
      <div className="aviso aviso-error" role="alert">
        <p>
          <strong>El banco no aprobó el pago.</strong> {error.detalle}
        </p>
        <p>Puede intentarlo de nuevo con otra tarjeta.</p>
      </div>
    );
  }
  if (error.estado === 504) {
    return (
      <div className="aviso aviso-error" role="alert">
        <p>
          <strong>La pasarela de pagos no respondió.</strong> {error.detalle}
        </p>
      </div>
    );
  }
  return <MensajeError error={error} />;
}

/** Resumen de lo que se va a pagar, al lado del formulario. */
function ResumenPago({ factura }) {
  return (
    <aside className="pagar-resumen" aria-labelledby="pagar-resumen-titulo">
      <span id="pagar-resumen-titulo" className="antetitulo">
        Total a pagar
      </span>
      <strong className="pagar-resumen-total cifra">{formatearMoneda(factura.total)}</strong>
      <dl>
        <div>
          <dt>Factura</dt>
          <dd className="cifra">{factura.numeroFactura}</dd>
        </div>
        <div>
          <dt>Servicio</dt>
          <dd>{factura.contrato.servicio}</dd>
        </div>
        <div>
          <dt>Periodo</dt>
          <dd>{partesPeriodo(factura.periodo).corto}</dd>
        </div>
        <div>
          <dt>Vencimiento</dt>
          <dd>{formatearFecha(factura.fechaVencimiento)}</dd>
        </div>
      </dl>
      <p>
        <Icono nombre="candado" tamano={15} />
        EPQ no guarda los datos de su tarjeta; solo los últimos cuatro dígitos en el registro del intento.
      </p>
    </aside>
  );
}

/** Formulario de la tarjeta. El valor es el total de la factura (CO-02): no se paga por partes. */
function FormularioTarjeta({ factura }) {
  const navegar = useNavigate();
  const formulario = useRef(null);
  const pago = usePagarEnLinea(factura.numeroFactura);
  const [datos, setDatos] = useState(TARJETA_VACIA);
  const [errores, setErrores] = useState({});

  const cambiar = (campo, valor) => {
    setDatos((anteriores) => ({ ...anteriores, [campo]: valor }));
    setErrores((anteriores) => ({ ...anteriores, [campo]: null }));
  };
  const propsCampo = (campo) => ({
    name: campo,
    value: datos[campo],
    error: errores[campo] ?? pago.error?.errores?.[campo],
    disabled: pago.isPending,
    onChange: (evento) => cambiar(campo, evento.target.value),
  });

  const pagar = (evento) => {
    evento.preventDefault();
    const erroresCliente = validarTarjeta(datos);
    setErrores(erroresCliente);
    if (Object.keys(erroresCliente).length > 0) {
      enfocarPrimerError(formulario.current);
      return;
    }
    pago.mutate(
      {
        numeroTarjeta: digitosTarjeta(datos.numeroTarjeta),
        titular: datos.titular.trim(),
        mesVencimiento: Number(datos.mesVencimiento),
        anioVencimiento: Number(datos.anioVencimiento),
        cvv: datos.cvv,
        valor: factura.total,
      },
      {
        onSuccess: (comprobante) =>
          navegar(`/mi-cuenta/pagos/${comprobante.codigoPago}`, { replace: true, state: { pagado: true } }),
        // Un rechazo no deja rastro de la tarjeta en pantalla: se borra el código de seguridad
        onError: () => setDatos((anteriores) => ({ ...anteriores, cvv: "" })),
      },
    );
  };

  const errorGeneral = pago.error && !pago.error.esValidacion ? pago.error : null;

  return (
    <form ref={formulario} className="cuenta-panel pagar-formulario" onSubmit={pagar} noValidate>
      <h3>Pago con tarjeta débito o crédito</h3>

      {errorGeneral && <ErrorPago error={errorGeneral} />}

      <Campo
        etiqueta="Número de la tarjeta"
        obligatorio
        className="cifra"
        inputMode="numeric"
        autoComplete="cc-number"
        placeholder="0000 0000 0000 0000"
        {...propsCampo("numeroTarjeta")}
        value={formatearTarjeta(datos.numeroTarjeta)}
        onChange={(evento) => cambiar("numeroTarjeta", digitosTarjeta(evento.target.value))}
      />
      <Campo
        etiqueta="Nombre del titular"
        obligatorio
        autoComplete="cc-name"
        maxLength={100}
        placeholder="Como aparece en la tarjeta"
        {...propsCampo("titular")}
      />
      <div className="pagar-fila">
        <Campo etiqueta="Mes" como="select" obligatorio autoComplete="cc-exp-month" {...propsCampo("mesVencimiento")}>
          <option value="">MM</option>
          {MESES.map((mes) => (
            <option key={mes} value={mes}>
              {mes}
            </option>
          ))}
        </Campo>
        <Campo etiqueta="Año" como="select" obligatorio autoComplete="cc-exp-year" {...propsCampo("anioVencimiento")}>
          <option value="">AAAA</option>
          {aniosVencimiento().map((anio) => (
            <option key={anio} value={anio}>
              {anio}
            </option>
          ))}
        </Campo>
        <Campo
          etiqueta="Código de seguridad"
          obligatorio
          type="password"
          className="cifra"
          inputMode="numeric"
          autoComplete="cc-csc"
          maxLength={4}
          placeholder="CVV"
          {...propsCampo("cvv")}
          onChange={(evento) => cambiar("cvv", evento.target.value.replace(/\D/g, ""))}
        />
      </div>

      {pago.isPending && (
        <div className="aviso aviso-info" role="status">
          <p>
            <strong>Procesando el pago con la pasarela…</strong> No cierre ni recargue esta página. Puede tardar hasta
            un minuto.
          </p>
        </div>
      )}

      <Boton type="submit" icono="candado" disabled={pago.isPending}>
        {pago.isPending ? "Procesando…" : `Pagar ${formatearMoneda(factura.total)}`}
      </Boton>

      <details className="pagar-pruebas">
        <summary>Tarjetas de prueba (pasarela simulada)</summary>
        <p>Use cualquier nombre, una fecha futura y cualquier código de 3 dígitos.</p>
        <ul>
          {TARJETAS_PRUEBA.map((tarjeta) => (
            <li key={tarjeta.numero}>
              <span className="cifra">{tarjeta.numero}</span>
              <small>{tarjeta.resultado}</small>
            </li>
          ))}
        </ul>
      </details>
    </form>
  );
}

/** Pago en línea de una factura (CU-04, DS-03, CO-02, SWR-21 a SWR-23). Si no admite pagos, explica por qué. */
export function PagarFactura() {
  const { numero } = useParams();
  useTituloPagina(`Pagar factura ${numero}`);
  const factura = useFacturaPortal(numero);
  const motivo = factura.data ? motivoNoPagable(factura.data) : null;

  return (
    <>
      <div className="cuenta-volver">
        <EnlaceVolver a={`/mi-cuenta/facturas/${numero}`} historial>
          Volver a la factura
        </EnlaceVolver>
      </div>
      <h2 className="cuenta-titulo-seccion">Pagar en línea</h2>

      {factura.isPending && <Cargando texto="Cargando la factura…" />}
      {factura.error?.noEncontrado && (
        <div className="aviso aviso-error" role="alert">
          <p>
            No encontramos la factura <strong className="cifra">{numero}</strong> entre sus facturas.
          </p>
        </div>
      )}
      {factura.error && !factura.error.noEncontrado && <MensajeError error={factura.error} />}
      {motivo && (
        <div className="aviso aviso-info" role="status">
          <p>
            {motivo} <Link to={`/mi-cuenta/facturas/${numero}`}>Ver la factura</Link>.
          </p>
        </div>
      )}
      {factura.data && !motivo && (
        <div className="pagar-rejilla">
          <FormularioTarjeta key={numero} factura={factura.data} />
          <ResumenPago factura={factura.data} />
        </div>
      )}
    </>
  );
}