import { createBrowserRouter } from "react-router-dom";
import { BackofficeLayout } from "./componentes/BackofficeLayout";
import { InicioGestion } from "./componentes/InicioGestion";
import { NoEncontrada } from "./componentes/NoEncontrada";
import { PortalLayout } from "./componentes/PortalLayout";
import { RutaCiudadano } from "./componentes/RutaCiudadano";
import { RutaProtegida } from "./componentes/RutaProtegida";
import { IniciarSesion } from "./paginas/acceso/IniciarSesion";
import { Constancia } from "./paginas/ciudadano/Constancia";
import { ActivarCuenta } from "./paginas/ciudadano/cuenta/ActivarCuenta";
import { ComprobanteCiudadano } from "./paginas/ciudadano/cuenta/ComprobanteCiudadano";
import { ContratoCiudadano } from "./paginas/ciudadano/cuenta/ContratoCiudadano";
import { CuentaLayout } from "./paginas/ciudadano/cuenta/CuentaLayout";
import { FacturaCiudadano } from "./paginas/ciudadano/cuenta/FacturaCiudadano";
import { FacturasCiudadano } from "./paginas/ciudadano/cuenta/FacturasCiudadano";
import { IngresarCiudadano } from "./paginas/ciudadano/cuenta/IngresarCiudadano";
import { MisDatos } from "./paginas/ciudadano/cuenta/MisDatos";
import { PagarFactura } from "./paginas/ciudadano/cuenta/PagarFactura";
import { PagosCiudadano } from "./paginas/ciudadano/cuenta/PagosCiudadano";
import { Resumen } from "./paginas/ciudadano/cuenta/Resumen";
import { ConsultarEstado } from "./paginas/ciudadano/ConsultarEstado";
import { Inicio } from "./paginas/ciudadano/Inicio";
import { RadicarPqr } from "./paginas/ciudadano/RadicarPqr";
import { Alertas } from "./paginas/gestion/alertas/Alertas";
import { Contratos } from "./paginas/gestion/contratos/Contratos";
import { DetalleContrato } from "./paginas/gestion/contratos/detalle/DetalleContrato";
import { RegistrarContrato } from "./paginas/gestion/contratos/RegistrarContrato";
import { Cumplimiento } from "./paginas/gestion/cumplimiento/Cumplimiento";
import { DetalleLote } from "./paginas/gestion/facturacion/DetalleLote";
import { DetalleFactura } from "./paginas/gestion/facturacion/factura/DetalleFactura";
import { Facturas } from "./paginas/gestion/facturacion/Facturas";
import { Lotes } from "./paginas/gestion/facturacion/Lotes";
import { Programacion } from "./paginas/gestion/facturacion/Programacion";
import { Comprobante } from "./paginas/gestion/pagos/Comprobante";
import { Pagos } from "./paginas/gestion/pagos/Pagos";
import { RegistrarPago } from "./paginas/gestion/pagos/RegistrarPago";
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

      // F-06 · Portal de autogestión ciudadana (IU-01)
      // RF-07, CU-05
      { path: "ingresar", element: <IngresarCiudadano /> },
      // CU-01 (autorregistro en el portal)
      { path: "activar-cuenta", element: <ActivarCuenta /> },
      {
        path: "mi-cuenta",
        element: (
          <RutaCiudadano>
            <CuentaLayout />
          </RutaCiudadano>
        ),
        children: [
          // RF-07, CU-05
          { index: true, element: <Resumen /> },
          // RF-07, CU-05, SWR-25, SWR-26, SWR-28
          { path: "contratos/:numero", element: <ContratoCiudadano /> },
          // RF-07, SWR-25, SWR-27
          { path: "facturas", element: <FacturasCiudadano /> },
          // RF-07, SWR-25, SWR-27
          { path: "facturas/:numero", element: <FacturaCiudadano /> },
          // RF-06, CU-04, RN-05, SWR-21, SWR-22, SWR-23
          { path: "facturas/:numero/pagar", element: <PagarFactura /> },
          // RF-06, SWR-22, SWR-24
          { path: "pagos", element: <PagosCiudadano /> },
          // SWR-22
          { path: "pagos/:codigo", element: <ComprobanteCiudadano /> },
          // RF-07 (actualización de datos de contacto)
          { path: "datos", element: <MisDatos /> },
        ],
      },
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

      // F-04 · Contratos de servicio
      // RF-04, CU-02, SWR-20
      { path: "contratos", element: protegida("COMERCIAL_CONSULTAR", <Contratos />) },
      // RF-04, CU-02, RN-01, RN-08, SWR-17, SWR-18, SWR-19
      { path: "contratos/nuevo", element: protegida("COMERCIAL_GESTIONAR", <RegistrarContrato />) },
      // RF-04, CU-02, SWR-18, SWR-19, SWR-20
      { path: "contratos/:numero", element: protegida("COMERCIAL_CONSULTAR", <DetalleContrato />) },

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

      // F-05 · Pagos
      // RF-06, CU-04, SWR-24
      { path: "pagos", element: protegida("FACTURACION_CONSULTAR", <Pagos />) },
      // RF-06, CU-04, RN-05, SWR-22, SWR-23, SWR-24
      { path: "pagos/nuevo", element: protegida("FACTURACION_OPERAR", <RegistrarPago />) },
      // RF-06, SWR-22
      { path: "pagos/:codigo", element: protegida("FACTURACION_CONSULTAR", <Comprobante />) },
    ],
  },
  { path: "*", element: <NoEncontrada /> },
]);