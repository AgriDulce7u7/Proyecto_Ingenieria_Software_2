import { calcularMetricas } from "./bandeja";

/** Franja de indicadores: pendientes, próximas a vencer (SWR-07), vencidas (RN-04) y resueltas. */
export function MetricasBandeja({ pqrs }) {
  const metricas = calcularMetricas(pqrs);

  const indicadores = [
    { etiqueta: "Pendientes", valor: metricas.pendientes, detalle: `${metricas.sinAsignar} sin asignar` },
    { etiqueta: "Próximas a vencer", valor: metricas.proximas, detalle: "En las próximas 48 h", tono: "plazo" },
    { etiqueta: "Vencidas", valor: metricas.vencidas, detalle: "Requieren atención", tono: "vencido" },
    { etiqueta: "Resueltas", valor: metricas.resueltas, detalle: "Resueltas o cerradas" },
  ];

  return (
    <dl className="bandeja-metricas">
      {indicadores.map((indicador) => (
        <div key={indicador.etiqueta}>
          <dt>{indicador.etiqueta}</dt>
          <dd className={indicador.tono ? `bandeja-metrica-${indicador.tono}` : undefined}>{indicador.valor}</dd>
          <dd className="bandeja-metrica-detalle">{indicador.detalle}</dd>
        </div>
      ))}
    </dl>
  );
}