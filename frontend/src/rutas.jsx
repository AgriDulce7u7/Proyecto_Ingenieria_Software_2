import { createBrowserRouter } from "react-router-dom";
import { BackofficeLayout } from "./componentes/BackofficeLayout";
import { InicioGestion } from "./componentes/InicioGestion";
import { NoEncontrada } from "./componentes/NoEncontrada";
import { PortalLayout } from "./componentes/PortalLayout";
import { RutaProtegida } from "./componentes/RutaProtegida";
import { IniciarSesion } from "./paginas/acceso/IniciarSesion";
import { Constancia } from "./paginas/ciudadano/Constancia";
import { ConsultarEstado } from "./paginas/ciudadano/ConsultarEstado";
import { Inicio } from "./paginas/ciudadano/Inicio";
import { RadicarPqr } from "./paginas/ciudadano/RadicarPqr";
import { Alertas } from "./paginas/gestion/alertas/Alertas";
import { Cumplimiento } from "./paginas/gestion/cumplimiento/Cumplimiento";
import { DetalleLote } from "./paginas/gestion/facturacion/DetalleLote";
import { DetalleFactura } from "./paginas/gestion/facturacion/factura/DetalleFactura";
import { Facturas } from "./paginas/gestion/facturacion/Facturas";
import { Lotes } from "./paginas/gestion/facturacion/Lotes";
import { Programacion } from "./paginas/gestion/facturacion/Programacion";
import { BandejaPqr } from "./paginas/gestion/pqr/BandejaPqr";
import { DetallePqr } from "./paginas/gestion/pqr/detalle/DetallePqr";
import { Supervision } from "./paginas/gestion/supervision/Supervision";
import { PerfilUsuario } from "./paginas/gestion/usuarios/perfil/PerfilUsuario";
import { RegistrarUsuario } from "./paginas/gestion/usuarios/RegistrarUsuario";
import { Usuarios } from "./paginas/gestion/usuarios/Usuarios";

/**
 * Mapa de pantallas. Cada ruta indica los requisitos que respalda, con los identificadores del
 * student book (RF, CU, RN, DE) y del Plan de Requisitos (SWR), para la matriz de trazabilidad.
 *
 * El back-office exige sesión de funcionario y cada pantalla, la capacidad de su rol
 * (sesion/capacidades.js). El backend vuelve a verificar los permisos en cada petición.
 */

/** Pantalla del back-office que exige una capacidad del rol. */
const protegida = (capacidad, pagina) => <RutaProtegida capacidad={capacidad}>{pagina}</RutaProtegida>;

export const enrutador = createBrowserRouter([
  {
    path: "/",
    element: <PortalLayout />,
    children: [
      { index: true, element: <Inicio /> },
      // RF-08, CU-06, SWR-05, SWR-06
      { path: "radicar", element: <RadicarPqr /> },
      // RF-08, SWR-05, SWR-06, RN-04
      { path: "radicar/constancia", element: <Constancia /> },
      // RF-10, CU-07, SWR-08
      { path: "consultar", element: <ConsultarEstado /> },
      // RF-14, RNF-05
      { path: "acceso", element: <IniciarSesion /> },
    ],
  },
  {
    path: "/gestion",
    element: (
      <RutaProtegida>
        <BackofficeLayout />
      </RutaProtegida>
    ),
    children: [
      { index: true, element: <InicioGestion /> },

      // F-02 · PQR
      // RF-09, CU-06
      { path: "pqr", element: protegida("PQR_ATENDER", <BandejaPqr />) },
      // RF-09, CU-06, DE-02, RN-06, SWR-09
      { path: "pqr/:radicado", element: protegida("PQR_ATENDER", <DetallePqr />) },
      // RF-09, SWR-07, RN-04
      { path: "alertas", element: protegida("PQR_ATENDER", <Alertas />) },
      // StR-02c, SWR-07, RN-04
      { path: "supervision", element: protegida("PQR_SUPERVISAR", <Supervision />) },
      // SyR-02f, RN-04
      { path: "cumplimiento", element: protegida("PQR_INDICADORES", <Cumplimiento />) },
            // F-03 · Usuarios del servicio
      // RF-03, SWR-16
      { path: "usuarios", element: protegida("COMERCIAL_CONSULTAR", <Usuarios />) },
      // RF-01, CU-01, SWR-12, SWR-13, SWR-14
      { path: "usuarios/nuevo", element: protegida("COMERCIAL_GESTIONAR", <RegistrarUsuario />) },
      // RF-02, RF-03, SWR-14, SWR-15
      { path: "usuarios/:codigo", element: protegida("COMERCIAL_CONSULTAR", <PerfilUsuario />) },
      // F-01 · Facturación
      // RF-05, CU-03, SWR-01, SWR-02
      { path: "facturacion", element: protegida("FACTURACION_CONSULTAR", <Programacion />) },
      // RF-05, CU-03, SWR-02, SWR-04
      { path: "facturacion/lotes", element: protegida("FACTURACION_CONSULTAR", <Lotes />) },
      // RF-05, CU-03, SWR-02, SWR-04
      { path: "facturacion/lotes/:periodo", element: protegida("FACTURACION_CONSULTAR", <DetalleLote />) },
      // RF-05, SWR-03, SWR-04
      { path: "facturacion/facturas", element: protegida("FACTURACION_CONSULTAR", <Facturas />) },
      // RF-05, SWR-03, SWR-04
      { path: "facturacion/facturas/:numero", element: protegida("FACTURACION_CONSULTAR", <DetalleFactura />) },
    ],
  },
  { path: "*", element: <NoEncontrada /> },
]);