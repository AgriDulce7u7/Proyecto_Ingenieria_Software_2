import { useState } from "react";
import { MensajeError } from "../../../../componentes/MensajeError";
import { Boton } from "../../../../componentes/ui/Boton";
import { Campo } from "../../../../componentes/ui/Campo";
import { enfocarPrimerError } from "../../../../componentes/enfocarPrimerError";
import { useMunicipios } from "../../../../hooks/useTerritorio";
import { useRegistrarPredio } from "../../../../hooks/useUsuarios";
import { ESTRATOS, USOS_PREDIO } from "../../../../presentacion/usuarios";
import { CAMPOS_PREDIO, LIMITES, aSolicitudPredio, validar } from "../validacionUsuario";

/** Registro de otro predio del usuario (un usuario puede tener servicios en varios inmuebles). */
export function DialogoPredio({ usuario, onListo, onCancelar }) {
  const municipios = useMunicipios();
  const registro = useRegistrarPredio(usuario.codigoInterno);
  const [datos, setDatos] = useState({
    direccion: "",
    municipioId: usuario.municipioId ? String(usuario.municipioId) : "",
    estrato: "",
    uso: "RESIDENCIAL",
  });
  const [errores, setErrores] = useState({});

  const propsCampo = (campo) => ({
    value: datos[campo],
    error: errores[campo] ?? registro.error?.errores?.[campo],
    onChange: (evento) => {
      setDatos((anteriores) => ({ ...anteriores, [campo]: evento.target.value }));
      setErrores((anteriores) => ({ ...anteriores, [campo]: null }));
    },
  });

  const guardar = (evento) => {
    evento.preventDefault();
    const erroresCliente = validar(datos, CAMPOS_PREDIO);
    setErrores(erroresCliente);
    if (Object.keys(erroresCliente).length > 0) {
      enfocarPrimerError(evento.currentTarget);
      return;
    }
    registro.mutate(aSolicitudPredio(datos), {
      onSuccess: (predio) => onListo(`Se registró el predio ${predio.codigoPredio}.`),
    });
  };

  return (
    <form className="dialogo-formulario" onSubmit={guardar} noValidate>
      <Campo etiqueta="Dirección del predio" obligatorio maxLength={LIMITES.direccion} placeholder="Cra 14 # 20-35" {...propsCampo("direccion")} />
      <Campo etiqueta="Municipio" como="select" obligatorio disabled={municipios.isPending} {...propsCampo("municipioId")}>
        <option value="">Seleccione el municipio</option>
        {municipios.data?.map((municipio) => (
          <option key={municipio.id} value={municipio.id}>
            {municipio.nombre}
          </option>
        ))}
      </Campo>
      <div className="perfil-dialogo-fila">
        <Campo etiqueta="Estrato" como="select" obligatorio {...propsCampo("estrato")}>
          <option value="">Seleccione</option>
          {ESTRATOS.map((estrato) => (
            <option key={estrato} value={estrato}>
              Estrato {estrato}
            </option>
          ))}
        </Campo>
        <Campo etiqueta="Uso" como="select" {...propsCampo("uso")}>
          {USOS_PREDIO.map((uso) => (
            <option key={uso.codigo} value={uso.codigo}>
              {uso.nombre}
            </option>
          ))}
        </Campo>
      </div>
      {registro.error && !registro.error.esValidacion && <MensajeError error={registro.error} />}
      <div className="dialogo-acciones">
        <Boton variante="discreto" onClick={onCancelar}>
          Cancelar
        </Boton>
        <Boton type="submit" disabled={registro.isPending}>
          {registro.isPending ? "Registrando…" : "Registrar predio"}
        </Boton>
      </div>
    </form>
  );
}