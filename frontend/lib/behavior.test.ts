import { describe, expect, it } from "vitest";
import { apiUrl } from "./apiUrl";
import { deskHref } from "./deskQuery";
import { countText, formatMoney } from "./format";
import { readJson, responseData } from "./httpBody";
import { EMPTY_SEARCH_MESSAGE } from "./messages";
import { parseMoney, priceError, quantityError, selectedCustomerId } from "./orderFields";
import { hasAnotherPage, ordersPath, PAGE_SIZE } from "./ordersPath";
import { safeReturn } from "./safeReturn";
import { fold, matchSpan } from "./text";

describe("orders on the desk", () => {
  it("asks for one page of fifty and keeps the filters", () => {
    expect(PAGE_SIZE).toBe(50);
    expect(ordersPath({ term: "josé", status: "SHIPPED", page: 2 }))
      .toBe("/api/v1/orders?page=2&size=50&term=jos%C3%A9&status=SHIPPED");
  });

  it("knows when another page is still hidden", () => {
    expect(hasAnotherPage(0, 2)).toBe(true);
    expect(hasAnotherPage(1, 2)).toBe(false);
  });

  it("counts what is on screen against the real total", () => {
    expect(countText(14, false)).toBe("14 pedidos");
    expect(countText(1, false)).toBe("1 pedido");
    expect(countText(1, true)).toBe("1 coincidencia");
    expect(countText(0, true)).toBe("0 coincidencias");
    expect(countText(80, false, 50)).toBe("50 de 80 pedidos");
  });

  it("keeps the query that is already applied", () => {
    expect(deskHref({ term: "mesa", status: "SHIPPED" }, {})).toBe("/?term=mesa&status=SHIPPED");
    expect(deskHref({ term: "mesa", status: "SHIPPED" }, { term: "" })).toBe("/?status=SHIPPED");
    expect(deskHref({ term: "", status: "" }, { notice: "Pedido LIV-000001 eliminado" }))
      .toBe("/?notice=Pedido+LIV-000001+eliminado");
  });
});

describe("order fields", () => {
  it("accepts a quantity from 1 to 99", () => {
    expect(quantityError("1")).toBeUndefined();
    expect(quantityError("99")).toBeUndefined();
    expect(quantityError("0")).toBe("La cantidad mínima es 1.");
    expect(quantityError("100")).toBe("La cantidad máxima es 99.");
    expect(quantityError("1.5")).toBe("La cantidad mínima es 1.");
  });

  it("reads pesos and centavos without binary rounding", () => {
    expect(parseMoney("1890")).toBe(1890);
    expect(parseMoney("10,50")).toBe(10.5);
    expect(parseMoney("0.01")).toBe(0.01);
    expect(parseMoney("1.005")).toBeUndefined();
    expect(parseMoney("0")).toBeUndefined();
    expect(priceError("0")).toBe("El precio debe ser mayor a cero.");
    expect(priceError("10.50")).toBeUndefined();
  });

  it("keeps the chosen customer and fills the first one when none is chosen", () => {
    const customers = [{ id: "a" }, { id: "b" }];
    expect(selectedCustomerId("", customers)).toBe("a");
    expect(selectedCustomerId("b", customers)).toBe("b");
    expect(selectedCustomerId("gone", customers)).toBe("a");
    expect(selectedCustomerId("", [])).toBe("");
  });
});

describe("search text", () => {
  it("folds accents before marking the match", () => {
    expect(fold("José")).toBe("jose");
    expect(matchSpan("José Luis", "jose")).toEqual({ start: 0, end: 4 });
    expect(matchSpan("Camila", "zz")).toBeNull();
    expect(matchSpan("Camila", "c")).toBeNull();
  });

  it("keeps the empty-search sentence exact", () => {
    expect(EMPTY_SEARCH_MESSAGE).toBe("No se tienen resultados que coincidan con los criterios de búsqueda.");
  });
});

describe("api bodies and links", () => {
  it("keeps a successful page and a list intact", () => {
    expect(responseData('{"content":[{"orderNumber":"LIV-000014"}],"totalElements":14}')).toEqual({
      content: [{ orderNumber: "LIV-000014" }],
      totalElements: 14,
    });
    expect(responseData('[{"id":"customer-1","fullName":"Ana López"}]')).toEqual([
      { id: "customer-1", fullName: "Ana López" },
    ]);
    expect(responseData("")).toBeNull();
    expect(responseData("<html>")).toBeUndefined();
  });

  it("reads an error body and refuses anything that is not an object", () => {
    expect(readJson("")).toBeNull();
    expect(readJson("<html>")).toBeNull();
    expect(readJson("[]")).toBeNull();
    expect(readJson("{\"message\":\"Pedido no encontrado.\",\"details\":{\"email\":\"El correo electrónico no es válido\"}}"))
      .toEqual({
        message: "Pedido no encontrado.",
        details: { email: "El correo electrónico no es válido" },
      });
  });

  it("builds a private api url and rejects a path that leaves /api", () => {
    expect(apiUrl("http://127.0.0.1:8080/", ["v1", "orders"], "?size=1"))
      .toBe("http://127.0.0.1:8080/api/v1/orders?size=1");
    expect(apiUrl("http://api:8080", ["..", "admin"], "")).toBeNull();
    expect(apiUrl("http://api:8080", [], "")).toBeNull();
  });

  it("only follows a local return path", () => {
    expect(safeReturn(null)).toBe("/");
    expect(safeReturn("/?term=jose")).toBe("/?term=jose");
    expect(safeReturn("/orders/abc")).toBe("/orders/abc");
    expect(safeReturn("//evil.example")).toBe("/");
    expect(safeReturn("/\\evil.example")).toBe("/");
    expect(safeReturn("https://evil.example")).toBe("/");
  });

  it("formats pesos for the desk", () => {
    expect(formatMoney(2890)).toMatch(/\$2,890\.00/);
  });
});
