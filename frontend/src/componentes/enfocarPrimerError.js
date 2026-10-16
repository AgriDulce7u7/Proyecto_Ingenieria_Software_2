/** Mueve el foco al primer campo con error de un formulario para que el usuario sepa qué corregir. */
export function enfocarPrimerError(formulario) {
  requestAnimationFrame(() => formulario?.querySelector('[aria-invalid="true"]')?.focus());
}