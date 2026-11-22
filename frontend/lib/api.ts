import type {
  AltaPedido,
  AltaUsuario,
  CorreccionPedido,
  EstatusPedido,
  Pagina,
  Pedido,
  Sugerencia,
  Usuario,
} from "@/lib/tipos";

export class ErrorApi extends Error {
  estatus: number;
  detalles?: Record<string, string>;

  constructor(estatus: number, mensaje: string, detalles?: Record<string, string>) {
    super(mensaje);
    this.name = "ErrorApi";
    this.estatus = estatus;
    this.detalles = detalles;
  }
}

type Opciones = {
  method?: string;
  body?: unknown;
  signal?: AbortSignal;
};

async function solicitar<T>(ruta: string, opciones: Opciones = {}): Promise<T> {
  const headers: Record<string, string> = { Accept: "application/json" };
  if (opciones.body !== undefined) {
    headers["Content-Type"] = "application/json";
  }

  let respuesta: Response;
  try {
    respuesta = await fetch(ruta, {
      method: opciones.method ?? "GET",
      headers,
      body: opciones.body !== undefined ? JSON.stringify(opciones.body) : undefined,
      signal: opciones.signal,
      cache: "no-store",
    });
  } catch (error) {
    if (error instanceof DOMException && error.name === "AbortError") {
      throw error;
    }
    throw new ErrorApi(0, "No hay conexión con el servicio de pedidos.");
  }

  if (respuesta.status === 204) {
    return undefined as T;
  }

  const texto = await respuesta.text();
  const cuerpo = texto ? (JSON.parse(texto) as { mensaje?: string; detalles?: Record<string, string> }) : null;

  if (!respuesta.ok) {
    throw new ErrorApi(
      respuesta.status,
      cuerpo?.mensaje ?? "No se pudo completar la operación.",
      cuerpo?.detalles,
    );
  }

  return cuerpo as T;
}

export function listarPedidos(filtro: { termino?: string; estatus?: string }, signal?: AbortSignal) {
  const params = new URLSearchParams({ tamano: "50" });
  if (filtro.termino) params.set("termino", filtro.termino);
  if (filtro.estatus) params.set("estatus", filtro.estatus);
  return solicitar<Pagina<Pedido>>(`/api/v1/pedidos?${params.toString()}`, { signal });
}

export function sugerirPedidos(termino: string, signal?: AbortSignal) {
  const params = new URLSearchParams({ termino });
  return solicitar<Sugerencia[]>(`/api/v1/pedidos/sugerencias?${params.toString()}`, { signal });
}

export function obtenerPedido(id: string, signal?: AbortSignal) {
  return solicitar<Pedido>(`/api/v1/pedidos/${id}`, { signal });
}

export function crearPedido(alta: AltaPedido) {
  return solicitar<Pedido>("/api/v1/pedidos", { method: "POST", body: alta });
}

export function actualizarPedido(id: string, correccion: CorreccionPedido) {
  return solicitar<Pedido>(`/api/v1/pedidos/${id}`, { method: "PUT", body: correccion });
}

export function cambiarEstatus(id: string, estatus: EstatusPedido) {
  return solicitar<Pedido>(`/api/v1/pedidos/${id}/estatus`, { method: "PATCH", body: { estatus } });
}

export function eliminarPedido(id: string) {
  return solicitar<void>(`/api/v1/pedidos/${id}`, { method: "DELETE" });
}

export function listarUsuarios() {
  return solicitar<Usuario[]>("/api/v1/usuarios");
}

export function crearUsuario(alta: AltaUsuario) {
  return solicitar<Usuario>("/api/v1/usuarios", { method: "POST", body: alta });
}
