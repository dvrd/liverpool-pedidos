"use client";

import { Button } from "@/components/ui/Button";
import { Field } from "@/components/ui/Field";
import { Fact, Section } from "@/components/ui/Section";
import { Select } from "@/components/ui/Select";
import { StatusMark } from "@/components/ui/StatusMark";
import { apiFailure, changeStatus, deleteOrder, getOrder, isAbort, updateOrder } from "@/lib/api";
import { CHANNEL_LABEL, channelOptions, formatDate, formatMoney, STATUS_LABEL, statusOptions } from "@/lib/format";
import { parseMoney, priceError, quantityError } from "@/lib/orderFields";
import { safeReturn } from "@/lib/safeReturn";
import type { Order, OrderChannel, OrderStatus } from "@/lib/types";
import Link from "next/link";
import { useParams, useRouter, useSearchParams } from "next/navigation";
import { FormEvent, useEffect, useState } from "react";

export function OrderDetail() {
  const params = useParams<{ id: string }>();
  const search = useSearchParams();
  const router = useRouter();
  const id = params.id;
  const returnTo = safeReturn(search.get("returnTo"));

  const [order, setOrder] = useState<Order | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [status, setStatus] = useState<OrderStatus>("RECEIVED");
  const [savingStatus, setSavingStatus] = useState(false);
  const [savingCorrection, setSavingCorrection] = useState(false);
  const [note, setNote] = useState<string | null>(null);
  const [correcting, setCorrecting] = useState(false);
  const [confirmDelete, setConfirmDelete] = useState(false);
  const [deleting, setDeleting] = useState(false);

  useEffect(() => {
    const controller = new AbortController();
    setLoading(true);
    setError(null);
    getOrder(id, controller.signal)
      .then((data) => {
        setOrder(data);
        setStatus(data.status);
      })
      .catch((cause: unknown) => {
        if (isAbort(cause)) return;
        setError(cause instanceof Error ? cause.message : "No se pudo abrir el pedido.");
      })
      .finally(() => {
        if (!controller.signal.aborted) setLoading(false);
      });
    return () => controller.abort();
  }, [id]);

  useEffect(() => {
    if (!order) return;
    const title = document.title;
    document.title = `${order.orderNumber} · Mesa de pedidos`;
    return () => {
      document.title = title;
    };
  }, [order]);

  async function saveStatus() {
    if (!order) return;
    setSavingStatus(true);
    setNote(null);
    setError(null);
    try {
      const updated = await changeStatus(order.id, status);
      setOrder(updated);
      setStatus(updated.status);
      setNote("Estatus actualizado.");
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : "No se pudo cambiar el estatus.");
    } finally {
      setSavingStatus(false);
    }
  }

  async function remove() {
    if (!order) return;
    setDeleting(true);
    try {
      await deleteOrder(order.id);
      router.push(`/?notice=${encodeURIComponent(`Pedido ${order.orderNumber} eliminado`)}`);
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : "No se pudo eliminar el pedido.");
      setDeleting(false);
    }
  }

  if (loading) return <p className="state">Abriendo el pedido…</p>;
  if (!order) {
    return (
      <div className="state">
        <p role="alert">{error ?? "Este pedido no existe o fue eliminado."}</p>
        <Link href={returnTo}>Volver a la mesa</Link>
      </div>
    );
  }

  return (
    <article className="detail">
      <Link className="back" href={returnTo}>Volver a la mesa</Link>
      <header className="detail-head">
        <div>
          <p className="detail-channel">{CHANNEL_LABEL[order.channel]}</p>
          <h1 data-testid="detail-number">{order.orderNumber}</h1>
          <p className="detail-meta">
            Registrado {formatDate(order.createdAt)}
            {order.updatedAt !== order.createdAt ? ` · Actualizado ${formatDate(order.updatedAt)}` : ""}
          </p>
        </div>
        <div className="detail-status">
          <StatusMark status={order.status} />
          <label className="field" htmlFor="status">
            <span>Cambiar estatus</span>
            <Select
              id="status"
              testId="status"
              value={status}
              onChange={(next) => setStatus(next as OrderStatus)}
              options={statusOptions()}
            />
          </label>
          <Button type="button" data-testid="save-status" disabled={status === order.status || savingStatus || savingCorrection} onClick={() => void saveStatus()}>
            {savingStatus ? "Guardando…" : "Guardar estatus"}
          </Button>
        </div>
      </header>

      {note ? <p className="notice" role="status">{note}</p> : null}
      {error ? <p className="failure" role="alert">{error}</p> : null}

      <div className="sheet">
        <div className="sheet-body">
          <Section index="01" title="Datos del cliente" id="block-customer">
            <Fact label="Nombre" value={order.customer.firstName} />
            <Fact label="Apellido paterno" value={order.customer.paternalLastName} />
            <Fact label="Apellido materno" value={order.customer.maternalLastName} />
            <Fact label="Correo electrónico" value={order.customer.email} />
          </Section>
          <Section index="02" title="Datos de entrega" id="block-delivery">
            <Fact label="Dirección de envío" value={order.shippingAddress} />
          </Section>
          <Section index="03" title="Datos del pedido" id="block-order">
            <Fact label="Código del producto" value={order.productCode} />
            <Fact label="Descripción" value={order.productDescription} />
            <Fact label="Cantidad" value={order.quantity} />
            <Fact label="Precio" value={formatMoney(order.unitPrice)} />
            <Fact label="Importe" value={formatMoney(order.totalAmount)} />
            <Fact label="Estatus de pedido" value={STATUS_LABEL[order.status]} />
          </Section>

          <div className="correction">
            <Button type="button" variant="secondary" onClick={() => setCorrecting((value) => !value)}>
              {correcting ? "Cerrar corrección" : "Corregir datos del pedido"}
            </Button>
            {correcting ? (
              <Correction
                order={order}
                busy={savingStatus}
                onBusy={setSavingCorrection}
                onSaved={(updated) => {
                  setOrder(updated);
                  setStatus((current) => (current === order.status ? updated.status : current));
                  setCorrecting(false);
                  setNote("Pedido actualizado.");
                }}
                onError={setError}
              />
            ) : null}
          </div>

          <div className="danger-zone">
            {confirmDelete ? (
              <>
                <p>Se eliminará {order.orderNumber}. Esta acción no se deshace.</p>
                <div className="actions">
                  <Button type="button" variant="danger" data-testid="confirm-delete" disabled={deleting} onClick={() => void remove()}>
                    {deleting ? "Eliminando…" : "Eliminar pedido"}
                  </Button>
                  <Button type="button" variant="text" onClick={() => setConfirmDelete(false)}>Conservar</Button>
                </div>
              </>
            ) : (
              <Button type="button" variant="text" data-testid="delete" onClick={() => setConfirmDelete(true)}>
                Eliminar pedido
              </Button>
            )}
          </div>
        </div>

        <aside className="history" aria-label="Historial de estatus">
          <h2>Historial</h2>
          <ol>
            {order.history.map((event, index) => (
              <li key={`${event.status}-${event.occurredAt}-${index}`} data-current={index === order.history.length - 1}>
                <span>{STATUS_LABEL[event.status]}</span>
                <time dateTime={event.occurredAt}>{formatDate(event.occurredAt)}</time>
              </li>
            ))}
          </ol>
        </aside>
      </div>
    </article>
  );
}

function Correction({
  order,
  busy,
  onBusy,
  onSaved,
  onError,
}: {
  order: Order;
  busy: boolean;
  onBusy: (busy: boolean) => void;
  onSaved: (order: Order) => void;
  onError: (message: string) => void;
}) {
  const [address, setAddress] = useState(order.shippingAddress);
  const [code, setCode] = useState(order.productCode);
  const [description, setDescription] = useState(order.productDescription);
  const [quantity, setQuantity] = useState(String(order.quantity));
  const [unitPrice, setUnitPrice] = useState(String(order.unitPrice));
  const [channel, setChannel] = useState<OrderChannel>(order.channel);
  const [errors, setErrors] = useState<Record<string, string | undefined>>({});
  const [sending, setSending] = useState(false);

  async function submit(event: FormEvent) {
    event.preventDefault();
    const amount = parseMoney(unitPrice);
    const quantityMessage = quantityError(quantity);
    const priceMessage = priceError(unitPrice);
    if (quantityMessage || priceMessage || amount === undefined) {
      setErrors({ quantity: quantityMessage, unitPrice: priceMessage });
      onError("Revisa cantidad y precio.");
      return;
    }
    setSending(true);
    onBusy(true);
    setErrors({});
    try {
      const updated = await updateOrder(order.id, {
        shippingAddress: address,
        productCode: code,
        productDescription: description,
        quantity: Number(quantity.trim()),
        unitPrice: amount,
        channel,
        status: order.status,
      });
      onSaved(updated);
    } catch (cause) {
      const failure = apiFailure(cause, "No se pudo actualizar el pedido.");
      setErrors(failure.details ?? {});
      onError(failure.message);
    } finally {
      setSending(false);
      onBusy(false);
    }
  }

  return (
    <form className="form-panel" onSubmit={submit}>
      <div className="form-grid">
        <Field id="edit-address" label="Dirección de envío" error={errors.shippingAddress} className="field-wide">
          <textarea id="edit-address" rows={2} value={address} onChange={(event) => setAddress(event.target.value)} />
        </Field>
        <Field id="edit-code" label="Código del producto" error={errors.productCode}>
          <input id="edit-code" value={code} onChange={(event) => setCode(event.target.value)} />
        </Field>
        <Field id="edit-description" label="Descripción" error={errors.productDescription}>
          <input id="edit-description" value={description} onChange={(event) => setDescription(event.target.value)} />
        </Field>
        <Field id="edit-quantity" label="Cantidad" error={errors.quantity}>
          <input id="edit-quantity" inputMode="numeric" value={quantity} onChange={(event) => setQuantity(event.target.value)} />
        </Field>
        <Field id="edit-price" label="Precio" error={errors.unitPrice}>
          <input id="edit-price" inputMode="decimal" value={unitPrice} onChange={(event) => setUnitPrice(event.target.value)} />
        </Field>
        <Field id="edit-channel" label="Canal">
          <Select
            id="edit-channel"
            value={channel}
            onChange={(next) => setChannel(next as OrderChannel)}
            options={channelOptions()}
          />
        </Field>
      </div>
      <Button type="submit" disabled={sending || busy}>{sending ? "Guardando…" : "Guardar corrección"}</Button>
    </form>
  );
}


