"use client";

import { CreateCustomerForm, CreateOrderForm } from "@/components/orders/CreateForms";
import { OrderSearch } from "@/components/orders/OrderSearch";
import { Button } from "@/components/ui/Button";
import { Modal } from "@/components/ui/Modal";
import { StatusMark } from "@/components/ui/StatusMark";
import { isAbort, listCustomers, listOrders } from "@/lib/api";
import { deskHref } from "@/lib/deskQuery";
import { CHANNEL_LABEL, countText, formatMoney, STATUS_LABEL, STATUS_ORDER } from "@/lib/format";
import { EMPTY_SEARCH_MESSAGE, INVALID_RESPONSE_MESSAGE } from "@/lib/messages";
import { hasAnotherPage } from "@/lib/ordersPath";
import type { Customer, Order, OrderStatus } from "@/lib/types";
import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { useCallback, useEffect, useRef, useState } from "react";

type Panel = "order" | "customer" | null;

export function OrderDesk() {
  const router = useRouter();
  const params = useSearchParams();
  const term = params.get("term") ?? "";
  const status = params.get("status") ?? "";
  const notice = params.get("notice") ?? "";

  const [orders, setOrders] = useState<Order[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [note, setNote] = useState<string | null>(null);
  const [panel, setPanel] = useState<Panel>(null);
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [customersFailed, setCustomersFailed] = useState(false);
  const requestId = useRef(0);

  const load = useCallback(async (pageIndex: number, signal?: AbortSignal) => {
    const id = ++requestId.current;
    if (pageIndex === 0) setLoading(true);
    else setLoadingMore(true);
    setError(null);
    try {
      const result = await listOrders({ term, status, page: pageIndex }, signal);
      if (id !== requestId.current) return;
      const incoming = result?.content;
      if (!Array.isArray(incoming)) {
        setOrders([]);
        setTotal(0);
        setTotalPages(0);
        setError(INVALID_RESPONSE_MESSAGE);
        return;
      }
      setOrders((current) => (pageIndex === 0 ? incoming : [...current, ...incoming]));
      setTotal(result.totalElements);
      setTotalPages(result.totalPages);
      setPage(pageIndex);
    } catch (cause) {
      if (isAbort(cause) || id !== requestId.current) return;
      setError(cause instanceof Error ? cause.message : "No se pudo cargar la mesa.");
    } finally {
      if (id === requestId.current && !signal?.aborted) {
        setLoading(false);
        setLoadingMore(false);
      }
    }
  }, [status, term]);

  useEffect(() => {
    const controller = new AbortController();
    void load(0, controller.signal);
    return () => controller.abort();
  }, [load]);

  useEffect(() => {
    let current = true;
    listCustomers()
      .then((list) => {
        if (!current) return;
        if (!Array.isArray(list)) {
          setCustomers([]);
          setCustomersFailed(true);
          return;
        }
        setCustomers(list);
        setCustomersFailed(false);
      })
      .catch(() => {
        if (!current) return;
        setCustomers([]);
        setCustomersFailed(true);
      });
    return () => {
      current = false;
    };
  }, [note]);

  function apply(next: { term?: string; status?: string; notice?: string }) {
    router.replace(deskHref({ term, status }, next));
  }

  const returnTo = params.toString() ? `/?${params.toString()}` : "/";
  const message = note || notice;
  const filtered = Boolean(term);

  return (
    <div className="panel">
      <div className="desk-head">
        <div>
          <p className="count" data-testid="count" aria-live="polite">
            {loading ? "Consultando pedidos…" : countText(total, filtered || Boolean(status), orders.length)}
          </p>
          <OrderSearch appliedTerm={term} onFilter={(value) => apply({ term: value })} />
          {term ? (
            <p className="active-filter">
              Filtro: <strong>{term}</strong>
              <button type="button" onClick={() => apply({ term: "" })}>Quitar</button>
            </p>
          ) : null}
        </div>
        <div className="shortcuts">
          <Button type="button" variant={panel === "customer" ? "secondary" : "primary"} data-testid="open-order" onClick={() => setPanel(panel === "order" ? null : "order")}>
            Registrar pedido
          </Button>
          <Button type="button" variant={panel === "customer" ? "primary" : "secondary"} data-testid="open-customer" onClick={() => setPanel(panel === "customer" ? null : "customer")}>
            Nuevo cliente
          </Button>
        </div>
      </div>

      <div className="status-filters" role="toolbar" aria-label="Filtrar por estatus">
        <button type="button" aria-pressed={status === ""} className={status === "" ? "chip active" : "chip"} onClick={() => apply({ status: "" })}>
          Todos
        </button>
        {STATUS_ORDER.map((value) => (
          <button
            key={value}
            type="button"
            aria-pressed={status === value}
            className={status === value ? "chip active" : "chip"}
            onClick={() => apply({ status: value })}
          >
            {STATUS_LABEL[value as OrderStatus]}
          </button>
        ))}
      </div>

      {message ? <p className="notice" role="status">{message}</p> : null}
      {error ? (
        <p className="failure" role="alert">
          {error} <button type="button" onClick={() => void load(0)}>Reintentar</button>
        </p>
      ) : null}

      {panel ? (
        <Modal labelId={panel === "customer" ? "customer-dialog-title" : "order-dialog-title"} onClose={() => setPanel(null)}>
          {panel === "customer" ? (
            <CreateCustomerForm
              onCancel={() => setPanel(null)}
              onCreated={(name) => {
                setPanel(null);
                setNote(`Cliente ${name} registrado.`);
              }}
            />
          ) : (
            <CreateOrderForm
              customers={customers}
              customersFailed={customersFailed}
              onCancel={() => setPanel(null)}
              onCreated={(orderNumber) => {
                setPanel(null);
                setNote(`Pedido ${orderNumber} registrado.`);
                void load(0);
              }}
            />
          )}
        </Modal>
      ) : null}

      <section className="list" aria-busy={loading} data-testid="list">
        {loading ? <ListSkeleton /> : null}
        {!loading && orders.length === 0 && term ? (
          <p className="no-results empty-list" data-testid="empty-list">{EMPTY_SEARCH_MESSAGE}</p>
        ) : null}
        {!loading && orders.length === 0 && !term && status ? (
          <p className="empty-list">No hay pedidos con estatus {STATUS_LABEL[status as OrderStatus] ?? status}.</p>
        ) : null}
        {!loading && orders.length === 0 && !term && !status ? (
          <p className="empty-list">Aún no hay pedidos registrados.</p>
        ) : null}

        {!loading && orders.length > 0 ? (
          <>
            <div className="table-wrap">
              <table className="table">
                <thead>
                  <tr>
                    <th scope="col">Folio</th>
                    <th scope="col">Cliente</th>
                    <th scope="col">Pedido</th>
                    <th scope="col">Canal</th>
                    <th scope="col">Estatus</th>
                    <th scope="col" className="numeric">Importe</th>
                  </tr>
                </thead>
                <tbody>
                  {orders.map((order, index) => (
                    <tr key={order.id} className="row" style={{ animationDelay: `${index * 35}ms` }}>
                      <td>
                        <Link className="stretched" href={orderHref(order.id, returnTo)} data-testid={`order-${order.orderNumber}`}>
                          {order.orderNumber}
                        </Link>
                      </td>
                      <td>
                        <span className="name">{order.customer.fullName}</span>
                        <span className="muted">{order.customer.email}</span>
                      </td>
                      <td>
                        <span className="name">{order.productDescription}</span>
                        <span className="muted">{order.productCode}</span>
                      </td>
                      <td>{CHANNEL_LABEL[order.channel]}</td>
                      <td><StatusMark status={order.status} /></td>
                      <td className="numeric">{formatMoney(order.totalAmount)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            <div className="cards">
              {orders.map((order) => (
                <Link key={order.id} className="card" href={orderHref(order.id, returnTo)}>
                  <div className="card-head">
                    <span className="order-number">{order.orderNumber}</span>
                    <StatusMark status={order.status} />
                  </div>
                  <p className="card-name">{order.customer.fullName}</p>
                  <p className="card-product">{order.productDescription}</p>
                  <p className="muted">{order.productCode} · {CHANNEL_LABEL[order.channel]}</p>
                  <p className="card-amount">{formatMoney(order.totalAmount)}</p>
                </Link>
              ))}
            </div>
            {hasAnotherPage(page, totalPages) ? (
              <div className="list-more">
                <Button type="button" variant="secondary" data-testid="more-orders" disabled={loadingMore} onClick={() => void load(page + 1)}>
                  {loadingMore ? "Cargando…" : "Mostrar más"}
                </Button>
              </div>
            ) : null}
          </>
        ) : null}
      </section>
    </div>
  );
}

function orderHref(id: string, returnTo: string) {
  return `/orders/${id}?returnTo=${encodeURIComponent(returnTo)}`;
}

function ListSkeleton() {
  return (
    <div className="skeleton" aria-hidden="true">
      <span />
      <span />
      <span />
      <span />
    </div>
  );
}
