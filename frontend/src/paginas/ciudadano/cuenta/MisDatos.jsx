import { useRef, useState } from "react";
import { Cargando } from "../../../componentes/Cargando";
import { MensajeError } from "../../../componentes/MensajeError";
import { enfocarPrimerError } from "../../../componentes/enfocarPrimerError";
import { Boton } from "../../../componentes/ui/Boton";
import { Campo } from "../../../componentes/ui/Campo";
import { useActualizarContacto, usePerfilPortal } from "../../../hooks/usePortal";
import { useTituloPagina } from "../../../hooks/useTituloPagina";
import { PATRON_CORREO, PATRON_TELEFONO } from "../../../presentacion/datosPersonales";

function validar(datos) {
  const errores = {};
  if (!PATRON_CORREO.test(datos.correo.trim())) errores.correo = "Escriba un correo válido.";
  if (!PATRON_TELEFONO.test(datos.telefono.trim())) errores.telefono = "Use entre 7 y 20 dígitos.";
  if (!datos.direccionNotificacion.trim()) errores.direccionNotificacion = "Escriba la dirección de notificación.";
  return errores;
}

const contactoDe = (perfil) => ({
  correo: perfil.correo ?? "",
  telefono: perfil.telefono ?? "",
  direccionNotificacion: perfil.direccionNotificacion ?? "",
});

/** Datos de contacto editables. Nombre y documento solo los cambia EPQ en una oficina. */
function FormularioContacto({ perfil }) {
  const formulario = useRef(null);
  const actualizacion = useActualizarContacto();
  const [datos, setDatos] = useState(() => contactoDe(perfil));
  const [errores, setErrores] = useState({});
  const [guardado, setGuardado] = useState(false);
  const original = contactoDe(perfil);
  const sinCambios = Object.keys(datos).every((campo) => datos[campo].trim() === original[campo]);

  const propsCampo = (campo) => ({
    name: campo,
    value: datos[campo],
    error: errores[campo] ?? actualizacion.error?.errores?.[campo],
    onChange: (evento) => {
      setDatos((anteriores) => ({ ...anteriores, [campo]: evento.target.value }));
      setErrores((anteriores) => ({ ...anteriores, [campo]: null }));
      setGuardado(false);
    },
  });

  const guardar = (evento) => {
    evento.preventDefault();
    const erroresCliente = validar(datos);
    setErrores(erroresCliente);
    if (Object.keys(erroresCliente).length > 0) {
      enfocarPrimerError(formulario.current);
      return;
    }
    actualizacion.mutate(
      {
        correo: datos.correo.trim(),
        telefono: datos.telefono.trim(),
        direccionNotificacion: datos.direccionNotificacion.trim(),
      },
      {
        onSuccess: (nuevo) => {
          setDatos(contactoDe(nuevo));
          setGuardado(true);
        },
      },
    );
  };

  return (
    <form ref={formulario} className="cuenta-panel cuenta-contacto" onSubmit={guardar} noValidate>
      <div className="cuenta-panel-titulo">
        <div>
          <h3>Datos de contacto</h3>
          <p>EPQ usa estos datos para comunicarse con usted sobre sus servicios.</p>
        </div>
      </div>
      {guardado && (
        <div className="aviso aviso-exito" role="status">
          <p>Sus datos de contacto quedaron actualizados.</p>
        </div>
      )}
      {actualizacion.error && !actualizacion.error.esValidacion && <MensajeError error={actualizacion.error} />}
      <Campo
        etiqueta="Correo electrónico"
        obligatorio
        type="email"
        autoComplete="email"
        maxLength={150}
        {...propsCampo("correo")}
      />
      <Campo etiqueta="Teléfono" obligatorio type="tel" autoComplete="tel" maxLength={20} {...propsCampo("telefono")} />
      <Campo
        etiqueta="Dirección de notificación"
        obligatorio
        autoComplete="street-address"
        maxLength={200}
        {...propsCampo("direccionNotificacion")}
      />
      <div className="cuenta-contacto-acciones">
        <Boton type="submit" icono="check" disabled={actualizacion.isPending || sinCambios}>
          {actualizacion.isPending ? "Guardando…" : "Guardar cambios"}
        </Boton>
      </div>
    </form>
  );
}

/** Datos del ciudadano en el portal (Tabla 7): identificación de solo lectura y contacto editable. */
export function MisDatos() {
  useTituloPagina("Mis datos");
  const perfil = usePerfilPortal();
  const datos = perfil.data;

  return (
    <section className="cuenta-seccion" aria-labelledby="datos-titulo">
      <div className="cuenta-seccion-titulo">
        <h2 id="datos-titulo">Mis datos</h2>
      </div>
      {perfil.isPending && <Cargando texto="Cargando sus datos…" />}
      <MensajeError error={perfil.error} />
      {datos && (
        <div className="cuenta-datos-rejilla">
          <section className="cuenta-panel" aria-labelledby="identificacion-titulo">
            <h3 id="identificacion-titulo">Identificación</h3>
            <dl className="cuenta-identificacion">
              <div>
                <dt>Nombre</dt>
                <dd>{datos.nombreCompleto}</dd>
              </div>
              <div>
                <dt>Documento</dt>
                <dd className="cifra">
                  {datos.tipoDocumento} {datos.numeroDocumento}
                </dd>
              </div>
              <div>
                <dt>Código de usuario</dt>
                <dd className="cifra">{datos.codigoInterno}</dd>
              </div>
              {datos.municipio && (
                <div>
                  <dt>Municipio</dt>
                  <dd>{datos.municipio}</dd>
                </div>
              )}
            </dl>
            <p className="cuenta-nota">Para corregir el nombre o el documento, acuda a una oficina de atención de EPQ.</p>
          </section>
          <FormularioContacto key={datos.codigoInterno} perfil={datos} />
        </div>
      )}
    </section>
  );
}