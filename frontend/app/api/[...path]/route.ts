import { apiUrl } from "@/lib/apiUrl";
import { NextRequest } from "next/server";

export const dynamic = "force-dynamic";

const WAIT_MS = 75_000;

type Context = { params: Promise<{ path: string[] }> };

function target(): string {
  const configured = process.env.INTERNAL_API_URL?.trim();
  return (configured || "http://127.0.0.1:8080").replace(/\/$/, "");
}

function unavailable() {
  return Response.json(
    { message: "El servicio de pedidos no está disponible." },
    { status: 502 },
  );
}

async function proxy(request: NextRequest, context: Context) {
  const { path } = await context.params;
  const url = apiUrl(target(), path, request.nextUrl.search);
  if (!url) {
    return Response.json({ message: "No encontrado" }, { status: 404 });
  }
  const headers = new Headers();
  const contentType = request.headers.get("content-type");
  if (contentType) headers.set("content-type", contentType);
  headers.set("accept", request.headers.get("accept") ?? "application/json");

  const init: RequestInit = {
    method: request.method,
    headers,
    cache: "no-store",
  };
  if (request.method !== "GET" && request.method !== "HEAD") {
    init.body = await request.arrayBuffer();
  }

  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), WAIT_MS);
  try {
    const response = await fetch(url, { ...init, signal: controller.signal });
    if (response.status === 204) {
      return new Response(null, { status: 204 });
    }
    const type = response.headers.get("content-type") ?? "";
    if (type.includes("text/html")) {
      return unavailable();
    }
    const body = await response.arrayBuffer();
    return new Response(body, {
      status: response.status,
      headers: {
        "content-type": type || "application/json",
      },
    });
  } catch {
    return unavailable();
  } finally {
    clearTimeout(timer);
  }
}

export const GET = proxy;
export const POST = proxy;
export const PUT = proxy;
export const PATCH = proxy;
export const DELETE = proxy;
