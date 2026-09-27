/** Lleva el foco al primer campo con error del formulario, cuando React ya lo marcó como inválido. */
export function enfocarError(formulario) {
  requestAnimationFrame(() => formulario?.querySelector('[aria-invalid="true"]')?.focus());
}