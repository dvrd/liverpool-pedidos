"use client";

import { sugerirPedidos } from "@/lib/api";
import { ETIQUETA_TIPO } from "@/lib/formato";
import { MENSAJE_SIN_RESULTADOS } from "@/lib/mensajes";
import type { Sugerencia } from "@/lib/tipos";
import { useEffect, useId, useRef, useState } from "react";

type Props = {
  terminoAplicado: string;
  onFiltrar: (termino: string) => void;
};

export function BuscadorPedidos({ terminoAplicado, onFiltrar }: Props) {
  const listaId = useId();
  const caja = useRef<HTMLDivElement>(null);
  const elegido = useRef<string | null>(null);
  const [draft, setDraft] = useState(terminoAplicado);
  const [sugerencias, setSugerencias] = useState<Sugerencia[]>([]);
  const [abierto, setAbierto] = useState(false);
  const [cargando, setCargando] = useState(false);
  const [activo, setActivo] = useState(-1);
  const [sinResultados, setSinResultados] = useState(false);

  useEffect(() => {
    setDraft(terminoAplicado);
  }, [terminoAplicado]);

  useEffect(() => {
    const termino = draft.trim();
    if (elegido.current === termino) {
      setAbierto(false);
      setCargando(false);
      return;
    }
    if (termino.length < 2) {
      setSugerencias([]);
      setSinResultados(false);
      setCargando(false);
      return;
    }

    const controlador = new AbortController();
    const espera = window.setTimeout(() => {
      setCargando(true);
      sugerirPedidos(termino, controlador.signal)
        .then((opciones) => {
          setSugerencias(opciones);
          setSinResultados(opciones.length === 0);
          setActivo(opciones.length > 0 ? 0 : -1);
          setAbierto(true);
        })
        .catch((error: unknown) => {
          if (error instanceof DOMException && error.name === "AbortError") return;
          setSugerencias([]);
          setSinResultados(false);
        })
        .finally(() => {
          if (!controlador.signal.aborted) setCargando(false);
        });
    }, 220);

    return () => {
      window.clearTimeout(espera);
      controlador.abort();
    };
  }, [draft]);

  useEffect(() => {
    function cerrar(event: MouseEvent) {
      if (!caja.current?.contains(event.target as Node)) setAbierto(false);
    }
    document.addEventListener("mousedown", cerrar);
    return () => document.removeEventListener("mousedown", cerrar);
  }, []);

  useEffect(() => {
    if (activo < 0) return;
    document.getElementById(`${listaId}-opcion-${activo}`)?.scrollIntoView({ block: "nearest" });
  }, [activo, listaId]);

  function elegir(opcion: Sugerencia) {
    elegido.current = opcion.valor;
    setSugerencias([]);
    setSinResultados(false);
    setDraft(opcion.valor);
    setAbierto(false);
    onFiltrar(opcion.valor);
  }

  function confirmar() {
    setAbierto(false);
    onFiltrar(draft.trim());
  }

  const terminoLargo = draft.trim().length >= 2;
  const muestraLista = abierto && terminoLargo && (cargando || sugerencias.length > 0 || sinResultados);

  return (
    <form
      className="buscador"
      role="search"
      onSubmit={(event) => {
        event.preventDefault();
        confirmar();
      }}
    >
      <label htmlFor="buscador">Buscar en la mesa</label>
      <div className="buscador-caja" ref={caja}>
        <input
          id="buscador"
          data-testid="buscador"
          role="combobox"
          aria-expanded={muestraLista}
          aria-controls={listaId}
          aria-autocomplete="list"
          aria-activedescendant={activo >= 0 ? `${listaId}-opcion-${activo}` : undefined}
          autoComplete="off"
          spellCheck={false}
          placeholder="Cliente, folio, correo o código"
          value={draft}
          onChange={(event) => {
            setDraft(event.target.value);
            setAbierto(true);
          }}
          onFocus={() => {
            if (terminoLargo) setAbierto(true);
          }}
          onKeyDown={(event) => {
            if (event.key === "ArrowDown") {
              event.preventDefault();
              setAbierto(true);
              setActivo((indice) => Math.min(indice + 1, sugerencias.length - 1));
            } else if (event.key === "ArrowUp") {
              event.preventDefault();
              setActivo((indice) => Math.max(indice - 1, 0));
            } else if (event.key === "Enter" && activo >= 0 && sugerencias[activo] && abierto) {
              event.preventDefault();
              elegir(sugerencias[activo]);
            } else if (event.key === "Escape") {
              setAbierto(false);
            }
          }}
        />
        {draft ? (
          <button
            type="button"
            className="limpiar"
            onClick={() => {
              elegido.current = null;
              setDraft("");
              setAbierto(false);
              onFiltrar("");
            }}
          >
            Limpiar
          </button>
        ) : null}
        {muestraLista ? (
          <div className="popover" id={listaId}>
            {cargando && sugerencias.length === 0 ? <p className="popover-nota">Buscando…</p> : null}
            {sugerencias.length > 0 ? (
              <ul role="listbox" data-testid="sugerencias">
                {sugerencias.map((opcion, indice) => (
                  <li key={`${opcion.tipo}-${opcion.valor}`} role="presentation">
                    <button
                      type="button"
                      role="option"
                      id={`${listaId}-opcion-${indice}`}
                      aria-selected={indice === activo}
                      className={indice === activo ? "opcion activa" : "opcion"}
                      onMouseEnter={() => setActivo(indice)}
                      onClick={() => elegir(opcion)}
                    >
                      <span className="opcion-tipo">{ETIQUETA_TIPO[opcion.tipo]}</span>
                      <span className="opcion-etiqueta">{resaltar(opcion.etiqueta, draft)}</span>
                      <span className="opcion-detalle">{opcion.detalle}</span>
                    </button>
                  </li>
                ))}
              </ul>
            ) : null}
            {!cargando && sinResultados ? (
              <p className="sin-resultados" role="status" data-testid="sin-resultados">
                {MENSAJE_SIN_RESULTADOS}
              </p>
            ) : null}
          </div>
        ) : null}
      </div>
    </form>
  );
}

function resaltar(texto: string, aguja: string) {
  const consulta = aguja.trim();
  if (consulta.length < 2) return texto;
  const indice = texto.toLocaleLowerCase("es-MX").indexOf(consulta.toLocaleLowerCase("es-MX"));
  if (indice < 0) return texto;
  return (
    <>
      {texto.slice(0, indice)}
      <mark>{texto.slice(indice, indice + consulta.length)}</mark>
      {texto.slice(indice + consulta.length)}
    </>
  );
}
