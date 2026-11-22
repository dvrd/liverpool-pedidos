import type { CanalPedido, EstatusPedido, TipoSugerencia } from "@/lib/tipos";

const moneda = new Intl.NumberFormat("es-MX", {
  style: "currency",
  currency: "MXN",
});

const fecha = new Intl.DateTimeFormat("es-MX", {
  dateStyle: "medium",
  timeStyle: "short",
});

export const ORDEN_ESTATUS: EstatusPedido[] = [
  "RECIBIDO",
  "CONFIRMADO",
  "EN_PREPARACION",
  "ENVIADO",
  "ENTREGADO",
  "CANCELADO",
];

export const ETIQUETA_ESTATUS: Record<EstatusPedido, string> = {
  RECIBIDO: "Recibido",
  CONFIRMADO: "Confirmado",
  EN_PREPARACION: "En preparación",
  ENVIADO: "Enviado",
  ENTREGADO: "Entregado",
  CANCELADO: "Cancelado",
};

export const ETIQUETA_CANAL: Record<CanalPedido, string> = {
  APLICACION: "Aplicación",
  SITIO_WEB: "Sitio web",
};

export const ETIQUETA_TIPO: Record<TipoSugerencia, string> = {
  FOLIO: "Folio",
  CLIENTE: "Cliente",
  CORREO: "Correo",
  PRODUCTO: "Producto",
};

export function formatoMoneda(valor: number): string {
  return moneda.format(valor);
}

export function formatoFecha(iso: string): string {
  const valor = new Date(iso);
  if (Number.isNaN(valor.getTime())) {
    return iso;
  }
  return fecha.format(valor);
}

export function textoConteo(total: number, filtrado: boolean): string {
  if (filtrado) {
    return total === 1 ? "1 coincidencia" : `${total} coincidencias`;
  }
  return total === 1 ? "1 pedido" : `${total} pedidos`;
}

export function aNumero(valor: string): number {
  return Number(valor.replace(",", ".").trim());
}
