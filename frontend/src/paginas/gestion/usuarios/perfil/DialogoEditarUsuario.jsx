import { useState } from "react";
import { MensajeError } from "../../../../componentes/MensajeError";
import { Boton } from "../../../../componentes/ui/Boton";
import { Campo } from "../../../../componentes/ui/Campo";
import { useMunicipios } from "../../../../hooks/useTerritorio";
import { useActualizarUsuario } from "../../../../hooks/useUsuarios";
import { enfocarPrimerError } from "../../../../componentes/enfocarPrimerError";
import { CAMPOS_EDICION, LIMITES, aSolicitudEdicion, validar } from "../validacionUsuario";

/**
 * Actualización de datos del usuario (RF-02, SWR-15). La identificación no se modifica (SWR-12);
 * el backend audita cada campo que cambia.
 */
export function DialogoEditarUsuario({ usuario, onListo, onCancelar }) {
  const municipios = useMunicipios();
  const actualizacion = useActualizarUsuario(usuario.codigoInterno);
  const [datos, setDatos] = useState({
    nombreCompleto: usuario.nombreCompleto,
    telefono: usuario.telefono ?? "",
    correo: usuario.correo ?? "",
    direccionNotificacion: usuario.direccionNotificacion ?? "",
    municipioId: usuario.municipioId ? String(usuario.municipioId) : "",
  });
  const [errores, setErrores] = useState({});

  const propsCampo = (campo) => ({
    value: datos[campo],
    error: errores[campo] ?? actualizacion.error?.errores?.[campo],
    onChange: (evento) => {
      setDatos((anteriores) => ({ ...anteriores, [campo]: evento.target.value }));
      setErrores((anteriores) => ({ ...anteriores, [campo]: null }));
    },
  });

  const guardar = (evento) => {
    evento.preventDefault();
    const erroresCliente = validar(datos, CAMPOS_EDICION);
    setErrores(erroresCliente);
    if (Object.keys(erroresCliente).length > 0) {
      enfocarPrimerError(evento.currentTarget);
      return;
    }
    actualizacion.mutate(aSolicitudEdicion(datos), { onSuccess: () => onListo("Se actualizaron los datos del usuario.") });
  };

  return (
    <form className="dialogo-formulario" onSubmit={guardar} noValidate>
      <p className="perfil-dialogo-nota">
        Identificación <strong className="cifra">{usuario.tipoDocumento} {usuario.numeroDocumento}</strong>: no se
        puede modificar.
      </p>
      <Campo etiqueta="Nombre completo o razón social" obligatorio maxLength={LIMITES.nombreCompleto} {...propsCampo("nombreCompleto")} />
      <div className="perfil-dialogo-fila">
        <Campo etiqueta="Teléfono" obligatorio type="tel" maxLength={20} {...propsCampo("telefono")} />
        <Campo etiqueta="Correo electrónico" obligatorio type="email" maxLength={LIMITES.correo} {...propsCampo("correo")} />
      </div>
      <Campo etiqueta="Dirección de notificación" obligatorio maxLength={LIMITES.direccion} {...propsCampo("direccionNotificacion")} />
      <Campo etiqueta="Municipio" como="select" obligatorio disabled={municipios.isPending} {...propsCampo("municipioId")}>
        <option value="">Seleccione el municipio</option>
        {municipios.data?.map((municipio) => (
          <option key={municipio.id} value={municipio.id}>
            {municipio.nombre}
          </option>
        ))}
      </Campo>
      {actualizacion.error && !actualizacion.error.esValidacion && <MensajeError error={actualizacion.error} />}
      <div className="dialogo-acciones">
        <Boton variante="discreto" onClick={onCancelar}>
          Cancelar
        </Boton>
        <Boton type="submit" disabled={actualizacion.isPending}>
          {actualizacion.isPending ? "Guardando…" : "Guardar cambios"}
        </Boton>
      </div>
    </form>
  );
}