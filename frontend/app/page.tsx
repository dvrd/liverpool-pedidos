import { MesaPedidos } from "@/components/pedidos/MesaPedidos";
import { Suspense } from "react";

export default function Page() {
  return (
    <Suspense fallback={<p className="estado">Cargando la mesa…</p>}>
      <MesaPedidos />
    </Suspense>
  );
}
