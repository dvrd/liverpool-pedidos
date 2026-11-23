import { OrderDetail } from "@/components/orders/OrderDetail";
import { Suspense } from "react";

export default function Page() {
  return (
    <Suspense fallback={<p className="state">Abriendo el pedido…</p>}>
      <OrderDetail />
    </Suspense>
  );
}
