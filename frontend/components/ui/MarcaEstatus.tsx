import { ETIQUETA_ESTATUS } from "@/lib/formato";
import type { EstatusPedido } from "@/lib/tipos";

export function MarcaEstatus({ estatus }: { estatus: EstatusPedido }) {
  return (
    <span className="marca-estatus" data-estatus={estatus}>
      <span className="cuadro" aria-hidden="true" />
      {ETIQUETA_ESTATUS[estatus]}
    </span>
  );
}
