import { useEffect } from "react";
import { Boton } from "../../../componentes/ui/Boton";
import { Etiqueta } from "../../../componentes/ui/Etiqueta";
import { Icono } from "../../../componentes/ui/Icono";
import { useTituloPagina } from "../../../hooks/useTituloPagina";
import { nombreServicio, nombreUsoPredio, presentarValidacion } from "../../../presentacion/usuarios";

/**
 * Confirmación del registro (CU-01 paso 6): código interno asignado, predio y resultado de la
 * validación de identidad. Si la RNEC no validó (SWR-14), el usuario queda pendiente y se explica que
 * la validación se reintenta desde el perfil. La advertencia del backend trae la ruta de la API, que no
 * le sirve a quien atiende, por eso aquí se redacta el aviso.
 */
export function RegistroExitoso({ registro, onOtro }) {
  useTituloPagina("Usuario registrado");
   // El formulario quedó desplazado hasta el botón de envío: la confirmación se muestra desde arriba.
  // Va entre llaves: en Chrome reciente scrollTo devuelve una promesa, y un efecto solo puede
  // devolver una función de limpieza (si no, React falla con "destroy is not a function").
  useEffect(() => {
    window.scrollTo(0, 0);
  }, []);
  const validacion = presentarValidacion(registro.estadoValidacion);
  const { predio } = registro;

  return (
    <div className="usuarios-registrado">
      <span className="usuarios-registrado-icono" aria-hidden="true">
        <Icono nombre="check" tamano={30} />
      </span>
      <span className="antetitulo">Usuario registrado</span>
      <h1>{registro.nombreCompleto}</h1>
      <p>Para vincular el servicio, cree el contrato del predio desde el perfil del usuario.</p>

      <div className="usuarios-registrado-codigo">
        <span>Código interno</span>
        <strong className="cifra">{registro.codigoInterno}</strong>
      </div>

      {registro.advertencia && (
        <div className="aviso aviso-error" role="status">
          <p>
            No fue posible validar la identificación con la Registraduría. El usuario quedó registrado como pendiente de
            validación; puede reintentarla desde su perfil.
          </p>
        </div>
      )}

      <dl className="usuarios-registrado-datos">
        <div>
          <dt>Identidad</dt>
          <dd>
            <Etiqueta tono={validacion.tono}>{validacion.etiqueta}</Etiqueta>
          </dd>
        </div>
        {predio && (
          <div>
            <dt>Predio</dt>
            <dd>
              <span>
                <span className="cifra">{predio.codigoPredio}</span> · {predio.direccion}, {predio.municipio}
              </span>
              <small>
                Estrato {predio.estrato} · {nombreUsoPredio(predio.uso)}
              </small>
            </dd>
          </div>
        )}
        {registro.servicioSolicitado && (
          <div>
            <dt>Servicio solicitado</dt>
            <dd>{nombreServicio(registro.servicioSolicitado)}</dd>
          </div>
        )}
      </dl>

      <div className="usuarios-registrado-acciones">
        <Boton a={`/gestion/usuarios/${registro.codigoInterno}`} icono="flecha">
          Ver perfil del usuario
        </Boton>
        <Boton variante="secundario" icono="mas" onClick={onOtro}>
          Registrar otro usuario
        </Boton>
      </div>
    </div>
  );
}