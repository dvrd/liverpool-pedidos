import type { ReactNode } from "react";

type Props = {
  indice: string;
  titulo: string;
  id: string;
  children: ReactNode;
};

export function Bloque({ indice, titulo, id, children }: Props) {
  return (
    <section className="bloque" id={id} aria-labelledby={`${id}-titulo`} data-testid={id}>
      <header className="bloque-cabeza">
        <span className="bloque-indice">{indice}</span>
        <h2 id={`${id}-titulo`}>{titulo}</h2>
      </header>
      <dl className="datos">{children}</dl>
    </section>
  );
}

export function Dato({ etiqueta, valor }: { etiqueta: string; valor: ReactNode }) {
  return (
    <div className="dato">
      <dt>{etiqueta}</dt>
      <dd>{valor}</dd>
    </div>
  );
}
