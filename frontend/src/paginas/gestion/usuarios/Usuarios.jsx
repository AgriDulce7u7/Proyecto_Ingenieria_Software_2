import { useState } from "react";
import { Cargando } from "../../../componentes/Cargando";
import { EncabezadoGestion } from "../../../componentes/EncabezadoGestion";
import { MensajeError } from "../../../componentes/MensajeError";
import { Boton } from "../../../componentes/ui/Boton";
import { Icono } from "../../../componentes/ui/Icono";
import { useBusquedaUsuarios } from "../../../hooks/useUsuarios";
import { useFiltrosUrl } from "../../../hooks/useFiltrosUrl";
import { useSesion } from "../../../sesion/useSesion";
import { TablaUsuarios } from "./TablaUsuarios";
import "../pqr/bandeja.css";
import "./usuarios.css";

/** Criterios de GET /api/usuarios-servicio. Nombre y dirección exigen al menos 3 caracteres. */
const CRITERIOS = [
  { codigo: "identificacion", texto: "Identificación", ayuda: "Número de documento o el inicio", minimo: 1 },
  { codigo: "nombre", texto: "Nombre", ayuda: "Nombre o razón social", minimo: 3 },
  { codigo: "direccion", texto: "Dirección del predio", ayuda: "Dirección del predio", minimo: 3 },
];
const LIMITE_RESULTADOS = 50;

/**
 * Búsqueda de usuarios del servicio (RF-03, SWR-16) por identificación, nombre o dirección del predio.
 * El criterio y el texto quedan en la URL; la consulta se hace al buscar, no al escribir.
 */
export function Usuarios() {
  const { puede } = useSesion();
  const { filtros, cambiarFiltros, limpiar } = useFiltrosUrl(["por", "q"]);
  const criterio = CRITERIOS.find((c) => c.codigo === filtros.por) ?? CRITERIOS[0];
  const [texto, setTexto] = useState(filtros.q);
  const [porElegido, setPorElegido] = useState(criterio.codigo);
  const [error, setError] = useState("");

  const busqueda = useBusquedaUsuarios(filtros.q ? { [criterio.codigo]: filtros.q } : {});
  const usuarios = busqueda.data ?? [];

  const buscar = (evento) => {
    evento.preventDefault();
    const elegido = CRITERIOS.find((c) => c.codigo === porElegido);
    const limpio = texto.trim();
    if (limpio.length < elegido.minimo) {
      setError(
        elegido.minimo === 1
          ? "Escriba el número de identificación."
          : `Escriba al menos ${elegido.minimo} caracteres del ${elegido.texto.toLowerCase()}.`,
      );
      return;
    }
    setError("");
    cambiarFiltros({ por: elegido.codigo, q: limpio });
  };

  const nuevaBusqueda = () => {
    setTexto("");
    setError("");
    limpiar();
  };

  return (
    <>
      <EncabezadoGestion
        antetitulo="Gestión comercial"
        titulo="Usuarios del servicio"
        descripcion="Consulte usuarios por identificación, nombre o dirección del predio."
        accion={
          puede("COMERCIAL_GESTIONAR") && (
            <Boton a="/gestion/usuarios/nuevo" icono="mas">
              Registrar usuario
            </Boton>
          )
        }
      />

      <section className="bandeja-panel" aria-label="Usuarios del servicio">
        <form className="bandeja-filtros usuarios-busqueda" role="search" onSubmit={buscar} noValidate>
          <select
            aria-label="Buscar por"
            value={porElegido}
            onChange={(evento) => {
              setPorElegido(evento.target.value);
              setError("");
            }}
          >
            {CRITERIOS.map((c) => (
              <option key={c.codigo} value={c.codigo}>
                {c.texto}
              </option>
            ))}
          </select>
          <label className="bandeja-buscador">
            <Icono nombre="buscar" tamano={18} />
            <span className="solo-lectores">{CRITERIOS.find((c) => c.codigo === porElegido).ayuda}</span>
            <input
              type="search"
              placeholder={`${CRITERIOS.find((c) => c.codigo === porElegido).ayuda}...`}
              value={texto}
              aria-invalid={error ? true : undefined}
              aria-describedby={error ? "busqueda-error" : undefined}
              onChange={(evento) => {
                setTexto(evento.target.value);
                setError("");
              }}
            />
          </label>
          <Boton type="submit" variante="secundario" disabled={busqueda.isFetching}>
            Buscar
          </Boton>
          {error && (
            <p id="busqueda-error" className="campo-error usuarios-busqueda-error" role="alert">
              <Icono nombre="alerta" tamano={14} />
              {error}
            </p>
          )}
        </form>

        {!filtros.q && (
          <div className="bandeja-estado usuarios-inicio">
            <Icono nombre="personas" tamano={28} />
            <p>
              Escriba un criterio y presione <strong>Buscar</strong>. Para evitar duplicados, busque al usuario antes
              de registrarlo.
            </p>
          </div>
        )}

        {filtros.q && (
          <div className="bandeja-resumen" aria-live="polite">
            <strong>
              {usuarios.length} {usuarios.length === 1 ? "usuario" : "usuarios"}
            </strong>
            <small>
              {busqueda.isFetching
                ? "Buscando…"
                : usuarios.length >= LIMITE_RESULTADOS
                  ? `Se muestran los primeros ${LIMITE_RESULTADOS}: precise la búsqueda`
                  : `${criterio.texto}: “${filtros.q}”`}
            </small>
          </div>
        )}

        {filtros.q && busqueda.isPending && (
          <div className="bandeja-estado">
            <Cargando texto="Buscando usuarios…" />
          </div>
        )}
        {busqueda.isError && (
          <div className="bandeja-estado">
            <MensajeError error={busqueda.error} />
          </div>
        )}
        {filtros.q && busqueda.isSuccess && usuarios.length === 0 && (
          <div className="bandeja-estado bandeja-vacia">
            <p>Ningún usuario coincide con la búsqueda.</p>
            <div className="usuarios-vacio-acciones">
              <Boton variante="secundario" onClick={nuevaBusqueda}>
                Nueva búsqueda
              </Boton>
              {puede("COMERCIAL_GESTIONAR") && (
                <Boton a="/gestion/usuarios/nuevo" variante="discreto" icono="mas">
                  Registrar usuario
                </Boton>
              )}
            </div>
          </div>
        )}

        {usuarios.length > 0 && filtros.q && <TablaUsuarios usuarios={usuarios} />}
      </section>
    </>
  );
}