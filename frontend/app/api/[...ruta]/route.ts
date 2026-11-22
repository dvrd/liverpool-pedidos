import { NextRequest } from "next/server";

export const dynamic = "force-dynamic";

const DESTINO = (process.env.API_INTERNA_URL || "http://127.0.0.1:8080").replace(/\/$/, "");

type Contexto = { params: Promise<{ ruta: string[] }> };

async function proxy(request: NextRequest, contexto: Contexto) {
  const { ruta } = await contexto.params;
  const url = `${DESTINO}/api/${ruta.join("/")}${request.nextUrl.search}`;
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

  try {
    const respuesta = await fetch(url, init);
    if (respuesta.status === 204) {
      return new Response(null, { status: 204 });
    }
    const body = await respuesta.arrayBuffer();
    return new Response(body, {
      status: respuesta.status,
      headers: {
        "content-type": respuesta.headers.get("content-type") ?? "application/json",
      },
    });
  } catch {
    return Response.json(
      { mensaje: "El servicio de pedidos no está disponible." },
      { status: 502 },
    );
  }
}

export const GET = proxy;
export const POST = proxy;
export const PUT = proxy;
export const PATCH = proxy;
export const DELETE = proxy;
