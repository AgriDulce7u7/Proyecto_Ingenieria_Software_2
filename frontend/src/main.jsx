import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { RouterProvider } from "react-router-dom";
import { enrutador } from "./rutas";
import { SesionProvider } from "./sesion/SesionProvider";
import "./estilos/base.css";

const clienteConsultas = new QueryClient({
  defaultOptions: {
    queries: {
      // Los errores 4xx (por ejemplo, un radicado inexistente) no se reintentan.
      retry: (intentos, error) => (error?.estado >= 500 || error?.estado === 0) && intentos < 2,
      refetchOnWindowFocus: false,
    },
  },
});

createRoot(document.getElementById("root")).render(
  <StrictMode>
    <QueryClientProvider client={clienteConsultas}>
      <SesionProvider>
        <RouterProvider router={enrutador} />
      </SesionProvider>
    </QueryClientProvider>
  </StrictMode>,
);
