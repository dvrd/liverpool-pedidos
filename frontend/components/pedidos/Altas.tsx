"use client";

import { Boton } from "@/components/ui/Boton";
import { Campo } from "@/components/ui/Campo";
import { Selector } from "@/components/ui/Selector";
import { crearPedido, crearUsuario, ErrorApi } from "@/lib/api";
import { aNumero, ETIQUETA_CANAL, ETIQUETA_ESTATUS, ORDEN_ESTATUS } from "@/lib/formato";
import type { CanalPedido, EstatusPedido, Usuario } from "@/lib/tipos";
import { FormEvent, useState } from "react";

type Errores = Record<string, string | undefined>;

export function AltaCliente({
  onCreado,
  onCancelar,
}: {
  onCreado: (nombre: string) => void;
  onCancelar: () => void;
}) {
  const [nombre, setNombre] = useState("");
  const [apellidoPaterno, setApellidoPaterno] = useState("");
  const [apellidoMaterno, setApellidoMaterno] = useState("");
  const [correo, setCorreo] = useState("");
  const [errores, setErrores] = useState<Errores>({});
  const [falla, setFalla] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);

  async function enviar(event: FormEvent) {
    event.preventDefault();
    setEnviando(true);
    setFalla(null);
    setErrores({});
    try {
      const creado = await crearUsuario({
        nombre,
        apellidoPaterno,
        apellidoMaterno,
        correoElectronico: correo,
      });
      onCreado(creado.nombreCompleto);
    } catch (error) {
      if (error instanceof ErrorApi) {
        setErrores(error.detalles ?? {});
        setFalla(error.message);
      } else {
        setFalla("No se pudo registrar el cliente.");
      }
    } finally {
      setEnviando(false);
    }
  }

  return (
    <form className="alta" data-testid="form-cliente" onSubmit={enviar}>
      <h2>Nuevo cliente</h2>
      <div className="alta-grid">
        <Campo id="nombre" etiqueta="Nombre" error={errores.nombre}>
          <input id="nombre" value={nombre} onChange={(event) => setNombre(event.target.value)} aria-invalid={Boolean(errores.nombre)} aria-describedby={errores.nombre ? "nombre-error" : undefined} />
        </Campo>
        <Campo id="apellidoPaterno" etiqueta="Apellido paterno" error={errores.apellidoPaterno}>
          <input id="apellidoPaterno" value={apellidoPaterno} onChange={(event) => setApellidoPaterno(event.target.value)} aria-invalid={Boolean(errores.apellidoPaterno)} aria-describedby={errores.apellidoPaterno ? "apellidoPaterno-error" : undefined} />
        </Campo>
        <Campo id="apellidoMaterno" etiqueta="Apellido materno" error={errores.apellidoMaterno}>
          <input id="apellidoMaterno" value={apellidoMaterno} onChange={(event) => setApellidoMaterno(event.target.value)} aria-invalid={Boolean(errores.apellidoMaterno)} aria-describedby={errores.apellidoMaterno ? "apellidoMaterno-error" : undefined} />
        </Campo>
        <Campo id="correo" etiqueta="Correo electrónico" error={errores.correoElectronico}>
          <input id="correo" type="email" autoComplete="off" value={correo} onChange={(event) => setCorreo(event.target.value)} aria-invalid={Boolean(errores.correoElectronico)} aria-describedby={errores.correoElectronico ? "correo-error" : undefined} />
        </Campo>
      </div>
      {falla ? <p className="falla" role="alert">{falla}</p> : null}
      <div className="acciones">
        <Boton type="submit" disabled={enviando}>{enviando ? "Guardando…" : "Registrar cliente"}</Boton>
        <Boton type="button" variante="texto" onClick={onCancelar}>Cancelar</Boton>
      </div>
    </form>
  );
}

export function AltaPedido({
  usuarios,
  onCreado,
  onCancelar,
}: {
  usuarios: Usuario[];
  onCreado: (folio: string) => void;
  onCancelar: () => void;
}) {
  const [usuarioId, setUsuarioId] = useState(usuarios[0]?.id ?? "");
  const [direccion, setDireccion] = useState("");
  const [codigo, setCodigo] = useState("");
  const [descripcion, setDescripcion] = useState("");
  const [cantidad, setCantidad] = useState("1");
  const [precio, setPrecio] = useState("");
  const [canal, setCanal] = useState<CanalPedido>("SITIO_WEB");
  const [estatus, setEstatus] = useState<EstatusPedido>("RECIBIDO");
  const [errores, setErrores] = useState<Errores>({});
  const [falla, setFalla] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);

  async function enviar(event: FormEvent) {
    event.preventDefault();
    const cantidadNumero = aNumero(cantidad);
    const precioNumero = aNumero(precio);
    const locales: Errores = {};
    if (!usuarioId) locales.usuarioId = "Elige un cliente.";
    if (!Number.isInteger(cantidadNumero) || cantidadNumero < 1) locales.cantidad = "La cantidad mínima es 1.";
    if (!Number.isFinite(precioNumero) || precioNumero <= 0) locales.precio = "El precio debe ser mayor a cero.";
    if (Object.keys(locales).length > 0) {
      setErrores(locales);
      setFalla("Revisa los campos marcados.");
      return;
    }

    setEnviando(true);
    setFalla(null);
    setErrores({});
    try {
      const creado = await crearPedido({
        usuarioId,
        direccionEnvio: direccion,
        codigoProducto: codigo,
        descripcionProducto: descripcion,
        cantidad: cantidadNumero,
        precio: Math.round(precioNumero * 100) / 100,
        canal,
        estatus,
      });
      onCreado(creado.folio);
    } catch (error) {
      if (error instanceof ErrorApi) {
        setErrores(error.detalles ?? {});
        setFalla(error.message);
      } else {
        setFalla("No se pudo registrar el pedido.");
      }
    } finally {
      setEnviando(false);
    }
  }

  return (
    <form className="alta" data-testid="form-pedido" onSubmit={enviar}>
      <h2>Registrar pedido</h2>
      {usuarios.length === 0 ? <p className="popover-nota">Primero registra un cliente.</p> : null}
      <div className="alta-grid">
        <Campo id="usuarioId" etiqueta="Cliente" error={errores.usuarioId} className="campo-ancho">
          <Selector
            id="usuarioId"
            valor={usuarioId}
            deshabilitado={usuarios.length === 0}
            onCambio={setUsuarioId}
            opciones={usuarios.map((usuario) => ({
              valor: usuario.id,
              etiqueta: usuario.nombreCompleto,
              detalle: usuario.correoElectronico,
            }))}
          />
        </Campo>
        <Campo id="direccion" etiqueta="Dirección de envío" error={errores.direccionEnvio} className="campo-ancho">
          <textarea id="direccion" rows={2} value={direccion} onChange={(event) => setDireccion(event.target.value)} aria-invalid={Boolean(errores.direccionEnvio)} aria-describedby={errores.direccionEnvio ? "direccion-error" : undefined} />
        </Campo>
        <Campo id="codigo" etiqueta="Código del producto" error={errores.codigoProducto}>
          <input id="codigo" value={codigo} onChange={(event) => setCodigo(event.target.value)} aria-invalid={Boolean(errores.codigoProducto)} aria-describedby={errores.codigoProducto ? "codigo-error" : undefined} />
        </Campo>
        <Campo id="descripcion" etiqueta="Descripción" error={errores.descripcionProducto}>
          <input id="descripcion" value={descripcion} onChange={(event) => setDescripcion(event.target.value)} aria-invalid={Boolean(errores.descripcionProducto)} aria-describedby={errores.descripcionProducto ? "descripcion-error" : undefined} />
        </Campo>
        <Campo id="cantidad" etiqueta="Cantidad" error={errores.cantidad}>
          <input id="cantidad" inputMode="numeric" value={cantidad} onChange={(event) => setCantidad(event.target.value)} aria-invalid={Boolean(errores.cantidad)} aria-describedby={errores.cantidad ? "cantidad-error" : undefined} />
        </Campo>
        <Campo id="precio" etiqueta="Precio unitario" error={errores.precio}>
          <input id="precio" inputMode="decimal" value={precio} onChange={(event) => setPrecio(event.target.value)} aria-invalid={Boolean(errores.precio)} aria-describedby={errores.precio ? "precio-error" : undefined} />
        </Campo>
        <Campo id="canal" etiqueta="Canal">
          <Selector
            id="canal"
            valor={canal}
            onCambio={(siguiente) => setCanal(siguiente as CanalPedido)}
            opciones={(Object.keys(ETIQUETA_CANAL) as CanalPedido[]).map((item) => ({
              valor: item,
              etiqueta: ETIQUETA_CANAL[item],
            }))}
          />
        </Campo>
        <Campo id="estatus-alta" etiqueta="Estatus">
          <Selector
            id="estatus-alta"
            valor={estatus}
            onCambio={(siguiente) => setEstatus(siguiente as EstatusPedido)}
            opciones={ORDEN_ESTATUS.map((item) => ({
              valor: item,
              etiqueta: ETIQUETA_ESTATUS[item],
              estatus: item,
            }))}
          />
        </Campo>
      </div>
      {falla ? <p className="falla" role="alert">{falla}</p> : null}
      <div className="acciones">
        <Boton type="submit" disabled={enviando || usuarios.length === 0}>{enviando ? "Guardando…" : "Crear pedido"}</Boton>
        <Boton type="button" variante="texto" onClick={onCancelar}>Cancelar</Boton>
      </div>
    </form>
  );
}
