import { createBrowserRouter, Navigate } from "react-router-dom";
import { BackofficeLayout } from "./componentes/BackofficeLayout";
import { NoEncontrada } from "./componentes/NoEncontrada";
import { Pendiente } from "./componentes/Pendiente";
import { PortalLayout } from "./componentes/PortalLayout";
import { Inicio } from "./paginas/ciudadano/Inicio";

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
      { path: "radicar", element: <Pendiente titulo="Radicar una PQR" requisitos="RF-08, CU-06" /> },
      {
        path: "radicar/constancia",
        element: <Pendiente titulo="Constancia de radicación" requisitos="RF-08, RN-04" />,
      },
      { path: "consultar", element: <Pendiente titulo="Consultar estado" requisitos="SWR-08" /> },
    ],
  },
  {
    path: "/gestion",
    element: <BackofficeLayout />,
    children: [
      { index: true, element: <Navigate to="pqr" replace /> },

      // F-02 · PQR
      { path: "pqr", element: <Pendiente titulo="Bandeja de PQR" requisitos="RF-09, CU-07" /> },
      {
        path: "pqr/:radicado",
        element: <Pendiente titulo="Detalle de PQR" requisitos="CU-07, DE-02, RN-06, SWR-09" />,
      },
      { path: "alertas", element: <Pendiente titulo="Mis alertas" requisitos="SWR-07, RN-04" /> },

      // F-01 · Facturación
      {
        path: "facturacion",
        element: <Pendiente titulo="Programación de facturación" requisitos="SWR-01, SWR-02" />,
      },
      { path: "facturacion/lotes", element: <Pendiente titulo="Lotes de facturación" requisitos="RF-05, CU-03" /> },
      {
        path: "facturacion/lotes/:periodo",
        element: <Pendiente titulo="Detalle del lote" requisitos="RF-05, CU-03, RN-02" />,
      },
      { path: "facturacion/facturas", element: <Pendiente titulo="Facturas" requisitos="RF-05" /> },
      {
        path: "facturacion/facturas/:numero",
        element: <Pendiente titulo="Detalle de factura" requisitos="SWR-03, SWR-04" />,
      },
    ],
  },
  { path: "*", element: <NoEncontrada /> },
]);
