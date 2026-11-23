import { NextRequest } from "next/server";

export const dynamic = "force-dynamic";

// Un servicio gratuito de Render tarda cerca de un minuto en despertar.
const ESPERA_MS = 75_000;

type Contexto = { params: Promise<{ ruta: string[] }> };

function destino(): string {
  const configurado = process.env.API_INTERNA_URL?.trim();
  return (configurado || "http://127.0.0.1:8080").replace(/\/$/, "");
}

function noDisponible() {
  return Response.json(
    { mensaje: "El servicio de pedidos no está disponible." },
    { status: 502 },
  );
}

async function proxy(request: NextRequest, contexto: Contexto) {
  const { ruta } = await contexto.params;
  const url = `${destino()}/api/${ruta.join("/")}${request.nextUrl.search}`;
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

  const controlador = new AbortController();
  const temporizador = setTimeout(() => controlador.abort(), ESPERA_MS);
  try {
    const respuesta = await fetch(url, { ...init, signal: controlador.signal });
    if (respuesta.status === 204) {
      return new Response(null, { status: 204 });
    }
    const tipo = respuesta.headers.get("content-type") ?? "";
    if (tipo.includes("text/html")) {
      return noDisponible();
    }
    const body = await respuesta.arrayBuffer();
    return new Response(body, {
      status: respuesta.status,
      headers: {
        "content-type": tipo || "application/json",
      },
    });
  } catch {
    return noDisponible();
  } finally {
    clearTimeout(temporizador);
  }
}

export const GET = proxy;
export const POST = proxy;
export const PUT = proxy;
export const PATCH = proxy;
export const DELETE = proxy;
