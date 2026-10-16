/** Íconos de trazo (24x24) del sistema visual. Decorativos: siempre aria-hidden. */
const TRAZOS = {
  gota: <path d="M12 2.5S5.5 9.3 5.5 14.2a6.5 6.5 0 0 0 13 0C18.5 9.3 12 2.5 12 2.5Z" />,
  flecha: (
    <>
      <path d="M5 12h14" />
      <path d="m14 7 5 5-5 5" />
    </>
  ),
  volver: (
    <>
      <path d="M19 12H5" />
      <path d="m10 7-5 5 5 5" />
    </>
  ),
  salir: (
    <>
      <path d="M15 4h4v16h-4" />
      <path d="M10 8l-4 4 4 4M6 12h10" />
    </>
  ),
  candado: (
    <>
      <rect x="5" y="10" width="14" height="10" rx="2" />
      <path d="M8 10V7a4 4 0 0 1 8 0v3" />
    </>
  ),
  ojo: (
    <>
      <path d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7S2 12 2 12Z" />
      <circle cx="12" cy="12" r="3" />
    </>
  ),
  adjunto: <path d="m20 11-8.5 8.5a5 5 0 0 1-7-7L13 4a3.5 3.5 0 0 1 5 5l-8.5 8.5a2 2 0 0 1-3-3L14 7" />,
  descargar: (
    <>
      <path d="M12 4v11M7 10l5 5 5-5" />
      <path d="M5 20h14" />
    </>
  ),
    grafica: (
    <>
      <path d="M4 4v16h16" />
      <path d="M8 16v-4M12 16V8M16 16v-6" />
    </>
  ),
  buscar: (
    <>
      <circle cx="11" cy="11" r="7" />
      <path d="m20 20-4-4" />
    </>
  ),
  documento: (
    <>
      <path d="M6 3h8l4 4v14H6z" />
      <path d="M14 3v5h5M9 13h6M9 17h4" />
    </>
  ),
  reloj: (
    <>
      <circle cx="12" cy="12" r="9" />
      <path d="M12 7v5l3 2" />
    </>
  ),
  campana: (
    <>
      <path d="M18 8a6 6 0 0 0-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9" />
      <path d="M10 21h4" />
    </>
  ),
  recibo: (
    <>
      <path d="M6 3v18l3-2 3 2 3-2 3 2V3l-3 2-3-2-3 2z" />
      <path d="M9 10h6M9 14h6" />
    </>
  ),
  capas: (
    <>
      <path d="m12 3 9 5-9 5-9-5z" />
      <path d="m3 12 9 5 9-5M3 16l9 5 9-5" />
    </>
  ),
  personas: (
    <>
      <circle cx="9" cy="8" r="3" />
      <path d="M3 20c0-4 2-6 6-6s6 2 6 6" />
      <path d="M16 5a3 3 0 0 1 0 6M17 14c3 0 4 2 4 5" />
    </>
  ),
  menu: <path d="M4 7h16M4 12h16M4 17h16" />,
  check: <path d="m5 12 4 4L19 6" />,
  alerta: (
    <>
      <path d="M12 3 2.8 20h18.4z" />
      <path d="M12 9v4M12 17h.01" />
    </>
  ),
  sincronizar: (
    <>
      <path d="M20 7h-5V2M4 17h5v5" />
      <path d="M18.5 11a7 7 0 0 0-12-4L4 9M5.5 13a7 7 0 0 0 12 4l2.5-2" />
    </>
  ),
  chevron: <path d="m9 18 6-6-6-6" />,
  mas: <path d="M12 5v14M5 12h14" />,
  cerrar: <path d="m6 6 12 12M18 6 6 18" />,
  inicio: (
    <>
      <path d="m3 11 9-8 9 8" />
      <path d="M5 10v11h14V10M9 21v-7h6v7" />
    </>
  ),
};

export function Icono({ nombre, tamano = 20 }) {
  return (
    <svg
      className="icono"
      width={tamano}
      height={tamano}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.8"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      {TRAZOS[nombre]}
    </svg>
  );
}