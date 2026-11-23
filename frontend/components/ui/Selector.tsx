"use client";

import type { EstatusPedido } from "@/lib/tipos";
import { useEffect, useId, useLayoutEffect, useRef, useState } from "react";

export type OpcionSelector = {
  valor: string;
  etiqueta: string;
  detalle?: string;
  estatus?: EstatusPedido;
};

type Props = {
  id: string;
  valor: string;
  opciones: OpcionSelector[];
  onCambio: (valor: string) => void;
  deshabilitado?: boolean;
  testId?: string;
};

function plano(valor: string): string {
  return valor.normalize("NFD").replace(/[\u0300-\u036f]/g, "").toLowerCase();
}

export function Selector({ id, valor, opciones, onCambio, deshabilitado = false, testId }: Props) {
  const listaId = useId();
  const caja = useRef<HTMLDivElement>(null);
  const menuRef = useRef<HTMLDivElement>(null);
  const buffer = useRef("");
  const reloj = useRef<number | null>(null);
  const [abierto, setAbierto] = useState(false);
  const [activo, setActivo] = useState(-1);
  const actual = opciones.find((opcion) => opcion.valor === valor);
  const vacio = opciones.length === 0;
  const texto = actual
    ? actual.detalle
      ? `${actual.etiqueta} · ${actual.detalle}`
      : actual.etiqueta
    : vacio
      ? "Sin opciones"
      : "Elige una opción";

  function medir() {
    const raiz = caja.current;
    const menu = menuRef.current;
    if (!raiz || !menu) return;
    const rect = raiz.getBoundingClientRect();
    const abajo = window.innerHeight - rect.bottom - 12;
    const arriba = rect.top - 12;
    const abreArriba = abajo < 200 && arriba > abajo;
    menu.dataset.lado = abreArriba ? "arriba" : "abajo";
    menu.style.maxHeight = `${Math.max(120, Math.min(320, abreArriba ? arriba : abajo))}px`;
  }

  function abrir() {
    if (deshabilitado || vacio) return;
    setActivo(Math.max(0, opciones.findIndex((opcion) => opcion.valor === valor)));
    setAbierto(true);
  }

  function elegir(opcion: OpcionSelector) {
    onCambio(opcion.valor);
    setAbierto(false);
  }

  useLayoutEffect(() => {
    if (abierto) medir();
  }, [abierto, opciones.length]);

  useEffect(() => {
    if (!abierto) return;
    function cerrar(event: MouseEvent) {
      if (!caja.current?.contains(event.target as Node)) setAbierto(false);
    }
    function seguir() {
      medir();
    }
    document.addEventListener("mousedown", cerrar);
    window.addEventListener("resize", seguir);
    window.addEventListener("scroll", seguir, true);
    return () => {
      document.removeEventListener("mousedown", cerrar);
      window.removeEventListener("resize", seguir);
      window.removeEventListener("scroll", seguir, true);
    };
  }, [abierto]);

  useEffect(() => {
    if (!abierto || activo < 0) return;
    document.getElementById(`${listaId}-opcion-${activo}`)?.scrollIntoView({ block: "nearest" });
  }, [abierto, activo, listaId]);

  useEffect(() => {
    return () => {
      if (reloj.current) window.clearTimeout(reloj.current);
    };
  }, []);

  function buscarPorTecla(tecla: string) {
    buffer.current = (buffer.current + tecla).slice(-32);
    if (reloj.current) window.clearTimeout(reloj.current);
    reloj.current = window.setTimeout(() => {
      buffer.current = "";
    }, 700);
    const consulta = plano(buffer.current);
    const indice = opciones.findIndex((opcion) =>
      plano(`${opcion.etiqueta} ${opcion.detalle ?? ""}`).startsWith(consulta),
    );
    if (indice < 0) return;
    setActivo(indice);
    setAbierto(true);
  }

  return (
    <div className="selector" ref={caja}>
      <button
        id={id}
        type="button"
        className="selector-boton"
        role="combobox"
        aria-haspopup="listbox"
        aria-expanded={abierto}
        aria-controls={listaId}
        aria-autocomplete="list"
        aria-activedescendant={abierto && activo >= 0 ? `${listaId}-opcion-${activo}` : undefined}
        data-estatus={actual?.estatus}
        data-testid={testId}
        disabled={deshabilitado || vacio}
        onClick={() => (abierto ? setAbierto(false) : abrir())}
        onKeyDown={(event) => {
          if (deshabilitado || vacio) return;
          if (event.key === "ArrowDown") {
            event.preventDefault();
            if (!abierto) abrir();
            else setActivo((indice) => Math.min(indice + 1, opciones.length - 1));
          } else if (event.key === "ArrowUp") {
            event.preventDefault();
            if (!abierto) abrir();
            else setActivo((indice) => Math.max(indice - 1, 0));
          } else if (event.key === "Home") {
            event.preventDefault();
            setAbierto(true);
            setActivo(0);
          } else if (event.key === "End") {
            event.preventDefault();
            setAbierto(true);
            setActivo(opciones.length - 1);
          } else if (event.key === "Enter" || event.key === " ") {
            event.preventDefault();
            if (!abierto) abrir();
            else if (opciones[activo]) elegir(opciones[activo]);
          } else if (event.key === "Escape") {
            setAbierto(false);
          } else if (event.key === "Tab") {
            setAbierto(false);
          } else if (event.key.length === 1 && !event.metaKey && !event.ctrlKey && !event.altKey) {
            buscarPorTecla(event.key);
          }
        }}
      >
        <span className="selector-valor">
          {actual?.estatus ? <span className="cuadro" aria-hidden="true" /> : null}
          <span className="selector-texto">{texto}</span>
        </span>
        <span className="selector-flecha" aria-hidden="true" />
      </button>
      {abierto ? (
        <div ref={menuRef} id={listaId} className="selector-menu" role="listbox">
          {opciones.map((opcion, indice) => (
            <button
              key={opcion.valor}
              type="button"
              role="option"
              id={`${listaId}-opcion-${indice}`}
              aria-selected={opcion.valor === valor}
              data-estatus={opcion.estatus}
              className={indice === activo ? "menu-opcion activa" : "menu-opcion"}
              onMouseEnter={() => setActivo(indice)}
              onMouseDown={(event) => event.preventDefault()}
              onClick={() => elegir(opcion)}
            >
              {opcion.estatus ? <span className="cuadro" aria-hidden="true" /> : null}
              <span className="menu-texto">
                <span className="menu-etiqueta">{opcion.etiqueta}</span>
                {opcion.detalle ? <span className="menu-detalle">{opcion.detalle}</span> : null}
              </span>
            </button>
          ))}
        </div>
      ) : null}
    </div>
  );
}
