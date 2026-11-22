"use client";

import { AltaCliente, AltaPedido } from "@/components/pedidos/Altas";
import { BuscadorPedidos } from "@/components/pedidos/BuscadorPedidos";
import { Boton } from "@/components/ui/Boton";
import { MarcaEstatus } from "@/components/ui/MarcaEstatus";
import { listarPedidos, listarUsuarios } from "@/lib/api";
import { ETIQUETA_CANAL, ETIQUETA_ESTATUS, formatoMoneda, ORDEN_ESTATUS, textoConteo } from "@/lib/formato";
import { MENSAJE_SIN_RESULTADOS } from "@/lib/mensajes";
import type { EstatusPedido, Pedido, Usuario } from "@/lib/tipos";
import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { useCallback, useEffect, useState } from "react";

type Panel = "pedido" | "cliente" | null;

export function MesaPedidos() {
  const router = useRouter();
  const params = useSearchParams();
  const termino = params.get("termino") ?? "";
  const estatus = params.get("estatus") ?? "";
  const aviso = params.get("aviso") ?? "";

  const [pedidos, setPedidos] = useState<Pedido[]>([]);
  const [total, setTotal] = useState(0);
  const [carga, setCarga] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [nota, setNota] = useState<string | null>(null);
  const [panel, setPanel] = useState<Panel>(null);
  const [usuarios, setUsuarios] = useState<Usuario[]>([]);

  const cargar = useCallback(async (signal?: AbortSignal) => {
    setCarga(true);
    setError(null);
    try {
      const pagina = await listarPedidos({ termino, estatus }, signal);
      setPedidos(pagina.contenido);
      setTotal(pagina.totalElementos);
    } catch (causa) {
      if (causa instanceof DOMException && causa.name === "AbortError") return;
      setError(causa instanceof Error ? causa.message : "No se pudo cargar la mesa.");
    } finally {
      if (!signal?.aborted) setCarga(false);
    }
  }, [estatus, termino]);

  useEffect(() => {
    const controlador = new AbortController();
    void cargar(controlador.signal);
    return () => controlador.abort();
  }, [cargar]);

  useEffect(() => {
    let vigente = true;
    listarUsuarios()
      .then((lista) => {
        if (vigente) setUsuarios(lista);
      })
      .catch(() => {
        if (vigente) setUsuarios([]);
      });
    return () => {
      vigente = false;
    };
  }, [nota]);

  function aplicar(siguiente: { termino?: string; estatus?: string; aviso?: string }) {
    const consulta = new URLSearchParams();
    const nuevoTermino = siguiente.termino ?? termino;
    const nuevoEstatus = siguiente.estatus ?? estatus;
    if (nuevoTermino) consulta.set("termino", nuevoTermino);
    if (nuevoEstatus) consulta.set("estatus", nuevoEstatus);
    if (siguiente.aviso) consulta.set("aviso", siguiente.aviso);
    const texto = consulta.toString();
    router.replace(texto ? `/?${texto}` : "/");
  }

  const regreso = params.toString() ? `/?${params.toString()}` : "/";
  const mensaje = nota || aviso;
  const filtrado = Boolean(termino);

  return (
    <div className="panel">
      <div className="mesa-cabeza">
        <div>
          <p className="conteo" data-testid="conteo" aria-live="polite">
            {carga ? "Consultando pedidos…" : textoConteo(total, filtrado || Boolean(estatus))}
          </p>
          <BuscadorPedidos terminoAplicado={termino} onFiltrar={(valor) => aplicar({ termino: valor })} />
          {termino ? (
            <p className="filtro-activo">
              Filtro: <strong>{termino}</strong>
              <button type="button" onClick={() => aplicar({ termino: "" })}>Quitar</button>
            </p>
          ) : null}
        </div>
        <div className="atajos">
          <Boton type="button" variante={panel === "pedido" ? "primario" : "secundario"} data-testid="abrir-pedido" onClick={() => setPanel(panel === "pedido" ? null : "pedido")}>
            Registrar pedido
          </Boton>
          <Boton type="button" variante={panel === "cliente" ? "primario" : "secundario"} data-testid="abrir-cliente" onClick={() => setPanel(panel === "cliente" ? null : "cliente")}>
            Nuevo cliente
          </Boton>
        </div>
      </div>

      <div className="estatus-filtros" role="toolbar" aria-label="Filtrar por estatus">
        <button type="button" aria-pressed={estatus === ""} className={estatus === "" ? "chip activo" : "chip"} onClick={() => aplicar({ estatus: "" })}>
          Todos
        </button>
        {ORDEN_ESTATUS.map((valor) => (
          <button
            key={valor}
            type="button"
            aria-pressed={estatus === valor}
            className={estatus === valor ? "chip activo" : "chip"}
            onClick={() => aplicar({ estatus: valor })}
          >
            {ETIQUETA_ESTATUS[valor as EstatusPedido]}
          </button>
        ))}
      </div>

      {mensaje ? <p className="aviso" role="status">{mensaje}</p> : null}
      {error ? (
        <p className="falla" role="alert">
          {error} <button type="button" onClick={() => void cargar()}>Reintentar</button>
        </p>
      ) : null}

      {panel === "cliente" ? (
        <AltaCliente
          onCancelar={() => setPanel(null)}
          onCreado={(nombre) => {
            setPanel(null);
            setNota(`Cliente ${nombre} registrado.`);
          }}
        />
      ) : null}
      {panel === "pedido" ? (
        <AltaPedido
          usuarios={usuarios}
          onCancelar={() => setPanel(null)}
          onCreado={(folio) => {
            setPanel(null);
            setNota(`Pedido ${folio} registrado.`);
            void cargar();
          }}
        />
      ) : null}

      <section className="lista" aria-busy={carga} data-testid="lista">
        {carga ? <ListaEsqueleto /> : null}
        {!carga && pedidos.length === 0 && termino ? (
          <p className="sin-resultados lista-vacia" data-testid="lista-vacia">{MENSAJE_SIN_RESULTADOS}</p>
        ) : null}
        {!carga && pedidos.length === 0 && !termino && estatus ? (
          <p className="lista-vacia">No hay pedidos con estatus {ETIQUETA_ESTATUS[estatus as EstatusPedido] ?? estatus}.</p>
        ) : null}
        {!carga && pedidos.length === 0 && !termino && !estatus ? (
          <p className="lista-vacia">Aún no hay pedidos registrados.</p>
        ) : null}

        {!carga && pedidos.length > 0 ? (
          <>
            <div className="tabla-envoltorio">
              <table className="tabla">
                <thead>
                  <tr>
                    <th scope="col">Folio</th>
                    <th scope="col">Cliente</th>
                    <th scope="col">Pedido</th>
                    <th scope="col">Canal</th>
                    <th scope="col">Estatus</th>
                    <th scope="col" className="numero">Importe</th>
                  </tr>
                </thead>
                <tbody>
                  {pedidos.map((pedido, indice) => (
                    <tr key={pedido.id} className="fila" style={{ animationDelay: `${indice * 35}ms` }}>
                      <td>
                        <Link className="estirado" href={hrefPedido(pedido.id, regreso)} data-testid={`pedido-${pedido.folio}`}>
                          {pedido.folio}
                        </Link>
                      </td>
                      <td>
                        <span className="nombre">{pedido.cliente.nombreCompleto}</span>
                        <span className="secundario">{pedido.cliente.correoElectronico}</span>
                      </td>
                      <td>
                        <span className="nombre">{pedido.descripcionProducto}</span>
                        <span className="secundario">{pedido.codigoProducto}</span>
                      </td>
                      <td>{ETIQUETA_CANAL[pedido.canal]}</td>
                      <td><MarcaEstatus estatus={pedido.estatus} /></td>
                      <td className="numero">{formatoMoneda(pedido.importeTotal)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            <div className="tarjetas">
              {pedidos.map((pedido) => (
                <Link key={pedido.id} className="tarjeta" href={hrefPedido(pedido.id, regreso)}>
                  <div className="tarjeta-cabeza">
                    <span className="folio">{pedido.folio}</span>
                    <MarcaEstatus estatus={pedido.estatus} />
                  </div>
                  <p className="tarjeta-nombre">{pedido.cliente.nombreCompleto}</p>
                  <p className="tarjeta-producto">{pedido.descripcionProducto}</p>
                  <p className="secundario">{pedido.codigoProducto} · {ETIQUETA_CANAL[pedido.canal]}</p>
                  <p className="tarjeta-importe">{formatoMoneda(pedido.importeTotal)}</p>
                </Link>
              ))}
            </div>
          </>
        ) : null}
      </section>
    </div>
  );
}

function hrefPedido(id: string, regreso: string) {
  return `/pedidos/${id}?regreso=${encodeURIComponent(regreso)}`;
}

function ListaEsqueleto() {
  return (
    <div className="esqueleto" aria-hidden="true">
      <span />
      <span />
      <span />
      <span />
    </div>
  );
}
