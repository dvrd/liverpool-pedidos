import type { OrderChannel, OrderStatus, SuggestionType } from "@/lib/types";

const currency = new Intl.NumberFormat("es-MX", {
  style: "currency",
  currency: "MXN",
});

const dateTime = new Intl.DateTimeFormat("es-MX", {
  dateStyle: "medium",
  timeStyle: "short",
});

export const STATUS_ORDER: OrderStatus[] = [
  "RECEIVED",
  "CONFIRMED",
  "PREPARING",
  "SHIPPED",
  "DELIVERED",
  "CANCELLED",
];

export const STATUS_LABEL: Record<OrderStatus, string> = {
  RECEIVED: "Recibido",
  CONFIRMED: "Confirmado",
  PREPARING: "En preparación",
  SHIPPED: "Enviado",
  DELIVERED: "Entregado",
  CANCELLED: "Cancelado",
};

export const CHANNEL_LABEL: Record<OrderChannel, string> = {
  APP: "Aplicación",
  WEBSITE: "Sitio web",
};

export const SUGGESTION_LABEL: Record<SuggestionType, string> = {
  ORDER_NUMBER: "Folio",
  CUSTOMER: "Cliente",
  EMAIL: "Correo",
  PRODUCT: "Producto",
};

export function formatMoney(value: number): string {
  return currency.format(value);
}

export function formatDate(iso: string): string {
  const value = new Date(iso);
  if (Number.isNaN(value.getTime())) {
    return iso;
  }
  return dateTime.format(value);
}

export function countText(total: number, filtered: boolean, shown = total): string {
  const noun = filtered
    ? total === 1 ? "coincidencia" : "coincidencias"
    : total === 1 ? "pedido" : "pedidos";
  if (shown < total) return `${shown} de ${total} ${noun}`;
  if (total === 1) return `1 ${noun}`;
  return `${total} ${noun}`;
}

export function statusOptions() {
  return STATUS_ORDER.map((status) => ({
    value: status,
    label: STATUS_LABEL[status],
    status,
  }));
}

export function channelOptions() {
  return (Object.keys(CHANNEL_LABEL) as OrderChannel[]).map((channel) => ({
    value: channel,
    label: CHANNEL_LABEL[channel],
  }));
}

export function toNumber(value: string): number {
  return Number(value.replace(",", ".").trim());
}
