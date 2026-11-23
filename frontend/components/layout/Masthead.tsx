import type { ReactNode } from "react";

const DOCS = process.env.NEXT_PUBLIC_DOCS_URL || "http://localhost:8080/swagger-ui.html";

export function Masthead({ children }: { children: ReactNode }) {
  return (
    <>
      <header className="mast">
        <div className="bar">
          <div className="bar-inner">
            <p>Mesa de pedidos</p>
            <p className="bar-channels">Aplicación y sitio web</p>
          </div>
        </div>
        <div className="mast-inner">
          <div>
            <p className="wordmark">liverpool</p>
            <p className="desk-kicker">Equipo de ventas</p>
          </div>
          <p className="channels">Consulta y corrección de pedidos</p>
        </div>
      </header>
      <main id="content" className="content">
        {children}
      </main>
      <footer className="footer">
        <div className="footer-inner">
          <p>Ejercicio técnico para el equipo de ventas. No es un sitio oficial de El Puerto de Liverpool.</p>
          <a href={DOCS}>Documentación del API</a>
        </div>
      </footer>
    </>
  );
}
