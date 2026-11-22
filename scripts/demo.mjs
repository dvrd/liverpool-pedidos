import { chromium } from "playwright";
import { mkdir, rename } from "node:fs/promises";
import path from "node:path";
import { fileURLToPath } from "node:url";

const raiz = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..");
const evidencias = path.join(raiz, "docs", "evidencias");
const mesa = process.env.MESA_URL || "http://127.0.0.1:3000";
const api = process.env.API_URL || "http://127.0.0.1:8080";
const vacio = "No se tienen resultados que coincidan con los criterios de búsqueda.";

await mkdir(evidencias, { recursive: true });

const browser = await chromium.launch({ headless: true });
const context = await browser.newContext({
  viewport: { width: 1440, height: 900 },
  recordVideo: { dir: evidencias, size: { width: 1440, height: 900 } },
  locale: "es-MX",
});
const page = await context.newPage();
page.setDefaultTimeout(15000);

async function tiro(nombre) {
  await page.screenshot({ path: path.join(evidencias, nombre), fullPage: false });
}

try {
  await page.goto(`file://${path.join(raiz, "docs", "recorrido.html")}`);
  for (const [id, archivo] of [
    ["#problema", "01-problema.png"],
    ["#arquitectura", "02-arquitectura.png"],
    ["#decisiones", "03-decisiones.png"],
    ["#pantallas", "04-pantallas.png"],
  ]) {
    await page.locator(id).scrollIntoViewIfNeeded();
    await page.waitForTimeout(1400);
    await tiro(archivo);
  }

  await page.goto(`${api}/swagger-ui/index.html`);
  await page.getByText("Mesa de pedidos").first().waitFor();
  await page.waitForTimeout(1200);
  await tiro("05-swagger.png");

  await page.goto(mesa);
  await page.getByText("14 pedidos").waitFor();
  await page.waitForTimeout(800);
  await tiro("06-lista.png");

  const buscador = page.getByTestId("buscador");
  await buscador.click();
  await buscador.pressSequentially("jose", { delay: 140 });
  const opcion = page.getByRole("option", { name: /^Cliente José Luis Herrera Díaz/ });
  await opcion.waitFor();
  await page.waitForTimeout(700);
  await tiro("07-typeahead.png");
  await opcion.click();
  await page.getByText("2 coincidencias").waitFor();
  await page.getByTestId("pedido-LIV-000002").waitFor();
  await page.getByTestId("pedido-LIV-000013").waitFor();
  await page.waitForTimeout(700);
  await tiro("08-filtro.png");

  await page.getByRole("button", { name: "Limpiar" }).click();
  await page.getByText("14 pedidos").waitFor();
  await buscador.click();
  await buscador.pressSequentially("zzzz", { delay: 120 });
  await page.getByTestId("sin-resultados").waitFor();
  const mensaje = await page.getByTestId("sin-resultados").innerText();
  if (mensaje.trim() !== vacio) throw new Error(`Mensaje inesperado: ${mensaje}`);
  await page.waitForTimeout(700);
  await tiro("09-sin-resultados.png");
  await buscador.press("Enter");
  await page.getByTestId("lista-vacia").waitFor();
  const listaVacia = await page.getByTestId("lista-vacia").innerText();
  if (!listaVacia.includes(vacio)) throw new Error("La lista no muestra el vacío de búsqueda");
  await tiro("10-lista-vacia.png");

  await page.getByRole("button", { name: "Limpiar" }).click();
  await page.getByText("14 pedidos").waitFor();
  await page.locator("a.estirado", { hasText: "LIV-000001" }).click();
  await page.getByTestId("detalle-folio").waitFor();
  await page.getByTestId("bloque-cliente").waitFor();
  await page.getByTestId("bloque-entrega").waitFor();
  await page.getByTestId("bloque-pedido").waitFor();
  await page.getByText("mariana.castillo@correo.com").waitFor();
  await page.getByText("Av. Presidente Masaryk 111").waitFor();
  await page.getByText("LP-MOD-10442").waitFor();
  await page.waitForTimeout(800);
  await page.screenshot({ path: path.join(evidencias, "11-detalle.png"), fullPage: true });

  await page.getByRole("link", { name: "Volver a la mesa" }).click();
  await page.getByText("14 pedidos").waitFor();
  await page.getByTestId("abrir-pedido").click();
  await page.getByTestId("form-pedido").waitFor();
  await page.locator("#direccion").fill("Calle de la demo 10, Centro, CDMX, 06000");
  await page.locator("#codigo").fill("LP-MOD-99001");
  await page.locator("#descripcion").fill("Pañuelo de seda");
  await page.locator("#precio").fill("890");
  await page.getByRole("button", { name: "Crear pedido" }).click();
  await page.getByText(/Pedido LIV-\d+ registrado/).waitFor();
  const nota = await page.locator(".aviso").innerText();
  const folio = nota.match(/LIV-\d+/)?.[0];
  if (!folio) throw new Error(`No se leyó el folio creado: ${nota}`);
  await page.waitForTimeout(600);
  await tiro("12-creado.png");
  await page.locator("a.estirado", { hasText: folio }).click();
  await page.getByTestId("detalle-folio").waitFor();
  await page.getByTestId("estatus").selectOption("CONFIRMADO");
  await page.getByTestId("guardar-estatus").click();
  await page.getByText("Estatus actualizado.").waitFor();
  await page.getByText("Confirmado").first().waitFor();
  await page.waitForTimeout(700);
  await tiro("13-estatus.png");

  await page.getByTestId("eliminar").click();
  await page.getByTestId("confirmar-eliminar").click();
  await page.getByText(`Pedido ${folio} eliminado`).waitFor();
  await page.getByText("14 pedidos").waitFor();
  await tiro("14-eliminado.png");

  await page.setViewportSize({ width: 390, height: 844 });
  await page.goto(mesa);
  await page.getByText("14 pedidos").waitFor();
  await page.locator("a.tarjeta", { hasText: "LIV-000014" }).waitFor();
  await page.waitForTimeout(600);
  await page.screenshot({ path: path.join(evidencias, "15-movil-lista.png") });
  await page.locator("a.tarjeta", { hasText: "LIV-000014" }).click();
  await page.getByTestId("bloque-cliente").waitFor();
  await page.waitForTimeout(500);
  await page.screenshot({ path: path.join(evidencias, "16-movil-detalle.png"), fullPage: true });

  console.log(`Demo correcta. Pedido temporal ${folio} creado y eliminado.`);
} finally {
  const video = page.video();
  await context.close();
  await browser.close();
  if (video) {
    const origen = await video.path();
    const destino = path.join(evidencias, "demo.webm");
    await rename(origen, destino);
    console.log(`Video: ${destino}`);
  }
}
