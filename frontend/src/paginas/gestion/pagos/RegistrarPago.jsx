import { useRef, useState } from "react";
import { Link, useNavigate, useSearchParams } from "react-router-dom";
import { Cargando } from "../../../componentes/Cargando";
import { EncabezadoGestion } from "../../../componentes/EncabezadoGestion";
import { MensajeError } from "../../../componentes/MensajeError";
import { enfocarPrimerError } from "../../../componentes/enfocarPrimerError";
import { PieFormulario, SeccionFormulario } from "../../../componentes/SeccionFormulario";
import { Boton } from "../../../componentes/ui/Boton";
import { Campo } from "../../../componentes/ui/Campo";
import { Etiqueta } from "../../../componentes/ui/Etiqueta";
import { Icono } from "../../../componentes/ui/Icono";
import { useFactura } from "../../../hooks/useFacturacion";
import { useRegistrarPagoPresencial } from "../../../hooks/usePagos";
import { formatearMoneda, partesPeriodo, presentarEstadoCobro } from "../../../presentacion/facturacion";
import { formatearFecha } from "../../../presentacion/fechas";
import { CANALES_PRESENCIALES, motivoNoPagable, presentarCanal } from "../../../presentacion/pagos";
import "./pagos.css";

const MAXIMO_REFERENCIA = 100;

/** Número de factura escrito por el funcionario: sin espacios y en mayúsculas (FAC-202609-000001). */
const normalizarFactura = (texto) => texto.trim().toUpperCase();

/** Búsqueda de la factura por número. El número queda en la URL (?factura=...). */
function BuscarFactura({ inicial, onBuscar }) {
  const [numero, setNumero] = useState(inicial);
  const [error, setError] = useState("");

  const buscar = (evento) => {
    evento.preventDefault();
    const valor = normalizarFactura(numero);
    if (!valor) {
      setError("Escriba el número de la factura.");
      enfocarPrimerError(evento.currentTarget);
      return;
    }
    onBuscar(valor);
  };

  return (
    <form className="pago-buscar formulario-campo-completo" onSubmit={buscar} noValidate role="search">
      <Campo
        etiqueta="Número de factura"
        obligatorio
        className="cifra"
        placeholder="FAC-202609-000001"
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

/** Datos de la factura encontrada: a quién pertenece, qué periodo cubre y cuánto se debe pagar. */
function ResumenFactura({ factura }) {
  const cobro = presentarEstadoCobro(factura.estadoCobro);
  const { contrato } = factura;
  return (
    <div className="pago-factura formulario-campo-completo">
      <div className="pago-factura-datos">
        <span className="antetitulo">Factura {partesPeriodo(factura.periodo).texto}</span>
        <Link to={`/gestion/facturacion/facturas/${factura.numeroFactura}`} className="pago-factura-numero cifra">
          {factura.numeroFactura}
        </Link>
        <strong>{contrato.cliente}</strong>
        <small>
          <span className="cifra">{contrato.numeroContrato}</span> · {contrato.servicio} · {contrato.direccionServicio}
        </small>
      </div>
      <dl className="pago-factura-total">
        <div>
          <dt>{factura.estadoCobro === "PAGADA" ? "Total pagado" : "Total a pagar"}</dt>
          <dd className="cifra">{formatearMoneda(factura.total)}</dd>
        </div>
        <div>
          <dt>Vencimiento</dt>
          <dd>
            {formatearFecha(factura.fechaVencimiento)} <Etiqueta tono={cobro.tono}>{cobro.etiqueta}</Etiqueta>
          </dd>
        </div>
      </dl>
    </div>
  );
}

/** Canal del pago como opciones de radio: caja (PRESENCIAL) o consignación (BANCO) (SWR-24). */
function SelectorCanal({ valor, error, onCambiar }) {
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

/** Formulario del pago para una factura pagable. El valor es el total: no se admiten pagos parciales. */
function FormularioPago({ factura }) {
  const navegar = useNavigate();
  const formulario = useRef(null);
  const registro = useRegistrarPagoPresencial();
  const [datos, setDatos] = useState({ canal: "PRESENCIAL", referencia: "" });
  const [errores, setErrores] = useState({});

  const cambiar = (campo, valor) => {
    setDatos((anteriores) => ({ ...anteriores, [campo]: valor }));
    setErrores((anteriores) => ({ ...anteriores, [campo]: null }));
  };

  const registrar = (evento) => {
    evento.preventDefault();
    const erroresCliente = {};
    if (!datos.canal) erroresCliente.canal = "Seleccione el canal del pago.";
    setErrores(erroresCliente);
    if (Object.keys(erroresCliente).length > 0) {
      enfocarPrimerError(formulario.current);
      return;
    }
    registro.mutate(
      {
        numeroFactura: factura.numeroFactura,
        valor: factura.total,
        canal: datos.canal,
        referencia: datos.referencia.trim() || null,
      },
      {
        onSuccess: (comprobante) =>
          navegar(`/gestion/pagos/${comprobante.codigoPago}`, { replace: true, state: { registrado: true } }),
      },
    );
  };

  // Los errores de canal y referencia se muestran en su campo; cualquier otro (valor, factura ya pagada) va al pie
  const enCampos = registro.error?.errores?.canal || registro.error?.errores?.referencia;
  const errorGeneral = registro.error && !enCampos ? registro.error : null;

  return (
    <form ref={formulario} onSubmit={registrar} noValidate>
      <SeccionFormulario
        numero={2}
        titulo="Datos del pago"
        descripcion="Los pagos en línea los confirma la pasarela desde el portal ciudadano; aquí solo se registran los de caja y consignación."
      >
        <SelectorCanal
          valor={datos.canal}
          error={errores.canal ?? registro.error?.errores?.canal}
          onCambiar={(valor) => cambiar("canal", valor)}
        />
        <Campo
          etiqueta={datos.canal === "BANCO" ? "Número de la consignación" : "Número del recibo de caja"}
          maxLength={MAXIMO_REFERENCIA}
          autoComplete="off"
          ayuda="Opcional. Sirve para conciliar el pago con la caja o el extracto bancario."
          value={datos.referencia}
          error={registro.error?.errores?.referencia}
          onChange={(evento) => cambiar("referencia", evento.target.value)}
        />
        <div className="pago-valor">
          <span>Valor del pago</span>
          <strong className="cifra">{formatearMoneda(factura.total)}</strong>
          <small>Es el total de la factura: no se admiten pagos parciales.</small>
        </div>
      </SeccionFormulario>

      <PieFormulario>
        <MensajeError error={errorGeneral} />
        <div className="formulario-pie-acciones">
          <Boton a="/gestion/pagos" variante="discreto">
            Cancelar
          </Boton>
          <Boton type="submit" icono="check" disabled={registro.isPending}>
            {registro.isPending ? "Registrando…" : `Registrar pago de ${formatearMoneda(factura.total)}`}
          </Boton>
        </div>
      </PieFormulario>
    </form>
  );
}

/** Resultado de la búsqueda: la factura encontrada o por qué no se puede pagar (SWR-23). */
function ResultadoFactura({ numero }) {
  const factura = useFactura(numero);
  const motivo = factura.data ? motivoNoPagable(factura.data) : null;

  return (
    <>
      {factura.isPending && (
        <div className="formulario-campo-completo">
          <Cargando texto="Buscando la factura…" />
        </div>
      )}
      {factura.error?.noEncontrado && (
        <div className="aviso aviso-error formulario-campo-completo" role="alert">
          <p>
            No existe la factura <strong className="cifra">{numero}</strong>. Revise el número impreso en la factura.
          </p>
        </div>
      )}
      {factura.error && !factura.error.noEncontrado && (
        <div className="formulario-campo-completo">
          <MensajeError error={factura.error} />
        </div>
      )}
      {factura.data && <ResumenFactura factura={factura.data} />}
      {motivo && (
        <div className="aviso aviso-error formulario-campo-completo" role="alert">
          <p>
            <strong>No se puede registrar el pago.</strong> {motivo}
          </p>
        </div>
      )}
    </>
  );
}

/** Muestra el formulario del pago solo cuando la factura buscada admite pagos. */
function PagoDeFactura({ numero }) {
  const factura = useFactura(numero);
  if (!factura.data || motivoNoPagable(factura.data)) return null;
  return <FormularioPago key={numero} factura={factura.data} />;
}

/**
 * Registro de un pago presencial o por consignación (CU-04, RF-06, RN-05, SWR-22 a SWR-24). Se busca la
 * factura por número (o llega desde su detalle con ?factura=...) y al registrar se abre el comprobante.
 */
export function RegistrarPago() {
  const [parametros, setParametros] = useSearchParams();
  const numero = parametros.get("factura") ?? "";

  return (
    <>
      <EncabezadoGestion
        antetitulo="Recaudo"
        titulo="Registrar pago"
        descripcion="Registre el pago de una factura recibido en caja o por consignación bancaria. Se genera el comprobante de inmediato."
      />

      <div className="formulario-gestion">
        <SeccionFormulario numero={1} titulo="Factura" descripcion="Busque la factura con el número impreso en ella.">
          <BuscarFactura
            key={numero}
            inicial={numero}
            onBuscar={(valor) => setParametros({ factura: valor }, { replace: Boolean(numero) })}
          />
          {numero && <ResultadoFactura numero={numero} />}
        </SeccionFormulario>
        {numero && <PagoDeFactura numero={numero} />}
      </div>
    </>
  );
}