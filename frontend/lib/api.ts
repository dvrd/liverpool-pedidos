import { errorFields, responseData } from "@/lib/httpBody";
import { INVALID_RESPONSE_MESSAGE } from "@/lib/messages";
import { ordersPath } from "@/lib/ordersPath";
import type {
  CreateCustomer,
  CreateOrder,
  Customer,
  Order,
  OrderStatus,
  Page,
  Suggestion,
  UpdateOrder,
} from "@/lib/types";

export class ApiError extends Error {
  status: number;
  details?: Record<string, string>;

  constructor(status: number, message: string, details?: Record<string, string>) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.details = details;
  }
}

type Options = {
  method?: string;
  body?: unknown;
  signal?: AbortSignal;
};

async function request<T>(path: string, options: Options = {}): Promise<T> {
  const headers: Record<string, string> = { Accept: "application/json" };
  if (options.body !== undefined) {
    headers["Content-Type"] = "application/json";
  }

  let response: Response;
  try {
    response = await fetch(path, {
      method: options.method ?? "GET",
      headers,
      body: options.body !== undefined ? JSON.stringify(options.body) : undefined,
      signal: options.signal,
      cache: "no-store",
    });
  } catch (error) {
    if (isAbort(error)) {
      throw error;
    }
    throw new ApiError(0, "No hay conexión con el servicio de pedidos.");
  }

  if (response.status === 204) {
    return undefined as T;
  }

  const text = await response.text();
  const body = responseData(text);
  if (text && body === undefined) {
    throw new ApiError(
      response.status,
      response.ok ? INVALID_RESPONSE_MESSAGE : "No se pudo completar la operación.",
    );
  }

  if (!response.ok) {
    const error = errorFields(body);
    throw new ApiError(
      response.status,
      error?.message ?? "No se pudo completar la operación.",
      error?.details,
    );
  }

  return body as T;
}

export function isAbort(error: unknown): boolean {
  return error instanceof DOMException && error.name === "AbortError";
}

export function apiFailure(error: unknown, fallback: string): { message: string; details?: Record<string, string> } {
  if (error instanceof ApiError) {
    return { message: error.message, details: error.details };
  }
  return { message: fallback };
}

export function listOrders(
  filter: { term?: string; status?: string; page?: number },
  signal?: AbortSignal,
) {
  return request<Page<Order>>(ordersPath(filter), { signal });
}

export function suggestOrders(term: string, signal?: AbortSignal) {
  const params = new URLSearchParams({ term });
  return request<Suggestion[]>(`/api/v1/orders/suggestions?${params.toString()}`, { signal });
}

export function getOrder(id: string, signal?: AbortSignal) {
  return request<Order>(`/api/v1/orders/${id}`, { signal });
}

export function createOrder(order: CreateOrder) {
  return request<Order>("/api/v1/orders", { method: "POST", body: order });
}

export function updateOrder(id: string, correction: UpdateOrder) {
  return request<Order>(`/api/v1/orders/${id}`, { method: "PUT", body: correction });
}

export function changeStatus(id: string, status: OrderStatus) {
  return request<Order>(`/api/v1/orders/${id}/status`, { method: "PATCH", body: { status } });
}

export function deleteOrder(id: string) {
  return request<void>(`/api/v1/orders/${id}`, { method: "DELETE" });
}

export function listCustomers() {
  return request<Customer[]>("/api/v1/customers");
}

export function createCustomer(customer: CreateCustomer) {
  return request<Customer>("/api/v1/customers", { method: "POST", body: customer });
}
