import type { ReactNode } from "react";

const DOCS = process.env.NEXT_PUBLIC_DOCS_URL || "http://localhost:8080/swagger-ui.html";

export function Mastil({ children }: { children: ReactNode }) {
  return (
    <>
      <header className="mastil">
        <div className="mastil-interior">
          <div>
            <p className="marca">Liverpool</p>
            <p className="mesa">Mesa de pedidos</p>
          </div>
          <p className="canales">Aplicación y sitio web</p>
        </div>
      </header>
      <main id="contenido" className="contenido">
        {children}
      </main>
      <footer className="pie">
        <p>Ejercicio técnico para el equipo de ventas. No es un sitio oficial de El Puerto de Liverpool.</p>
        <a href={DOCS}>Documentación del API</a>
      </footer>
    </>
  );
}
