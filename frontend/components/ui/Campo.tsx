import type { ReactNode } from "react";

type Props = {
  id: string;
  etiqueta: string;
  error?: string;
  className?: string;
  children: ReactNode;
};

export function Campo({ id, etiqueta, error, className, children }: Props) {
  const clases = ["campo", className].filter(Boolean).join(" ");
  return (
    <div className={clases}>
      <label htmlFor={id}>{etiqueta}</label>
      {children}
      {error ? (
        <p className="campo-error" id={`${id}-error`}>
          {error}
        </p>
      ) : null}
    </div>
  );
}
