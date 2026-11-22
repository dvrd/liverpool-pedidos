import type { ButtonHTMLAttributes, ReactNode } from "react";

type Variante = "primario" | "secundario" | "texto" | "peligro";

type Props = ButtonHTMLAttributes<HTMLButtonElement> & {
  variante?: Variante;
  children: ReactNode;
};

export function Boton({ variante = "primario", className, children, ...resto }: Props) {
  const clases = ["boton", `boton-${variante}`, className].filter(Boolean).join(" ");
  return (
    <button className={clases} {...resto}>
      {children}
    </button>
  );
}
