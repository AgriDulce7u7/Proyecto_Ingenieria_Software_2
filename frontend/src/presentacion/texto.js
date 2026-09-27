/** Texto en minúsculas y sin tildes, para que buscar "tramite" encuentre "trámite". */
export const normalizarTexto = (texto = "") =>
  texto
    .normalize("NFD")
    .replace(/\p{Diacritic}/gu, "")
    .toLowerCase();

/** Filtra una lista por texto libre en los campos indicados. Sin texto, devuelve la lista completa. */
export function buscarEn(elementos, texto, campos) {
  const buscado = normalizarTexto(texto.trim());
  if (!buscado) return elementos;
  return elementos.filter((elemento) =>
    campos.some((campo) => normalizarTexto(elemento[campo]).includes(buscado)),
  );
}

/** Muestra solo los últimos 4 caracteres de un documento: "1094000111" → "••• ••• 0111". */
export const enmascararDocumento = (documento = "") =>
  documento.length > 4 ? `••• ••• ${documento.slice(-4)}` : documento;