"use client";

import { Button } from "@/components/ui/Button";
import { Field } from "@/components/ui/Field";
import { Select } from "@/components/ui/Select";
import { apiFailure, createCustomer, createOrder } from "@/lib/api";
import { channelOptions, statusOptions } from "@/lib/format";
import { CUSTOMERS_FAILED_MESSAGE } from "@/lib/messages";
import { parseMoney, priceError, quantityError, selectedCustomerId } from "@/lib/orderFields";
import type { Customer, OrderChannel, OrderStatus } from "@/lib/types";
import { FormEvent, useEffect, useState } from "react";

type Errors = Record<string, string | undefined>;

export function CreateCustomerForm({
  onCreated,
  onCancel,
}: {
  onCreated: (name: string) => void;
  onCancel: () => void;
}) {
  const [firstName, setFirstName] = useState("");
  const [paternalLastName, setPaternalLastName] = useState("");
  const [maternalLastName, setMaternalLastName] = useState("");
  const [email, setEmail] = useState("");
  const [errors, setErrors] = useState<Errors>({});
  const [failure, setFailure] = useState<string | null>(null);
  const [sending, setSending] = useState(false);

  async function submit(event: FormEvent) {
    event.preventDefault();
    setSending(true);
    setFailure(null);
    setErrors({});
    try {
      const created = await createCustomer({
        firstName,
        paternalLastName,
        maternalLastName,
        email,
      });
      onCreated(created.fullName);
    } catch (error) {
      const failure = apiFailure(error, "No se pudo registrar el cliente.");
      setErrors(failure.details ?? {});
      setFailure(failure.message);
    } finally {
      setSending(false);
    }
  }

  return (
    <form className="form-panel" data-testid="customer-form" onSubmit={submit}>
      <h2>Nuevo cliente</h2>
      <div className="form-grid">
        <Field id="firstName" label="Nombre" error={errors.firstName}>
          <input id="firstName" value={firstName} onChange={(event) => setFirstName(event.target.value)} />
        </Field>
        <Field id="paternalLastName" label="Apellido paterno" error={errors.paternalLastName}>
          <input id="paternalLastName" value={paternalLastName} onChange={(event) => setPaternalLastName(event.target.value)} />
        </Field>
        <Field id="maternalLastName" label="Apellido materno" error={errors.maternalLastName}>
          <input id="maternalLastName" value={maternalLastName} onChange={(event) => setMaternalLastName(event.target.value)} />
        </Field>
        <Field id="email" label="Correo electrónico" error={errors.email}>
          <input id="email" type="email" autoComplete="off" value={email} onChange={(event) => setEmail(event.target.value)} />
        </Field>
      </div>
      {failure ? <p className="failure" role="alert">{failure}</p> : null}
      <div className="actions">
        <Button type="submit" disabled={sending}>{sending ? "Guardando…" : "Registrar cliente"}</Button>
        <Button type="button" variant="text" onClick={onCancel}>Cancelar</Button>
      </div>
    </form>
  );
}

export function CreateOrderForm({
  customers,
  customersFailed = false,
  onCreated,
  onCancel,
}: {
  customers: Customer[];
  customersFailed?: boolean;
  onCreated: (orderNumber: string) => void;
  onCancel: () => void;
}) {
  const [customerId, setCustomerId] = useState(customers[0]?.id ?? "");
  const [address, setAddress] = useState("");
  const [code, setCode] = useState("");
  const [description, setDescription] = useState("");
  const [quantity, setQuantity] = useState("1");
  const [unitPrice, setUnitPrice] = useState("");
  const [channel, setChannel] = useState<OrderChannel>("WEBSITE");
  const [status, setStatus] = useState<OrderStatus>("RECEIVED");
  const [errors, setErrors] = useState<Errors>({});
  const [failure, setFailure] = useState<string | null>(null);
  const [sending, setSending] = useState(false);

  useEffect(() => {
    setCustomerId((current) => selectedCustomerId(current, customers));
  }, [customers]);

  async function submit(event: FormEvent) {
    event.preventDefault();
    const amount = parseMoney(unitPrice);
    const local: Errors = {};
    if (!customerId) local.customerId = "Elige un cliente.";
    const quantityMessage = quantityError(quantity);
    const priceMessage = priceError(unitPrice);
    if (quantityMessage) local.quantity = quantityMessage;
    if (priceMessage) local.unitPrice = priceMessage;
    if (Object.keys(local).length > 0 || amount === undefined) {
      setErrors(local);
      setFailure("Revisa los campos marcados.");
      return;
    }

    setSending(true);
    setFailure(null);
    setErrors({});
    try {
      const created = await createOrder({
        customerId,
        shippingAddress: address,
        productCode: code,
        productDescription: description,
        quantity: Number(quantity.trim()),
        unitPrice: amount,
        channel,
        status,
      });
      onCreated(created.orderNumber);
    } catch (error) {
      const failure = apiFailure(error, "No se pudo registrar el pedido.");
      setErrors(failure.details ?? {});
      setFailure(failure.message);
    } finally {
      setSending(false);
    }
  }

  return (
    <form className="form-panel" data-testid="order-form" onSubmit={submit}>
      <h2>Registrar pedido</h2>
      {customersFailed ? <p className="failure" role="alert">{CUSTOMERS_FAILED_MESSAGE}</p> : null}
      {!customersFailed && customers.length === 0 ? <p className="popover-note">Primero registra un cliente.</p> : null}
      <div className="form-grid">
        <Field id="customerId" label="Cliente" error={errors.customerId} className="field-wide">
          <Select
            id="customerId"
            value={customerId}
            disabled={customers.length === 0}
            onChange={setCustomerId}
            options={customers.map((customer) => ({
              value: customer.id,
              label: customer.fullName,
              detail: customer.email,
            }))}
          />
        </Field>
        <Field id="address" label="Dirección de envío" error={errors.shippingAddress} className="field-wide">
          <textarea id="address" rows={2} value={address} onChange={(event) => setAddress(event.target.value)} />
        </Field>
        <Field id="code" label="Código del producto" error={errors.productCode}>
          <input id="code" value={code} onChange={(event) => setCode(event.target.value)} />
        </Field>
        <Field id="description" label="Descripción" error={errors.productDescription}>
          <input id="description" value={description} onChange={(event) => setDescription(event.target.value)} />
        </Field>
        <Field id="quantity" label="Cantidad" error={errors.quantity}>
          <input id="quantity" inputMode="numeric" value={quantity} onChange={(event) => setQuantity(event.target.value)} />
        </Field>
        <Field id="unitPrice" label="Precio unitario" error={errors.unitPrice}>
          <input id="unitPrice" inputMode="decimal" value={unitPrice} onChange={(event) => setUnitPrice(event.target.value)} />
        </Field>
        <Field id="channel" label="Canal">
          <Select
            id="channel"
            value={channel}
            onChange={(next) => setChannel(next as OrderChannel)}
            options={channelOptions()}
          />
        </Field>
        <Field id="create-status" label="Estatus">
          <Select
            id="create-status"
            value={status}
            onChange={(next) => setStatus(next as OrderStatus)}
            options={statusOptions()}
          />
        </Field>
      </div>
      {failure ? <p className="failure" role="alert">{failure}</p> : null}
      <div className="actions">
        <Button type="submit" disabled={sending || customersFailed || customers.length === 0}>{sending ? "Guardando…" : "Crear pedido"}</Button>
        <Button type="button" variant="text" onClick={onCancel}>Cancelar</Button>
      </div>
    </form>
  );
}
