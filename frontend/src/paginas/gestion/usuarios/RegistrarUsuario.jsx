import { useRef, useState } from "react";
import { EncabezadoGestion } from "../../../componentes/EncabezadoGestion";
import { MensajeError } from "../../../componentes/MensajeError";
import { enfocarPrimerError } from "../../../componentes/enfocarPrimerError";
import { PieFormulario, SeccionFormulario } from "../../../componentes/SeccionFormulario";
import { Boton } from "../../../componentes/ui/Boton";
import { Campo } from "../../../componentes/ui/Campo";
import { EnlaceVolver } from "../../../componentes/ui/EnlaceVolver";
import { useMunicipios } from "../../../hooks/useTerritorio";
import { useRegistrarUsuario } from "../../../hooks/useUsuarios";
import { TIPOS_DOCUMENTO } from "../../../presentacion/datosPersonales";
import { ESTRATOS, SERVICIOS, TIPOS_PERSONA, USOS_PREDIO } from "../../../presentacion/usuarios";
import { RegistroExitoso } from "./RegistroExitoso";
import { UsuarioExistente } from "./UsuarioExistente";
import { CAMPOS_REGISTRO, LIMITES, USUARIO_VACIO, aSolicitudRegistro, validar, validarCampo } from "./validacionUsuario";
import "./usuarios.css";

/**
 * Registro de un usuario del servicio con su predio (CU-01, RF-01, SWR-12 a SWR-14).
 * - Valida al enviar con las mismas reglas del backend y revalida en vivo cada campo con error.
 * - Si la identificación ya existe (409), ofrece abrir el perfil existente.
 * - Si la RNEC no responde, el backend registra igual y el usuario queda pendiente de validación.
 */
export function RegistrarUsuario() {
  const formulario = useRef(null);
  const [datos, setDatos] = useState(USUARIO_VACIO);
  const [errores, setErrores] = useState({});
  const municipios = useMunicipios();
  const registro = useRegistrarUsuario();

  const actualizar = (campo) => (evento) => {
    const valor = evento.target.value;
    setDatos((anteriores) => ({ ...anteriores, [campo]: valor }));
    if (errores[campo]) {
      setErrores((anteriores) => ({ ...anteriores, [campo]: validarCampo(CAMPOS_REGISTRO[campo], valor) }));
    }
  };

  const propsCampo = (campo) => ({ name: campo, value: datos[campo], error: errores[campo], onChange: actualizar(campo) });

  const registrar = (evento) => {
    evento.preventDefault();
    const erroresCliente = validar(datos, CAMPOS_REGISTRO);
    setErrores(erroresCliente);
    if (Object.keys(erroresCliente).length > 0) {
      enfocarPrimerError(formulario.current);
      return;
    }
    registro.mutate(aSolicitudRegistro(datos), {
      onError: (error) => {
        if (error.esValidacion) {
          setErrores(error.errores);
          enfocarPrimerError(formulario.current);
        }
      },
    });
  };

  const empezarDeNuevo = () => {
    registro.reset();
    setDatos(USUARIO_VACIO);
    setErrores({});
  };

  if (registro.isSuccess) return <RegistroExitoso registro={registro.data} onOtro={empezarDeNuevo} />;

  const duplicado = registro.error?.conflicto ? registro.error : null;
  const errorGeneral = registro.error && !registro.error.esValidacion && !duplicado ? registro.error : null;

  return (
    <>
      <div className="usuarios-volver">
        <EnlaceVolver a="/gestion/usuarios" historial>
          Volver a usuarios
        </EnlaceVolver>
      </div>
      <EncabezadoGestion
        antetitulo="Nuevo usuario"
        titulo="Registrar usuario del servicio"
        descripcion="Los campos marcados con * son obligatorios. La identidad se valida con la Registraduría."
      />

      {duplicado && <UsuarioExistente error={duplicado} />}

      <form ref={formulario} className="formulario-gestion" onSubmit={registrar} noValidate>
        <SeccionFormulario
          numero={1}
          titulo="Identificación y contacto"
          descripcion="El número de identificación no se puede cambiar después del registro."
        >
          <Campo etiqueta="Tipo de identificación" como="select" obligatorio {...propsCampo("tipoDocumento")}>
            {TIPOS_DOCUMENTO.map((tipo) => (
              <option key={tipo.codigo} value={tipo.codigo}>
                {tipo.nombre}
              </option>
            ))}
          </Campo>
          <Campo
            etiqueta="Número de identificación"
            obligatorio
            autoComplete="off"
            maxLength={25}
            placeholder="1094912580"
            {...propsCampo("numeroDocumento")}
          />
          <Campo
            etiqueta="Nombre completo o razón social"
            obligatorio
            maxLength={LIMITES.nombreCompleto}
            {...propsCampo("nombreCompleto")}
          />
          <Campo
            etiqueta="Tipo de persona"
            como="select"
            ayuda="Si no lo indica, el NIT se registra como persona jurídica y los demás como natural."
            {...propsCampo("tipoPersona")}
          >
            <option value="">Según el tipo de identificación</option>
            {TIPOS_PERSONA.map((tipo) => (
              <option key={tipo.codigo} value={tipo.codigo}>
                {tipo.nombre}
              </option>
            ))}
          </Campo>
          <Campo etiqueta="Teléfono" obligatorio type="tel" maxLength={20} placeholder="300 000 0000" {...propsCampo("telefono")} />
          <Campo
            etiqueta="Correo electrónico"
            obligatorio
            type="email"
            maxLength={LIMITES.correo}
            placeholder="nombre@correo.com"
            {...propsCampo("correo")}
          />
        </SeccionFormulario>

        <SeccionFormulario
          numero={2}
          titulo="Predio"
          descripcion="Inmueble donde se prestará el servicio. Solo se registran predios en municipios atendidos por EPQ."
        >
          <Campo
            etiqueta="Municipio"
            como="select"
            obligatorio
            disabled={municipios.isPending || municipios.isError}
            {...propsCampo("municipioId")}
            error={errores.municipioId ?? (municipios.isError ? "No fue posible cargar los municipios." : null)}
          >
            <option value="">{municipios.isPending ? "Cargando…" : "Seleccione el municipio"}</option>
            {municipios.data?.map((municipio) => (
              <option key={municipio.id} value={municipio.id}>
                {municipio.nombre}
              </option>
            ))}
          </Campo>
          <Campo
            etiqueta="Dirección del predio"
            obligatorio
            maxLength={LIMITES.direccion}
            placeholder="Cra 14 # 20-35"
            {...propsCampo("direccionPredio")}
          />
          <Campo etiqueta="Estrato" como="select" obligatorio {...propsCampo("estrato")}>
            <option value="">Seleccione el estrato</option>
            {ESTRATOS.map((estrato) => (
              <option key={estrato} value={estrato}>
                Estrato {estrato}
              </option>
            ))}
          </Campo>
          <Campo etiqueta="Uso del predio" como="select" {...propsCampo("usoPredio")}>
            {USOS_PREDIO.map((uso) => (
              <option key={uso.codigo} value={uso.codigo}>
                {uso.nombre}
              </option>
            ))}
          </Campo>
          <Campo
            etiqueta="Dirección de notificación"
            className="formulario-campo-completo"
            maxLength={LIMITES.direccion}
            placeholder="Si se deja vacía se usa la dirección del predio"
            {...propsCampo("direccionNotificacion")}
          />
        </SeccionFormulario>

        <SeccionFormulario
          numero={3}
          titulo="Servicio solicitado"
          descripcion="Queda como referencia. El contrato se crea después, desde el perfil del usuario."
        >
          <Campo etiqueta="Servicio" como="select" {...propsCampo("servicioSolicitado")}>
            <option value="">Sin servicio por ahora</option>
            {SERVICIOS.map((servicio) => (
              <option key={servicio.codigo} value={servicio.codigo}>
                {servicio.nombre}
              </option>
            ))}
          </Campo>
        </SeccionFormulario>

        <PieFormulario>
          <MensajeError error={errorGeneral} />
          <div className="formulario-pie-acciones">
            <Boton a="/gestion/usuarios" variante="discreto">
              Cancelar
            </Boton>
            <Boton type="submit" icono="check" disabled={registro.isPending}>
              {registro.isPending ? "Registrando y validando…" : "Registrar usuario"}
            </Boton>
          </div>
        </PieFormulario>
      </form>
    </>
  );
}