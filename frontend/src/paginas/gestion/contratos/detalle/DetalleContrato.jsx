import { useState } from "react";
import { Link, useLocation, useParams } from "react-router-dom";
import { Cargando } from "../../../../componentes/Cargando";
import { MensajeError } from "../../../../componentes/MensajeError";
import { Boton } from "../../../../componentes/ui/Boton";
import { Dialogo } from "../../../../componentes/ui/Dialogo";
import { EnlaceVolver } from "../../../../componentes/ui/EnlaceVolver";
import { Etiqueta } from "../../../../componentes/ui/Etiqueta";
import { Icono } from "../../../../componentes/ui/Icono";
import { useContrato } from "../../../../hooks/useContratos";
import { useTituloPagina } from "../../../../hooks/useTituloPagina";
import { accionHacia, limpiarAdvertencia } from "../../../../presentacion/contratos";
import { formatearMoneda } from "../../../../presentacion/facturacion";
import { formatearFecha, formatearFechaHora } from "../../../../presentacion/fechas";
import { nombreUsoPredio, presentarEstadoContrato } from "../../../../presentacion/usuarios";
import { enmascararDocumento } from "../../../../presentacion/texto";
import { useSesion } from "../../../../sesion/useSesion";
import { DialogoEstadoContrato } from "./DialogoEstadoContrato";
import { DialogoMedidor } from "./DialogoMedidor";
import { HistorialContrato } from "./HistorialContrato";
import "../../pqr/detalle/detalle.css";
import "../contratos.css";

/** Orden de los botones de cambio de estado: primero el habitual y al final el definitivo. */
const ORDEN_ACCIONES = ["ACTIVO", "SUSPENDIDO", "CANCELADO"];

/** Lista de datos (término y valor), con el mismo estilo del detalle de PQR. */
function Datos({ filas }) {
  return (
    <dl className="detalle-datos">
      {filas
        .filter((fila) => fila.valor)
        .map((fila) => (
          <div key={fila.termino}>
            <dt>{fila.termino}</dt>
            <dd className={fila.clase}>{fila.valor}</dd>
          </div>
        ))}
    </dl>
  );
}

/** Avisos del contrato: recién creado, pendiente por falta de medidor y candidato a suspensión. */
function AvisosContrato({ contrato, creado, advertencias }) {
  const faltaMedidor = contrato.estado === "PENDIENTE" && contrato.requiereMedidor && !contrato.medidorActivo;
  const otras = advertencias.map(limpiarAdvertencia).filter((texto) => texto && !texto.includes("PENDIENTE"));

  return (
    <>
      {creado && (
        <div className="aviso aviso-exito detalle-resultado" role="status">
          <p>
            Se registró el contrato <strong className="cifra">{contrato.numeroContrato}</strong> con la tarifa{" "}
            {contrato.tarifa?.nombre}.
          </p>
        </div>
      )}
      {faltaMedidor && (
        <div className="aviso aviso-info detalle-resultado" role="status">
          <p>
            El contrato está <strong>pendiente de activación</strong>: los contratos de{" "}
            {contrato.servicio.toLowerCase()} requieren un medidor instalado. Instálelo para poder activarlo.
          </p>
        </div>
      )}
      {otras.map((texto) => (
        <div key={texto} className="aviso aviso-info detalle-resultado" role="status">
          <p>{texto}</p>
        </div>
      ))}
      {contrato.candidatoSuspension && (
        <div className="aviso aviso-error detalle-resultado" role="status">
          <p>
            <strong>Candidato a suspensión</strong> por mora en el pago. La suspensión la decide el gestor de cartera.
          </p>
        </div>
      )}
    </>
  );
}

function ContenidoContrato({ contrato, creado, advertencias }) {
  const { puede } = useSesion();
  const puedeGestionar = puede("COMERCIAL_GESTIONAR");
  const [dialogo, setDialogo] = useState(null);
  const [resultado, setResultado] = useState(null);
  const estado = presentarEstadoContrato(contrato.estado);
  const acciones = ORDEN_ACCIONES.filter((destino) => contrato.accionesDisponibles.includes(destino));
  const cancelado = contrato.estado === "CANCELADO";
  const { predio, tarifa, medidorActivo } = contrato;

  const cerrar = () => setDialogo(null);
  const terminar = (mensaje) => {
    cerrar();
    setResultado(mensaje);
  };

  return (
    <>
      <div className="detalle-encabezado">
        <div>
          <span className="antetitulo">Contrato de {contrato.servicio.toLowerCase()}</span>
          <h1 className="cifra">{contrato.numeroContrato}</h1>
          <div className="detalle-etiquetas">
            <Etiqueta tono={estado.tono}>{estado.etiqueta}</Etiqueta>
            <span>{contrato.servicio}</span>
            {predio && <span>{predio.municipio}</span>}
            <span>Desde el {formatearFecha(contrato.fechaInicio)}</span>
          </div>
        </div>
        {puedeGestionar && !cancelado && (
          <div className="detalle-acciones">
            {contrato.requiereMedidor && (
              <Boton variante="secundario" icono="sincronizar" onClick={() => setDialogo("medidor")}>
                {medidorActivo ? "Cambiar medidor" : "Instalar medidor"}
              </Boton>
            )}
            {acciones.map((destino) => {
              const accion = accionHacia(destino, contrato.estado);
              return (
                <Boton key={destino} variante={accion.variante} onClick={() => setDialogo(destino)}>
                  {accion.texto}
                </Boton>
              );
            })}
          </div>
        )}
      </div>

      {resultado && (
        <div className="aviso aviso-exito detalle-resultado" role="status">
          <p>{resultado}</p>
        </div>
      )}
      <AvisosContrato contrato={contrato} creado={creado && !resultado} advertencias={resultado ? [] : advertencias} />

      <div className="detalle-rejilla">
        <div className="detalle-principal">
          <section className="panel" aria-labelledby="titular-titulo">
            <div className="panel-titulo">
              <h2 id="titular-titulo">Titular y predio</h2>
              <Link to={`/gestion/usuarios/${contrato.codigoUsuario}`} className="contrato-enlace-perfil">
                Ver perfil
                <Icono nombre="chevron" tamano={16} />
              </Link>
            </div>
            <Datos
              filas={[
                { termino: "Usuario", valor: contrato.nombreUsuario },
                { termino: "Código", valor: contrato.codigoUsuario, clase: "cifra" },
                { termino: "Documento", valor: enmascararDocumento(contrato.documentoUsuario ?? "") },
                { termino: "Dirección del predio", valor: predio && `${predio.direccion}, ${predio.municipio}` },
                {
                  termino: "Predio",
                  valor: predio && `${predio.codigoPredio} · Estrato ${predio.estrato} · ${nombreUsoPredio(predio.uso)}`,
                },
              ]}
            />
          </section>

          <HistorialContrato historial={contrato.historial} />
        </div>

        <aside className="detalle-lateral">
          {tarifa && (
            <section className="panel" aria-labelledby="tarifa-titulo">
              <h2 id="tarifa-titulo">Tarifa</h2>
              <Datos
                filas={[
                  { termino: "Tarifa aplicada", valor: tarifa.nombre },
                  { termino: "Aplica a", valor: tarifa.criterio },
                  { termino: "Valor por m³", valor: formatearMoneda(tarifa.valorPorUnidad), clase: "cifra" },
                  { termino: "Cargo fijo mensual", valor: formatearMoneda(tarifa.cargoFijo), clase: "cifra" },
                  { termino: "Vigente desde", valor: formatearFecha(tarifa.vigenciaInicio) },
                ]}
              />
            </section>
          )}

          <section className="panel" aria-labelledby="medidor-titulo">
            <h2 id="medidor-titulo">Medidor</h2>
            {medidorActivo ? (
              <Datos
                filas={[
                  { termino: "Número de serie", valor: medidorActivo.numeroSerie, clase: "cifra" },
                  { termino: "Instalado el", valor: formatearFecha(medidorActivo.fechaInstalacion) },
                ]}
              />
            ) : (
              <p className="contrato-vacio">
                {contrato.requiereMedidor
                  ? "Sin medidor instalado. Es obligatorio para activar este servicio."
                  : "Este servicio no requiere medidor."}
              </p>
            )}
          </section>

          <section className="panel" aria-labelledby="registro-contrato-titulo">
            <h2 id="registro-contrato-titulo">Registro</h2>
            <Datos
              filas={[
                { termino: "Registrado", valor: formatearFechaHora(contrato.fechaRegistro) },
                { termino: "Registrado por", valor: contrato.registradoPor },
              ]}
            />
          </section>
        </aside>
      </div>

      <Dialogo
        abierto={ORDEN_ACCIONES.includes(dialogo)}
        onCerrar={cerrar}
        antetitulo={contrato.numeroContrato}
        titulo={dialogo ? accionHacia(dialogo, contrato.estado).titulo : ""}
      >
        {ORDEN_ACCIONES.includes(dialogo) && (
          <DialogoEstadoContrato contrato={contrato} destino={dialogo} onListo={terminar} onCancelar={cerrar} />
        )}
      </Dialogo>
      <Dialogo
        abierto={dialogo === "medidor"}
        onCerrar={cerrar}
        antetitulo={contrato.numeroContrato}
        titulo={medidorActivo ? "Cambiar medidor" : "Instalar medidor"}
      >
        <DialogoMedidor contrato={contrato} onListo={terminar} onCancelar={cerrar} />
      </Dialogo>
    </>
  );
}

/** Detalle de un contrato de servicio (CU-02, RF-04, SWR-18 a SWR-20): titular, tarifa, medidor e historial. */
export function DetalleContrato() {
  const { numero } = useParams();
  const { state } = useLocation();
  useTituloPagina(`Contrato ${numero}`);
  const contrato = useContrato(numero);

  return (
    <>
      <div className="detalle-volver">
        <EnlaceVolver a="/gestion/contratos" historial>
          Volver
        </EnlaceVolver>
      </div>

      {contrato.isPending && <Cargando texto="Cargando el contrato…" />}
      {contrato.error?.noEncontrado && (
        <div className="aviso aviso-error" role="alert">
          <p>
            No existe el contrato <strong className="cifra">{numero}</strong>.
          </p>
        </div>
      )}
      {contrato.error && !contrato.error.noEncontrado && <MensajeError error={contrato.error} />}
      {contrato.data && (
        <ContenidoContrato
          key={numero}
          contrato={contrato.data}
          creado={Boolean(state?.creado)}
          advertencias={state?.advertencias ?? []}
        />
      )}
    </>
  );
}