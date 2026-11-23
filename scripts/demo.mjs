import { chromium } from "playwright";
import { mkdir, rename } from "node:fs/promises";
import path from "node:path";
import { fileURLToPath } from "node:url";

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..");
const evidenceDir = path.join(root, "docs", "evidencias");
const deskUrl = process.env.DESK_URL || process.env.MESA_URL || "http://127.0.0.1:3000";
const api = process.env.API_URL || "http://127.0.0.1:8080";
const emptyMessage = "No se tienen resultados que coincidan con los criterios de búsqueda.";

await mkdir(evidenceDir, { recursive: true });

const browser = await chromium.launch({ headless: true });
const context = await browser.newContext({
  viewport: { width: 1440, height: 900 },
  recordVideo: { dir: evidenceDir, size: { width: 1440, height: 900 } },
  locale: "es-MX",
});
const page = await context.newPage();
page.setDefaultTimeout(15000);

async function shot(name) {
  await page.screenshot({ path: path.join(evidenceDir, name), fullPage: false });
}

try {
  await page.goto(`file://${path.join(root, "docs", "recorrido.html")}`);
  for (const [id, file] of [
    ["#problema", "01-problema.png"],
    ["#arquitectura", "02-arquitectura.png"],
    ["#decisiones", "03-decisiones.png"],
    ["#pantallas", "04-pantallas.png"],
  ]) {
    await page.locator(id).scrollIntoViewIfNeeded();
    await page.waitForTimeout(1400);
    await shot(file);
  }

  await page.goto(`${api}/swagger-ui/index.html`);
  await page.getByText("Mesa de pedidos").first().waitFor();
  await page.waitForTimeout(1200);
  await shot("05-swagger.png");

  await page.goto(deskUrl);
  await page.getByText("14 pedidos").waitFor();
  await page.waitForTimeout(800);
  await shot("06-lista.png");

  const search = page.getByTestId("search");
  await search.click();
  await search.pressSequentially("jose", { delay: 140 });
  const option = page.getByRole("option", { name: /^Cliente José Luis Herrera Díaz/ });
  await option.waitFor();
  await page.waitForTimeout(700);
  await shot("07-typeahead.png");
  await option.click();
  await page.getByText("2 coincidencias").waitFor();
  await page.getByTestId("order-LIV-000002").waitFor();
  await page.getByTestId("order-LIV-000013").waitFor();
  await page.waitForTimeout(700);
  await shot("08-filtro.png");

  await page.getByRole("button", { name: "Limpiar" }).click();
  await page.getByText("14 pedidos").waitFor();
  await search.click();
  await search.pressSequentially("zzzz", { delay: 120 });
  await page.getByTestId("no-results").waitFor();
  const message = await page.getByTestId("no-results").innerText();
  if (message.trim() !== emptyMessage) throw new Error(`Unexpected message: ${message}`);
  await page.waitForTimeout(700);
  await shot("09-sin-resultados.png");
  await search.press("Enter");
  await page.getByTestId("empty-list").waitFor();
  const emptyList = await page.getByTestId("empty-list").innerText();
  if (!emptyList.includes(emptyMessage)) throw new Error("The list does not show the empty search sentence");
  await shot("10-lista-vacia.png");

  await page.getByRole("button", { name: "Limpiar" }).click();
  await page.getByText("14 pedidos").waitFor();
  await page.locator("a.stretched", { hasText: "LIV-000001" }).click();
  await page.getByTestId("detail-number").waitFor();
  await page.getByTestId("block-customer").waitFor();
  await page.getByTestId("block-delivery").waitFor();
  await page.getByTestId("block-order").waitFor();
  await page.getByText("mariana.castillo@correo.com").waitFor();
  await page.getByText("Av. Presidente Masaryk 111").waitFor();
  await page.getByText("LP-MOD-10442").waitFor();
  await page.waitForTimeout(800);
  await page.screenshot({ path: path.join(evidenceDir, "11-detalle.png"), fullPage: true });

  await page.getByRole("link", { name: "Volver a la mesa" }).click();
  await page.getByText("14 pedidos").waitFor();
  await page.getByTestId("open-order").click();
  await page.getByTestId("order-form").waitFor();
  await page.locator("#address").fill("Calle de la demo 10, Centro, CDMX, 06000");
  await page.locator("#code").fill("LP-MOD-99001");
  await page.locator("#description").fill("Pañuelo de seda");
  await page.locator("#unitPrice").fill("890");
  await page.getByRole("button", { name: "Crear pedido" }).click();
  await page.getByText(/Pedido LIV-\d+ registrado/).waitFor();
  const notice = await page.locator(".notice").innerText();
  const orderNumber = notice.match(/LIV-\d+/)?.[0];
  if (!orderNumber) throw new Error(`Created order number was not readable: ${notice}`);
  await page.waitForTimeout(600);
  await shot("12-creado.png");
  await page.locator("a.stretched", { hasText: orderNumber }).click();
  await page.getByTestId("detail-number").waitFor();
  await page.getByTestId("status").click();
  await page.getByRole("option", { name: "Confirmado" }).click();
  await page.getByTestId("save-status").click();
  await page.getByText("Estatus actualizado.").waitFor();
  await page.getByText("Confirmado").first().waitFor();
  await page.waitForTimeout(700);
  await shot("13-estatus.png");

  await page.getByTestId("delete").click();
  await page.getByTestId("confirm-delete").click();
  await page.getByText(`Pedido ${orderNumber} eliminado`).waitFor();
  await page.getByText("14 pedidos").waitFor();
  await shot("14-eliminado.png");

  await page.setViewportSize({ width: 390, height: 844 });
  await page.goto(deskUrl);
  await page.getByText("14 pedidos").waitFor();
  await page.locator("a.card", { hasText: "LIV-000014" }).waitFor();
  await page.waitForTimeout(600);
  await page.screenshot({ path: path.join(evidenceDir, "15-movil-lista.png") });
  await page.locator("a.card", { hasText: "LIV-000014" }).click();
  await page.getByTestId("block-customer").waitFor();
  await page.waitForTimeout(500);
  await page.screenshot({ path: path.join(evidenceDir, "16-movil-detalle.png"), fullPage: true });

  console.log(`Demo ok. Temporary order ${orderNumber} was created and deleted.`);
} finally {
  const video = page.video();
  await context.close();
  await browser.close();
  if (video) {
    const source = await video.path();
    const destination = path.join(evidenceDir, "demo.webm");
    await rename(source, destination);
    console.log(`Video: ${destination}`);
  }
}
