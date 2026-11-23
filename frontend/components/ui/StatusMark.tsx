import { STATUS_LABEL } from "@/lib/format";
import type { OrderStatus } from "@/lib/types";

export function StatusMark({ status }: { status: OrderStatus }) {
  return (
    <span className="status-mark" data-status={status}>
      <span className="swatch" aria-hidden="true" />
      {STATUS_LABEL[status]}
    </span>
  );
}
