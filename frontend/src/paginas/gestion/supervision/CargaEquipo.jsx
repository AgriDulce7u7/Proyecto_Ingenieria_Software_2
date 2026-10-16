/**
 * Carga de cada gestor: pendientes, vencidas y críticas. Al elegir un gestor se filtra la tabla,
 * para que el jefe decida a quién apoyar o qué solicitudes reasignar.
 */
export function CargaEquipo({ carga, gestorElegido, onElegir }) {
  return (
    <section className="panel supervision-carga" aria-labelledby="carga-titulo">
      <div className="panel-titulo">
        <h2 id="carga-titulo">Carga del equipo</h2>
        <span className="panel-nota">Pendientes por gestor</span>
      </div>

      {carga.length === 0 ? (
        <p className="supervision-carga-vacia">Nadie del equipo tiene solicitudes pendientes.</p>
      ) : (
        <ul>
          {carga.map((fila) => {
            const elegido = gestorElegido === String(fila.clave);
            return (
              <li key={fila.clave}>
                <button
                  type="button"
                  className="supervision-gestor"
                  aria-pressed={elegido}
                  onClick={() => onElegir(String(fila.clave))}
                >
                  <span className="supervision-gestor-datos">
                    <strong>{fila.gestor}</strong>
                    <small>
                      {[
                        fila.municipio,
                        fila.vencidas > 0 && `${fila.vencidas} ${fila.vencidas === 1 ? "vencida" : "vencidas"}`,
                        fila.criticas > 0 && `${fila.criticas} ${fila.criticas === 1 ? "crítica" : "críticas"}`,
                      ]
                        .filter(Boolean)
                        .join(" · ")}
                    </small>
                  </span>
                  <span
                    className={`supervision-gestor-total cifra${fila.vencidas > 0 ? " supervision-gestor-total-vencido" : ""}`}
                    aria-label={`${fila.pendientes} pendientes`}
                  >
                    {fila.pendientes}
                  </span>
                </button>
              </li>
            );
          })}
        </ul>
      )}
    </section>
  );
}