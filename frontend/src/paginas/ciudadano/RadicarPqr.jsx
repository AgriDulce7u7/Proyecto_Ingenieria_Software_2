import { useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import { MensajeError } from "../../componentes/MensajeError";
import { PaginaPortal } from "../../componentes/PaginaPortal";
import { Boton } from "../../componentes/ui/Boton";
import { Campo } from "../../componentes/ui/Campo";
import { Icono } from "../../componentes/ui/Icono";
import { useRegistrarPqr, useTiposSolicitud } from "../../hooks/usePqr";
import { LIMITES, PQR_VACIA, TIPOS_DOCUMENTO, aSolicitudPqr, validarCampo, validarPqr } from "./ValidacionPqr";
import "./radicar.css";

/** Bloque numerado del formulario (1. Datos personales, 2. Información de la solicitud). */
function SeccionFormulario({ numero, titulo, descripcion, children }) {
  return (
    <section className="radicar-seccion" aria-labelledby={`radicar-seccion-${numero}`}>
      <span className="radicar-seccion-numero" aria-hidden="true">
        {numero}
      </span>
      <div>
        <h2 id={`radicar-seccion-${numero}`}>{titulo}</h2>
        <p>{descripcion}</p>
        <div className="radicar-campos">{children}</div>
      </div>
    </section>
  );
}

/** Mueve el foco al primer campo con error para que el ciudadano sepa qué corregir. */
function enfocarPrimerError(formulario) {
  requestAnimationFrame(() => formulario?.querySelector('[aria-invalid="true"]')?.focus());
}

/**
 * Radicación de una PQR por el portal ciudadano (RF-08, CU-06).
 * Valida al enviar con las mismas reglas del backend y, desde ahí, revalida en vivo cada campo con error
 * (no al salir del campo: el mensaje desplazaría el botón y el clic de envío podría perderse).
 * Los errores que devuelva el servidor se muestran en su campo. Al radicar, lleva a la constancia.
 */
export function RadicarPqr() {
  const navegar = useNavigate();
  const formulario = useRef(null);
  const [datos, setDatos] = useState(PQR_VACIA);
  const [errores, setErrores] = useState({});
  const tiposSolicitud = useTiposSolicitud();
  const registro = useRegistrarPqr();

  const errorGeneral = registro.error && !registro.error.esValidacion ? registro.error : null;

  const actualizar = (campo) => (evento) => {
    const valor = evento.target.value;
    setDatos((anteriores) => ({ ...anteriores, [campo]: valor }));
    // Si el campo ya mostraba un error, se revalida mientras el ciudadano lo corrige
    if (errores[campo]) {
      setErrores((anteriores) => ({ ...anteriores, [campo]: validarCampo(campo, valor) }));
    }
  };

  const propsCampo = (campo) => ({
    name: campo,
    value: datos[campo],
    error: errores[campo],
    onChange: actualizar(campo),
  });

  const radicar = (evento) => {
    evento.preventDefault();
    const erroresCliente = validarPqr(datos);
    setErrores(erroresCliente);
    if (Object.keys(erroresCliente).length > 0) {
      enfocarPrimerError(formulario.current);
      return;
    }

    registro.mutate(aSolicitudPqr(datos), {
      onSuccess: (radicacion) => navegar("/radicar/constancia", { state: { radicacion } }),
      onError: (error) => {
        if (error.esValidacion) {
          setErrores(error.errores);
          enfocarPrimerError(formulario.current);
        }
      },
    });
  };

  return (
    <PaginaPortal
      volverA="/"
      textoVolver="Volver al inicio"
      antetitulo="Nueva solicitud"
      titulo="Radique su PQR"
      descripcion="Los campos marcados con * son obligatorios. Le tomará cerca de 5 minutos."
    >
      <form ref={formulario} className="radicar-formulario" onSubmit={radicar} noValidate>
        <SeccionFormulario
          numero={1}
          titulo="Datos personales"
          descripcion="Usaremos estos datos únicamente para responder su solicitud."
        >
          <Campo etiqueta="Tipo de documento" como="select" obligatorio {...propsCampo("tipoDocumento")}>
            {TIPOS_DOCUMENTO.map((tipo) => (
              <option key={tipo.codigo} value={tipo.codigo}>
                {tipo.nombre}
              </option>
            ))}
          </Campo>
          <Campo
            etiqueta="Número de documento"
            obligatorio
            placeholder="1094912580"
            autoComplete="off"
            maxLength={25}
            {...propsCampo("numeroDocumento")}
          />
          <Campo
            etiqueta="Nombre completo"
            obligatorio
            placeholder="Como aparece en su documento"
            autoComplete="name"
            maxLength={LIMITES.nombreCompleto}
            {...propsCampo("nombreCompleto")}
          />
          <Campo
            etiqueta="Correo electrónico"
            obligatorio
            type="email"
            placeholder="nombre@correo.com"
            autoComplete="email"
            maxLength={LIMITES.correo}
            {...propsCampo("correo")}
          />
          <Campo
            etiqueta="Teléfono"
            type="tel"
            placeholder="300 000 0000 (opcional)"
            autoComplete="tel"
            maxLength={20}
            {...propsCampo("telefono")}
          />
          <Campo
            etiqueta="Dirección"
            placeholder="Dirección de notificación (opcional)"
            autoComplete="street-address"
            maxLength={LIMITES.direccion}
            {...propsCampo("direccion")}
          />
        </SeccionFormulario>

        <SeccionFormulario
          numero={2}
          titulo="Información de la solicitud"
          descripcion="Sea claro y específico para que podamos ayudarle mejor."
        >
          <Campo
            etiqueta="Tipo de solicitud"
            como="select"
            obligatorio
            disabled={tiposSolicitud.isPending || tiposSolicitud.isError}
            {...propsCampo("tipoSolicitud")}
            error={
              errores.tipoSolicitud ??
              (tiposSolicitud.isError ? "No fue posible cargar los tipos de solicitud. Recargue la página." : null)
            }
          >
            <option value="">{tiposSolicitud.isPending ? "Cargando…" : "Seleccione una opción"}</option>
            {tiposSolicitud.data?.map((tipo) => (
              <option key={tipo.codigo} value={tipo.codigo}>
                {tipo.nombre}
              </option>
            ))}
          </Campo>
          <Campo
            etiqueta="Asunto"
            obligatorio
            placeholder="Resumen breve de la solicitud"
            maxLength={LIMITES.asunto}
            {...propsCampo("asunto")}
          />
          <Campo
            etiqueta="Descripción"
            como="textarea"
            obligatorio
            rows={6}
            className="radicar-campo-completo"
            placeholder="Describa los hechos, fechas y la solución que espera..."
            maxLength={LIMITES.descripcionMaxima}
            contador={`${datos.descripcion.length} / ${LIMITES.descripcionMaxima} caracteres`}
            {...propsCampo("descripcion")}
          />
        </SeccionFormulario>

        <div className="radicar-pie">
          <MensajeError error={errorGeneral} />
          <div className="radicar-pie-fila">
            <p>
              <Icono nombre="check" tamano={17} />
              Al enviar acepta que la información suministrada es correcta.
            </p>
            <Boton type="submit" icono="flecha" disabled={registro.isPending}>
              {registro.isPending ? "Radicando…" : "Radicar PQR"}
            </Boton>
          </div>
        </div>
      </form>
    </PaginaPortal>
  );
}