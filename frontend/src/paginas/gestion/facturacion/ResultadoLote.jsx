import { Boton } from "../../../componentes/ui/Boton";
import { Etiqueta } from "../../../componentes/ui/Etiqueta";
import { Icono } from "../../../componentes/ui/Icono";
import {
  formatearDuracion,
  formatearNumero,
  partesPeriodo,
  presentarEstadoLote,
  presentarIncidencia,
} from "../../../presentacion/facturacion";

/** Resultado de generar un lote: cifras, incidencias por contrato y estado de la sincronización con el ERP. */
export function ResultadoLote({ resultado, tiempoMaximoMinutos }) {
  const estado = presentarEstadoLote(resultado.estadoLote);
  const periodo = partesPeriodo(resultado.periodo);
  const erp = resultado.sincronizacionErp;
  const completado = resultado.estadoLote === "COMPLETADO";

  const cifras = [
    { etiqueta: "Generadas", valor: resultado.facturasGeneradas },
    { etiqueta: "Ya existentes", valor: resultado.facturasYaExistentes },
    { etiqueta: "Incidencias", valor: resultado.incidencias.length, tono: "plazo" },
    { etiqueta: "Sincronizadas ERP", valor: erp?.exitosas ?? 0 },
  ];

  return (
    <section className="lote-resultado" aria-labelledby="lote-resultado-titulo" role="status">
      <div className={`lote-resultado-banda ${completado ? "" : "lote-resultado-banda-error"}`}>
        <span className="lote-resultado-icono" aria-hidden="true">
          <Icono nombre={completado ? "check" : "alerta"} />
        </span>
        <div>
          <h2 id="lote-resultado-titulo">
            Lote de {periodo.texto} {completado ? "generado" : "con errores"}
          </h2>
          <p>
            El procesamiento terminó en {formatearDuracion(resultado.duracionMs)}
            {resultado.dentroDelTiempoMaximo
              ? `, dentro del límite de ${tiempoMaximoMinutos} min.`
              : ` y superó el límite de ${tiempoMaximoMinutos} min.`}{" "}
            Se evaluaron {resultado.contratosActivosEvaluados} contratos activos y se excluyeron{" "}
            {resultado.contratosNoActivosExcluidos} no activos.
          </p>
        </div>
        <Etiqueta tono={estado.tono}>{estado.etiqueta}</Etiqueta>
      </div>

      <dl className="lote-resultado-cifras">
        {cifras.map((cifra) => (
          <div key={cifra.etiqueta}>
            <dt>{cifra.etiqueta}</dt>
            <dd className={cifra.tono && cifra.valor > 0 ? `lote-cifra-${cifra.tono}` : undefined}>
              {formatearNumero(cifra.valor)}
            </dd>
          </div>
        ))}
      </dl>

      {resultado.incidencias.length > 0 && (
        <div className="lote-incidencias">
          <h3>Contratos no facturados</h3>
          <ul>
            {resultado.incidencias.map((incidencia) => (
              <li key={incidencia.numeroContrato}>
                <strong className="cifra">{incidencia.numeroContrato}</strong>
                <span>{presentarIncidencia(incidencia.tipo)}</span>
                <small>{incidencia.detalle}</small>
              </li>
            ))}
          </ul>
        </div>
      )}

      {erp?.fallidas > 0 && (
        <p className="lote-erp-fallidas">
          <Icono nombre="alerta" tamano={16} />
          {erp.fallidas === 1
            ? "1 factura no se sincronizó con el ERP."
            : `${erp.fallidas} facturas no se sincronizaron con el ERP.`}{" "}
          Puede reintentarlo desde el detalle del lote.
        </p>
      )}

      <div className="lote-resultado-pie">
        <Boton a={`/gestion/facturacion/lotes/${resultado.periodo}`} variante="secundario" icono="flecha">
          Ver lote y facturas
        </Boton>
      </div>
    </section>
  );
}