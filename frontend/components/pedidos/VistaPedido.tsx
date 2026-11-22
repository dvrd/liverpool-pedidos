"use client";

import { Boton } from "@/components/ui/Boton";
import { Bloque, Dato } from "@/components/ui/Bloque";
import { Campo } from "@/components/ui/Campo";
import { MarcaEstatus } from "@/components/ui/MarcaEstatus";
import { actualizarPedido, cambiarEstatus, eliminarPedido, ErrorApi, obtenerPedido } from "@/lib/api";
import { aNumero, ETIQUETA_CANAL, ETIQUETA_ESTATUS, formatoFecha, formatoMoneda, ORDEN_ESTATUS } from "@/lib/formato";
import type { CanalPedido, EstatusPedido, Pedido } from "@/lib/tipos";
import Link from "next/link";
import { useParams, useRouter, useSearchParams } from "next/navigation";
import { FormEvent, useEffect, useState } from "react";

export function VistaPedido() {
  const params = useParams<{ id: string }>();
  const busqueda = useSearchParams();
  const router = useRouter();
  const id = params.id;
  const regreso = destinoRegreso(busqueda.get("regreso"));

  const [pedido, setPedido] = useState<Pedido | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [carga, setCarga] = useState(true);
  const [estatus, setEstatus] = useState<EstatusPedido>("RECIBIDO");
  const [guardandoEstatus, setGuardandoEstatus] = useState(false);
  const [nota, setNota] = useState<string | null>(null);
  const [corrigiendo, setCorrigiendo] = useState(false);
  const [confirmarBorrado, setConfirmarBorrado] = useState(false);
  const [borrando, setBorrando] = useState(false);

  useEffect(() => {
    const controlador = new AbortController();
    setCarga(true);
    setError(null);
    obtenerPedido(id, controlador.signal)
      .then((dato) => {
        setPedido(dato);
        setEstatus(dato.estatus);
      })
      .catch((causa: unknown) => {
        if (causa instanceof DOMException && causa.name === "AbortError") return;
        setError(causa instanceof Error ? causa.message : "No se pudo abrir el pedido.");
      })
      .finally(() => {
        if (!controlador.signal.aborted) setCarga(false);
      });
    return () => controlador.abort();
  }, [id]);

  useEffect(() => {
    if (!pedido) return;
    const titulo = document.title;
    document.title = `${pedido.folio} · Mesa de pedidos`;
    return () => {
      document.title = titulo;
    };
  }, [pedido]);

  async function guardarEstatus() {
    if (!pedido) return;
    setGuardandoEstatus(true);
    setNota(null);
    setError(null);
    try {
      const actualizado = await cambiarEstatus(pedido.id, estatus);
      setPedido(actualizado);
      setEstatus(actualizado.estatus);
      setNota("Estatus actualizado.");
    } catch (causa) {
      setError(causa instanceof Error ? causa.message : "No se pudo cambiar el estatus.");
    } finally {
      setGuardandoEstatus(false);
    }
  }

  async function borrar() {
    if (!pedido) return;
    setBorrando(true);
    try {
      await eliminarPedido(pedido.id);
      router.push(`/?aviso=${encodeURIComponent(`Pedido ${pedido.folio} eliminado`)}`);
    } catch (causa) {
      setError(causa instanceof Error ? causa.message : "No se pudo eliminar el pedido.");
      setBorrando(false);
    }
  }

  if (carga) return <p className="estado">Abriendo el pedido…</p>;
  if (!pedido) {
    return (
      <div className="estado">
        <p role="alert">{error ?? "Este pedido no existe o fue eliminado."}</p>
        <Link href={regreso}>Volver a la mesa</Link>
      </div>
    );
  }

  return (
    <article className="vista">
      <Link className="volver" href={regreso}>Volver a la mesa</Link>
      <header className="vista-cabeza">
        <div>
          <p className="vista-canal">{ETIQUETA_CANAL[pedido.canal]}</p>
          <h1 data-testid="detalle-folio">{pedido.folio}</h1>
          <p className="vista-meta">
            Registrado {formatoFecha(pedido.creadoEn)}
            {pedido.actualizadoEn !== pedido.creadoEn ? ` · Actualizado ${formatoFecha(pedido.actualizadoEn)}` : ""}
          </p>
        </div>
        <div className="vista-estatus">
          <MarcaEstatus estatus={pedido.estatus} />
          <label className="campo" htmlFor="estatus">
            <span>Cambiar estatus</span>
            <select
              id="estatus"
              data-testid="estatus"
              value={estatus}
              onChange={(event) => setEstatus(event.target.value as EstatusPedido)}
            >
              {ORDEN_ESTATUS.map((valor) => (
                <option key={valor} value={valor}>{ETIQUETA_ESTATUS[valor]}</option>
              ))}
            </select>
          </label>
          <Boton type="button" data-testid="guardar-estatus" disabled={estatus === pedido.estatus || guardandoEstatus} onClick={() => void guardarEstatus()}>
            {guardandoEstatus ? "Guardando…" : "Guardar estatus"}
          </Boton>
        </div>
      </header>

      {nota ? <p className="aviso" role="status">{nota}</p> : null}
      {error ? <p className="falla" role="alert">{error}</p> : null}

      <div className="ficha">
        <div className="ficha-cuerpo">
          <Bloque indice="01" titulo="Datos del cliente" id="bloque-cliente">
            <Dato etiqueta="Nombre" valor={pedido.cliente.nombre} />
            <Dato etiqueta="Apellido paterno" valor={pedido.cliente.apellidoPaterno} />
            <Dato etiqueta="Apellido materno" valor={pedido.cliente.apellidoMaterno} />
            <Dato etiqueta="Correo electrónico" valor={pedido.cliente.correoElectronico} />
          </Bloque>
          <Bloque indice="02" titulo="Datos de entrega" id="bloque-entrega">
            <Dato etiqueta="Dirección de envío" valor={pedido.direccionEnvio} />
          </Bloque>
          <Bloque indice="03" titulo="Datos del pedido" id="bloque-pedido">
            <Dato etiqueta="Código del producto" valor={pedido.codigoProducto} />
            <Dato etiqueta="Descripción" valor={pedido.descripcionProducto} />
            <Dato etiqueta="Cantidad" valor={pedido.cantidad} />
            <Dato etiqueta="Precio" valor={formatoMoneda(pedido.precio)} />
            <Dato etiqueta="Importe" valor={formatoMoneda(pedido.importeTotal)} />
            <Dato etiqueta="Estatus de pedido" valor={ETIQUETA_ESTATUS[pedido.estatus]} />
          </Bloque>

          <div className="correccion">
            <Boton type="button" variante="secundario" onClick={() => setCorrigiendo((valor) => !valor)}>
              {corrigiendo ? "Cerrar corrección" : "Corregir datos del pedido"}
            </Boton>
            {corrigiendo ? (
              <Correccion
                pedido={pedido}
                onGuardado={(actualizado) => {
                  setPedido(actualizado);
                  setEstatus(actualizado.estatus);
                  setCorrigiendo(false);
                  setNota("Pedido actualizado.");
                }}
                onError={setError}
              />
            ) : null}
          </div>

          <div className="peligro-zona">
            {confirmarBorrado ? (
              <>
                <p>Se eliminará {pedido.folio}. Esta acción no se deshace.</p>
                <div className="acciones">
                  <Boton type="button" variante="peligro" data-testid="confirmar-eliminar" disabled={borrando} onClick={() => void borrar()}>
                    {borrando ? "Eliminando…" : "Eliminar pedido"}
                  </Boton>
                  <Boton type="button" variante="texto" onClick={() => setConfirmarBorrado(false)}>Conservar</Boton>
                </div>
              </>
            ) : (
              <Boton type="button" variante="texto" data-testid="eliminar" onClick={() => setConfirmarBorrado(true)}>
                Eliminar pedido
              </Boton>
            )}
          </div>
        </div>

        <aside className="historial" aria-label="Historial de estatus">
          <h2>Historial</h2>
          <ol>
            {pedido.historial.map((evento, indice) => (
              <li key={`${evento.estatus}-${evento.ocurridoEn}-${indice}`} data-actual={indice === pedido.historial.length - 1}>
                <span>{ETIQUETA_ESTATUS[evento.estatus]}</span>
                <time dateTime={evento.ocurridoEn}>{formatoFecha(evento.ocurridoEn)}</time>
              </li>
            ))}
          </ol>
        </aside>
      </div>
    </article>
  );
}

function Correccion({
  pedido,
  onGuardado,
  onError,
}: {
  pedido: Pedido;
  onGuardado: (pedido: Pedido) => void;
  onError: (mensaje: string) => void;
}) {
  const [direccion, setDireccion] = useState(pedido.direccionEnvio);
  const [codigo, setCodigo] = useState(pedido.codigoProducto);
  const [descripcion, setDescripcion] = useState(pedido.descripcionProducto);
  const [cantidad, setCantidad] = useState(String(pedido.cantidad));
  const [precio, setPrecio] = useState(String(pedido.precio));
  const [canal, setCanal] = useState<CanalPedido>(pedido.canal);
  const [errores, setErrores] = useState<Record<string, string | undefined>>({});
  const [enviando, setEnviando] = useState(false);

  async function enviar(event: FormEvent) {
    event.preventDefault();
    const cantidadNumero = aNumero(cantidad);
    const precioNumero = aNumero(precio);
    if (!Number.isInteger(cantidadNumero) || cantidadNumero < 1 || !Number.isFinite(precioNumero) || precioNumero <= 0) {
      onError("Revisa cantidad y precio.");
      return;
    }
    setEnviando(true);
    setErrores({});
    try {
      const actualizado = await actualizarPedido(pedido.id, {
        direccionEnvio: direccion,
        codigoProducto: codigo,
        descripcionProducto: descripcion,
        cantidad: cantidadNumero,
        precio: Math.round(precioNumero * 100) / 100,
        canal,
        estatus: pedido.estatus,
      });
      onGuardado(actualizado);
    } catch (causa) {
      if (causa instanceof ErrorApi) {
        setErrores(causa.detalles ?? {});
        onError(causa.message);
      } else {
        onError("No se pudo actualizar el pedido.");
      }
    } finally {
      setEnviando(false);
    }
  }

  return (
    <form className="alta" onSubmit={enviar}>
      <div className="alta-grid">
        <Campo id="dir-edicion" etiqueta="Dirección de envío" error={errores.direccionEnvio} className="campo-ancho">
          <textarea id="dir-edicion" rows={2} value={direccion} onChange={(event) => setDireccion(event.target.value)} />
        </Campo>
        <Campo id="cod-edicion" etiqueta="Código del producto" error={errores.codigoProducto}>
          <input id="cod-edicion" value={codigo} onChange={(event) => setCodigo(event.target.value)} />
        </Campo>
        <Campo id="desc-edicion" etiqueta="Descripción" error={errores.descripcionProducto}>
          <input id="desc-edicion" value={descripcion} onChange={(event) => setDescripcion(event.target.value)} />
        </Campo>
        <Campo id="cant-edicion" etiqueta="Cantidad" error={errores.cantidad}>
          <input id="cant-edicion" inputMode="numeric" value={cantidad} onChange={(event) => setCantidad(event.target.value)} />
        </Campo>
        <Campo id="precio-edicion" etiqueta="Precio" error={errores.precio}>
          <input id="precio-edicion" inputMode="decimal" value={precio} onChange={(event) => setPrecio(event.target.value)} />
        </Campo>
        <Campo id="canal-edicion" etiqueta="Canal">
          <select id="canal-edicion" value={canal} onChange={(event) => setCanal(event.target.value as CanalPedido)}>
            {(Object.keys(ETIQUETA_CANAL) as CanalPedido[]).map((valor) => (
              <option key={valor} value={valor}>{ETIQUETA_CANAL[valor]}</option>
            ))}
          </select>
        </Campo>
      </div>
      <Boton type="submit" disabled={enviando}>{enviando ? "Guardando…" : "Guardar corrección"}</Boton>
    </form>
  );
}

function destinoRegreso(valor: string | null): string {
  if (!valor || !valor.startsWith("/") || valor.startsWith("//")) return "/";
  return valor;
}
