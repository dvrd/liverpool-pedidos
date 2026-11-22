import { VistaPedido } from "@/components/pedidos/VistaPedido";
import { Suspense } from "react";

export default function Page() {
  return (
    <Suspense fallback={<p className="estado">Abriendo el pedido…</p>}>
      <VistaPedido />
    </Suspense>
  );
}
