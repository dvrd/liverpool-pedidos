import { OrderDesk } from "@/components/orders/OrderDesk";
import { Suspense } from "react";

export default function Page() {
  return (
    <Suspense fallback={<p className="state">Cargando la mesa…</p>}>
      <OrderDesk />
    </Suspense>
  );
}
