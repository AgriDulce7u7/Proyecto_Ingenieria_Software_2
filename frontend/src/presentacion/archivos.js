/**
 * Reglas de las evidencias de PQR. Replican EvidenciaPqrProperties del backend (PDF, JPG, PNG o Word,
 * máximo 5 MB) para avisar antes de subir; el backend sigue siendo la validación definitiva.
 */

export const EVIDENCIA = {
  tamanoMaximoBytes: 5 * 1024 * 1024,
  extensiones: [".pdf", ".jpg", ".jpeg", ".png", ".doc", ".docx"],
  descripcionMaxima: 300,
};

/** Valor del atributo accept del <input type="file">. */
export const ACEPTA_EVIDENCIA = EVIDENCIA.extensiones.join(",");

/** Valida el archivo elegido. Devuelve el mensaje de error o null. */
export function validarEvidencia(archivo) {
  if (!archivo) return "Seleccione el archivo que desea adjuntar.";
  const nombre = archivo.name.toLowerCase();
  if (!EVIDENCIA.extensiones.some((extension) => nombre.endsWith(extension))) {
    return "Solo se aceptan archivos PDF, JPG, PNG o Word.";
  }
  if (archivo.size > EVIDENCIA.tamanoMaximoBytes) return "El archivo supera el máximo de 5 MB.";
  if (archivo.size === 0) return "El archivo está vacío.";
  return null;
}

/** 2048 → "2 KB", 1572864 → "1,5 MB". */
export function formatearTamano(bytes) {
  if (bytes < 1024) return `${bytes} B`;
  const enKb = bytes / 1024;
  if (enKb < 1024) return `${Math.round(enKb)} KB`;
  return `${(enKb / 1024).toLocaleString("es-CO", { maximumFractionDigits: 1 })} MB`;
}