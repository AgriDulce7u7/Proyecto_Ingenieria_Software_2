import { createBrowserRouter, Navigate } from "react-router-dom";
import { BackofficeLayout } from "./componentes/BackofficeLayout";
import { NoEncontrada } from "./componentes/NoEncontrada";
import { Pendiente } from "./componentes/Pendiente";
import { PortalLayout } from "./componentes/PortalLayout";
import { ConsultarEstado } from "./paginas/ciudadano/ConsultarEstado";
import { Inicio } from "./paginas/ciudadano/Inicio";
import { RadicarPqr } from "./paginas/ciudadano/RadicarPqr";
import { Alertas } from "./paginas/gestion/alertas/Alertas";
import { Facturas } from "./paginas/gestion/facturacion/Facturas";
import { Lotes } from "./paginas/gestion/facturacion/Lotes";
import { Programacion } from "./paginas/gestion/facturacion/Programacion";
import { BandejaPqr } from "./paginas/gestion/pqr/BandejaPqr";
import { DetallePqr } from "./paginas/gestion/pqr/detalle/DetallePqr";

/**
 * Mapa de pantallas. Cada ruta indica los requisitos del Plan de Requisitos que respalda
 * (matriz de trazabilidad). Las pantallas <Pendiente> se reemplazan en las siguientes etapas.
 */
export const enrutador = createBrowserRouter([
  {
    path: "/",
    element: <PortalLayout />,
    children: [
      { index: true, element: <Inicio /> },
      // RF-08, CU-06
      { path: "radicar", element: <RadicarPqr /> },
      {
        path: "radicar/constancia",
        element: <Pendiente titulo="Constancia de radicación" requisitos="RF-08, RN-04" />,
      },
      // SWR-08
      { path: "consultar", element: <ConsultarEstado /> },
    ],
  },
  {
    path: "/gestion",
    element: <BackofficeLayout />,
    children: [
      { index: true, element: <Navigate to="pqr" replace /> },

      // F-02 · PQR
      // RF-09, CU-07
      { path: "pqr", element: <BandejaPqr /> },
      // CU-07, DE-02, RN-06, SWR-09
      { path: "pqr/:radicado", element: <DetallePqr /> },
      // SWR-07, RN-04
      { path: "alertas", element: <Alertas /> },

      // F-01 · Facturación
      // SWR-01, SWR-02, RF-05, CU-03
      { path: "facturacion", element: <Programacion /> },
      // RF-05, CU-03, SWR-02, SWR-04
      { path: "facturacion/lotes", element: <Lotes /> },
      {
        path: "facturacion/lotes/:periodo",
        element: <Pendiente titulo="Detalle del lote" requisitos="RF-05, CU-03, RN-02" />,
      },
      // RF-05, SWR-03, SWR-04
      { path: "facturacion/facturas", element: <Facturas /> },
      {
        path: "facturacion/facturas/:numero",
        element: <Pendiente titulo="Detalle de factura" requisitos="SWR-03, SWR-04" />,
      },
    ],
  },
  { path: "*", element: <NoEncontrada /> },
]);