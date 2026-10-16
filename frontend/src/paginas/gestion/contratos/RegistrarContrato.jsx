import { useRef, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { Cargando } from "../../../componentes/Cargando";
import { EncabezadoGestion } from "../../../componentes/EncabezadoGestion";
import { MensajeError } from "../../../componentes/MensajeError";
import { enfocarPrimerError } from "../../../componentes/enfocarPrimerError";
import { PieFormulario, SeccionFormulario } from "../../../componentes/SeccionFormulario";
import { Boton } from "../../../componentes/ui/Boton";
import { Campo } from "../../../componentes/ui/Campo";
import { EnlaceVolver } from "../../../componentes/ui/EnlaceVolver";
import { Icono } from "../../../componentes/ui/Icono";
import { useRegistrarContrato, useTarifaAplicable } from "../../../hooks/useContratos";
import { useUsuario } from "../../../hooks/useUsuarios";
import { PATRON_MEDIDOR } from "../../../presentacion/contratos";
import { SERVICIOS, nombreUsoPredio } from "../../../presentacion/usuarios";
import { TarifaAplicable } from "./TarifaAplicable";
import "./contratos.css";

const CONTRATO_VACIO = { predioId: "", servicio: "", numeroMedidor: "", fechaInstalacionMedidor: "", fechaInicio: "", activar: true };

/** Reglas de RegistrarContratoRequest. Sin medidor, acueducto y gas quedan pendientes (no es un error). */
function validarContrato(datos) {
  const errores = {};
  if (!datos.predioId) errores.predioId = "Seleccione el predio donde se presta el servicio.";
  if (!datos.servicio) errores.servicio = "Seleccione el servicio.";
  if (datos.numeroMedidor.trim() && !PATRON_MEDIDOR.test(datos.numeroMedidor.trim())) {
    errores.numeroMedidor = "Use entre 4 y 50 letras, números o guiones.";
  }
  return errores;
}

/** Cuerpo de POST /api/contratos. */
const aSolicitud = (codigoUsuario, datos) => ({
  codigoUsuario,
  predioId: Number(datos.predioId),
  servicio: datos.servicio,
  numeroMedidor: datos.numeroMedidor.trim() || null,
  fechaInstalacionMedidor: datos.fechaInstalacionMedidor || null,
  fechaInicio: datos.fechaInicio || null,
  activar: datos.activar,
});

/** Predios del usuario como opciones de radio: el contrato siempre se vincula a un predio (RF-04). */
function SelectorPredio({ predios, valor, error, onCambiar }) {
  return (
    <fieldset className="contrato-predios formulario-campo-completo" aria-invalid={error ? true : undefined}>
      <legend>
        Predio <b aria-hidden="true">*</b>
      </legend>
      {predios.map((predio) => (
        <label key={predio.id} className="contrato-predio">
          <input
            type="radio"
            name="predioId"
            value={predio.id}
            checked={valor === String(predio.id)}
            aria-invalid={error ? true : undefined}
            onChange={(evento) => onCambiar(evento.target.value)}
          />
          <span>
            <strong>{predio.direccion}</strong>
            <small>
              <span className="cifra">{predio.codigoPredio}</span> · {predio.municipio} · Estrato {predio.estrato} ·{" "}
              {nombreUsoPredio(predio.uso)}
            </small>
          </span>
        </label>
      ))}
      {error && (
        <small className="campo-error">
          <Icono nombre="alerta" tamano={14} />
          {error}
        </small>
      )}
    </fieldset>
  );
}

/** Formulario del contrato para un usuario ya cargado. */
function FormularioContrato({ usuario }) {
  const navegar = useNavigate();
  const formulario = useRef(null);
  const registro = useRegistrarContrato();
  const [datos, setDatos] = useState({
    ...CONTRATO_VACIO,
    predioId: usuario.predios.length === 1 ? String(usuario.predios[0].id) : "",
  });
  const [errores, setErrores] = useState({});

  const predio = usuario.predios.find((p) => String(p.id) === datos.predioId);
  const servicio = SERVICIOS.find((s) => s.codigo === datos.servicio);
  const quedaraPendiente = datos.activar && servicio?.requiereMedidor && !datos.numeroMedidor.trim();
  // Misma consulta que muestra TarifaAplicable (comparten caché): sin tarifa vigente no se puede registrar
  const tarifa = useTarifaAplicable({ servicio: datos.servicio, municipioId: predio?.municipioId, estrato: predio?.estrato });

  const cambiar = (campo, valor) => {
    setDatos((anteriores) => ({ ...anteriores, [campo]: valor }));
    setErrores((anteriores) => ({ ...anteriores, [campo]: null }));
  };
  const propsCampo = (campo) => ({
    name: campo,
    value: datos[campo],
    error: errores[campo] ?? registro.error?.errores?.[campo],
    onChange: (evento) => cambiar(campo, evento.target.value),
  });

  const registrar = (evento) => {
    evento.preventDefault();
    const erroresCliente = validarContrato(datos);
    setErrores(erroresCliente);
    if (Object.keys(erroresCliente).length > 0) {
      enfocarPrimerError(formulario.current);
      return;
    }
    registro.mutate(aSolicitud(usuario.codigoInterno, datos), {
      onSuccess: (contrato) =>
        navegar(`/gestion/contratos/${contrato.numeroContrato}`, {
          state: { creado: true, advertencias: contrato.advertencias },
        }),
    });
  };

  const errorGeneral = registro.error && !registro.error.esValidacion ? registro.error : null;

  return (
    <form ref={formulario} className="formulario-gestion" onSubmit={registrar} noValidate>
      <SeccionFormulario numero={1} titulo="Predio y servicio" descripcion="Un predio solo puede tener un contrato vigente por servicio.">
        <SelectorPredio
          predios={usuario.predios}
          valor={datos.predioId}
          error={errores.predioId}
          onCambiar={(valor) => cambiar("predioId", valor)}
        />
        <Campo etiqueta="Servicio" como="select" obligatorio {...propsCampo("servicio")}>
          <option value="">Seleccione el servicio</option>
          {SERVICIOS.map((opcion) => (
            <option key={opcion.codigo} value={opcion.codigo}>
              {opcion.nombre}
              {opcion.requiereMedidor ? " (requiere medidor)" : ""}
            </option>
          ))}
        </Campo>
        <TarifaAplicable servicio={datos.servicio} predio={predio} />
      </SeccionFormulario>

      <SeccionFormulario
        numero={2}
        titulo="Medidor y activación"
        descripcion="Acueducto y gas solo se activan con un medidor instalado. Alcantarillado no lo necesita."
      >
        <Campo
          etiqueta="Número de medidor"
          maxLength={50}
          autoComplete="off"
          placeholder="MED-001234"
          ayuda={servicio && !servicio.requiereMedidor ? "Opcional para este servicio." : undefined}
          {...propsCampo("numeroMedidor")}
        />
        <Campo
          etiqueta="Fecha de instalación del medidor"
          type="date"
          ayuda="Si se deja vacía se toma la fecha de hoy."
          disabled={!datos.numeroMedidor.trim()}
          {...propsCampo("fechaInstalacionMedidor")}
        />
        <Campo
          etiqueta="Fecha de inicio del contrato"
          type="date"
          ayuda="Si se deja vacía se toma la fecha de hoy."
          {...propsCampo("fechaInicio")}
        />
        <label className="contrato-casilla">
          <input type="checkbox" checked={datos.activar} onChange={(evento) => cambiar("activar", evento.target.checked)} />
          <span>
            <strong>Activar el contrato al registrarlo</strong>
            <small>Si no se activa, queda pendiente y se activa después desde su detalle.</small>
          </span>
        </label>
        {quedaraPendiente && (
          <div className="aviso aviso-info formulario-campo-completo" role="status">
            <p>
              Sin número de medidor, el contrato de {servicio.nombre.toLowerCase()} quedará <strong>pendiente</strong>{" "}
              de activación.
            </p>
          </div>
        )}
      </SeccionFormulario>

      <PieFormulario>
        <MensajeError error={errorGeneral} />
        <div className="formulario-pie-acciones">
          <Boton a={`/gestion/usuarios/${usuario.codigoInterno}`} variante="discreto">
            Cancelar
          </Boton>
          <Boton type="submit" icono="check" disabled={registro.isPending || tarifa.isError}>
            {registro.isPending ? "Registrando…" : "Registrar contrato"}
          </Boton>
        </div>
      </PieFormulario>
    </form>
  );
}

/**
 * Registro de un contrato de servicio (CU-02, RF-04, RN-01, SWR-17 a SWR-19) para el usuario que llega
 * en la URL (?usuario=USR-000001), con la tarifa que se aplicará según el predio (SWR-18).
 */
export function RegistrarContrato() {
  const [parametros] = useSearchParams();
  const codigo = parametros.get("usuario");

  if (!codigo) {
    return (
      <>
        <EncabezadoGestion antetitulo="Nuevo contrato" titulo="Registrar contrato" />
        <div className="aviso aviso-info" role="status">
          <p>El contrato se crea desde el perfil del usuario. Busque al usuario y use «Nuevo contrato».</p>
        </div>
        <div className="contrato-volver-usuarios">
          <Boton a="/gestion/usuarios" icono="buscar">
            Buscar usuario
          </Boton>
        </div>
      </>
    );
  }

  return <ContratoParaUsuario codigo={codigo} />;
}

/** Carga el usuario (solo cuando hay código en la URL) y muestra el formulario si puede tener contratos. */
function ContratoParaUsuario({ codigo }) {
  const usuario = useUsuario(codigo);

  return (
    <>
      <div className="contratos-volver">
        <EnlaceVolver a={`/gestion/usuarios/${codigo}`} historial>
          Volver al perfil
        </EnlaceVolver>
      </div>
      <EncabezadoGestion
        antetitulo={usuario.data ? `${usuario.data.nombreCompleto} · ${codigo}` : codigo}
        titulo="Registrar contrato"
        descripcion="La tarifa se asigna automáticamente según el servicio, el municipio y el estrato del predio."
      />

      {usuario.isPending && <Cargando texto="Cargando el usuario…" />}
      <MensajeError error={usuario.error} />
      {usuario.data && !usuario.data.activo && (
        <div className="aviso aviso-error" role="alert">
          <p>El usuario está inactivo. Reactívelo desde su perfil antes de registrarle contratos.</p>
        </div>
      )}
      {usuario.data?.activo && usuario.data.predios.length === 0 && (
        <div className="aviso aviso-error" role="alert">
          <p>El usuario no tiene predios. Agregue un predio desde su perfil antes de registrar el contrato.</p>
        </div>
      )}
      {usuario.data?.activo && usuario.data.predios.length > 0 && <FormularioContrato usuario={usuario.data} />}
    </>
  );
}