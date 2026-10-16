import { lazy } from "react";
import { createBrowserRouter } from "react-router-dom";
import { BackofficeLayout } from "./componentes/BackofficeLayout";
import { InicioGestion } from "./componentes/InicioGestion";
import { NoEncontrada } from "./componentes/NoEncontrada";
import { PortalLayout } from "./componentes/PortalLayout";
import { RutaCiudadano } from "./componentes/RutaCiudadano";
import { RutaProtegida } from "./componentes/RutaProtegida";
import { Inicio } from "./paginas/ciudadano/Inicio";

/**
 * Mapa de pantallas. Cada ruta indica los requisitos que respalda, con los identificadores del
 * student book (RF, CU, RN, DE) y del Plan de Requisitos (SWR), para la matriz de trazabilidad.
 *
 * El back-office exige sesión de funcionario y cada pantalla, la capacidad de su rol
 * (sesion/capacidades.js). El backend vuelve a verificar los permisos en cada petición.
 *
 * Carga diferida: los layouts y la página de inicio van en el paquete principal; cada pantalla se
 * descarga la primera vez que se abre, en su propio archivo. Así un ciudadano no descarga el
 * back-office y la primera carga es más liviana. Mientras llega, el layout muestra CargaPagina.
 */

/** Pantalla que se descarga al abrirla. `nombre` es el componente que exporta el módulo. */
const diferida = (importar, nombre) => lazy(() => importar().then((modulo) => ({ default: modulo[nombre] })));

// Portal ciudadano
const RadicarPqr = diferida(() => import("./paginas/ciudadano/RadicarPqr"), "RadicarPqr");
const Constancia = diferida(() => import("./paginas/ciudadano/Constancia"), "Constancia");
const ConsultarEstado = diferida(() => import("./paginas/ciudadano/ConsultarEstado"), "ConsultarEstado");
const IniciarSesion = diferida(() => import("./paginas/acceso/IniciarSesion"), "IniciarSesion");
const IngresarCiudadano = diferida(() => import("./paginas/ciudadano/cuenta/IngresarCiudadano"), "IngresarCiudadano");
const ActivarCuenta = diferida(() => import("./paginas/ciudadano/cuenta/ActivarCuenta"), "ActivarCuenta");
const CuentaLayout = diferida(() => import("./paginas/ciudadano/cuenta/CuentaLayout"), "CuentaLayout");
const Resumen = diferida(() => import("./paginas/ciudadano/cuenta/Resumen"), "Resumen");
const ContratoCiudadano = diferida(() => import("./paginas/ciudadano/cuenta/ContratoCiudadano"), "ContratoCiudadano");
const FacturasCiudadano = diferida(() => import("./paginas/ciudadano/cuenta/FacturasCiudadano"), "FacturasCiudadano");
const FacturaCiudadano = diferida(() => import("./paginas/ciudadano/cuenta/FacturaCiudadano"), "FacturaCiudadano");
const PagarFactura = diferida(() => import("./paginas/ciudadano/cuenta/PagarFactura"), "PagarFactura");
const PagosCiudadano = diferida(() => import("./paginas/ciudadano/cuenta/PagosCiudadano"), "PagosCiudadano");
const ComprobanteCiudadano = diferida(
  () => import("./paginas/ciudadano/cuenta/ComprobanteCiudadano"),
  "ComprobanteCiudadano",
);
const MisDatos = diferida(() => import("./paginas/ciudadano/cuenta/MisDatos"), "MisDatos");

// Back-office
const Dashboard = diferida(() => import("./paginas/gestion/dashboard/Dashboard"), "Dashboard");
const BandejaPqr = diferida(() => import("./paginas/gestion/pqr/BandejaPqr"), "BandejaPqr");
const DetallePqr = diferida(() => import("./paginas/gestion/pqr/detalle/DetallePqr"), "DetallePqr");
const Alertas = diferida(() => import("./paginas/gestion/alertas/Alertas"), "Alertas");
const Supervision = diferida(() => import("./paginas/gestion/supervision/Supervision"), "Supervision");
const Cumplimiento = diferida(() => import("./paginas/gestion/cumplimiento/Cumplimiento"), "Cumplimiento");
const Usuarios = diferida(() => import("./paginas/gestion/usuarios/Usuarios"), "Usuarios");
const RegistrarUsuario = diferida(() => import("./paginas/gestion/usuarios/RegistrarUsuario"), "RegistrarUsuario");
const PerfilUsuario = diferida(() => import("./paginas/gestion/usuarios/perfil/PerfilUsuario"), "PerfilUsuario");
const Contratos = diferida(() => import("./paginas/gestion/contratos/Contratos"), "Contratos");
const RegistrarContrato = diferida(() => import("./paginas/gestion/contratos/RegistrarContrato"), "RegistrarContrato");
const DetalleContrato = diferida(() => import("./paginas/gestion/contratos/detalle/DetalleContrato"), "DetalleContrato");
const Programacion = diferida(() => import("./paginas/gestion/facturacion/Programacion"), "Programacion");
const Lotes = diferida(() => import("./paginas/gestion/facturacion/Lotes"), "Lotes");
const DetalleLote = diferida(() => import("./paginas/gestion/facturacion/DetalleLote"), "DetalleLote");
const Facturas = diferida(() => import("./paginas/gestion/facturacion/Facturas"), "Facturas");
const DetalleFactura = diferida(() => import("./paginas/gestion/facturacion/factura/DetalleFactura"), "DetalleFactura");
const Pagos = diferida(() => import("./paginas/gestion/pagos/Pagos"), "Pagos");
const RegistrarPago = diferida(() => import("./paginas/gestion/pagos/RegistrarPago"), "RegistrarPago");
const Comprobante = diferida(() => import("./paginas/gestion/pagos/Comprobante"), "Comprobante");
const PagarCuota = diferida(() => import("./paginas/gestion/cartera/acuerdos/PagarCuota"), "PagarCuota");
const CarteraMorosa = diferida(() => import("./paginas/gestion/cartera/CarteraMorosa"), "CarteraMorosa");
const Acuerdos = diferida(() => import("./paginas/gestion/cartera/acuerdos/Acuerdos"), "Acuerdos");
const RegistrarAcuerdo = diferida(() => import("./paginas/gestion/cartera/acuerdos/RegistrarAcuerdo"), "RegistrarAcuerdo");
const DetalleAcuerdo = diferida(() => import("./paginas/gestion/cartera/acuerdos/DetalleAcuerdo"), "DetalleAcuerdo");
const CobroCartera = diferida(() => import("./paginas/gestion/cartera/CobroCartera"), "CobroCartera");
const Funcionarios = diferida(() => import("./paginas/gestion/administracion/Funcionarios"), "Funcionarios");
const RegistrarFuncionario = diferida(
  () => import("./paginas/gestion/administracion/RegistrarFuncionario"),
  "RegistrarFuncionario",
);
const DetalleFuncionario = diferida(
  () => import("./paginas/gestion/administracion/DetalleFuncionario"),
  "DetalleFuncionario",
);
const Roles = diferida(() => import("./paginas/gestion/administracion/Roles"), "Roles");
const Auditoria = diferida(() => import("./paginas/gestion/administracion/Auditoria"), "Auditoria");

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

      // F-08 · Dashboard gerencial
      // RF-13, CU-09, SWR-33, SWR-34, SWR-35
      { path: "dashboard", element: protegida("DASHBOARD", <Dashboard />) },

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
      // CU-08, RF-12 (pago de una cuota de acuerdo en ventanilla)
      { path: "pagos/cuota", element: protegida("CARTERA_PAGAR_CUOTA", <PagarCuota />) },
      // RF-06, SWR-22
      { path: "pagos/:codigo", element: protegida("FACTURACION_CONSULTAR", <Comprobante />) },

      // F-07 · Cobro persuasivo
      // RF-11, CU-08, SWR-29, SWR-32, RN-03
      { path: "cartera", element: protegida("CARTERA_REPORTE", <CarteraMorosa />) },
      // RF-12, CU-08, RN-07
      { path: "cartera/acuerdos", element: protegida("CARTERA_GESTIONAR", <Acuerdos />) },
      // RF-12, CU-08, SWR-30, RN-07
      { path: "cartera/acuerdos/nuevo", element: protegida("CARTERA_GESTIONAR", <RegistrarAcuerdo />) },
      // RF-12, CU-08, RN-07
      { path: "cartera/acuerdos/:numero", element: protegida("CARTERA_GESTIONAR", <DetalleAcuerdo />) },
      // RN-07, SWR-31, SWR-32
      { path: "cartera/cobro", element: protegida("CARTERA_GESTIONAR", <CobroCartera />) },

      // Administración
      // RF-14, CU-10, RNF-05
      { path: "administracion/funcionarios", element: protegida("ADMINISTRACION", <Funcionarios />) },
      // RF-14, CU-10
      { path: "administracion/funcionarios/nuevo", element: protegida("ADMINISTRACION", <RegistrarFuncionario />) },
      // RF-14, RF-15, CU-10
      { path: "administracion/funcionarios/:id", element: protegida("ADMINISTRACION", <DetalleFuncionario />) },
      // RF-14, RNF-05
      { path: "administracion/roles", element: protegida("ADMINISTRACION", <Roles />) },
      // RF-15, CU-11
      { path: "administracion/auditoria", element: protegida("ADMINISTRACION", <Auditoria />) },
    ],
  },
  { path: "*", element: <NoEncontrada /> },
]);