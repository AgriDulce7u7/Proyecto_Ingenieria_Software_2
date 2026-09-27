import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { Boton } from "../../componentes/ui/Boton";
import { Campo } from "../../componentes/ui/Campo";
import { Icono } from "../../componentes/ui/Icono";
import { useTituloPagina } from "../../hooks/useTituloPagina";
import { FORMATO_RADICADO, normalizarRadicado } from "./radicado";
import "./inicio.css";

const PASOS = [
  { titulo: "Cuéntenos su solicitud", texto: "Complete un formulario breve con la información necesaria." },
  { titulo: "Reciba su radicado", texto: "Le entregamos un número único para hacer seguimiento." },
  { titulo: "Consulte el avance", texto: "Revise el estado y las fechas sin crear una cuenta." },
];

/** Inicio del portal ciudadano: radicar una PQR o consultar una existente. */
export function Inicio() {
  useTituloPagina("Atención ciudadana");
  const navegar = useNavigate();
  const [radicado, setRadicado] = useState("");
  const [error, setError] = useState("");

  const consultar = (evento) => {
    evento.preventDefault();
    const valor = normalizarRadicado(radicado);
    if (!FORMATO_RADICADO.test(valor)) {
      setError("Escriba el radicado completo, por ejemplo 202609-0001.");
      return;
    }
    navegar(`/consultar?radicado=${encodeURIComponent(valor)}`);
  };

  return (
    <>
      <section className="inicio-hero">
        <div className="inicio-hero-interior">
          <div className="inicio-ondas" aria-hidden="true">
            <i />
            <i />
            <i />
          </div>

          <div className="inicio-texto">
            <span className="antetitulo">Atención ciudadana</span>
            <h1>Estamos aquí para escucharle y darle respuesta.</h1>
            <p>
              Presente una petición, queja o reclamo ante Empresas Públicas del Quindío. El trámite es gratuito y
              puede consultar su avance en cualquier momento.
            </p>
            <div className="inicio-acciones">
              <Boton a="/radicar" icono="mas">
                Radicar una PQR
              </Boton>
              <Boton a="/consultar" variante="secundario" icono="buscar">
                Consultar solicitud
              </Boton>
            </div>
          </div>

          <form className="inicio-consulta" onSubmit={consultar} noValidate>
            <span className="inicio-consulta-icono">
              <Icono nombre="documento" tamano={24} />
            </span>
            <h2>¿Ya tiene un radicado?</h2>
            <p>Consulte el estado actual y las fechas de su solicitud.</p>
            <Campo
              etiqueta="Número de radicado"
              className="cifra"
              placeholder="Ej. 202609-0001"
              autoComplete="off"
              inputMode="numeric"
              value={radicado}
              error={error}
              onChange={(evento) => {
                setRadicado(evento.target.value);
                setError("");
              }}
            />
            <Boton type="submit" icono="flecha">
              Consultar estado
            </Boton>
            <small>El número se encuentra en su constancia de radicación.</small>
          </form>
        </div>
      </section>

      <section className="inicio-pasos" aria-label="Cómo funciona">
        <ol>
          {PASOS.map((paso, indice) => (
            <li key={paso.titulo}>
              <span className="inicio-paso-numero" aria-hidden="true">
                {String(indice + 1).padStart(2, "0")}
              </span>
              <h3>{paso.titulo}</h3>
              <p>{paso.texto}</p>
            </li>
          ))}
        </ol>
      </section>
    </>
  );
}