"use client";

import { fold } from "@/lib/text";
import type { OrderStatus } from "@/lib/types";
import { useEffect, useId, useLayoutEffect, useRef, useState } from "react";
import { createPortal } from "react-dom";

export type SelectOption = {
  value: string;
  label: string;
  detail?: string;
  status?: OrderStatus;
};

type Props = {
  id: string;
  value: string;
  options: SelectOption[];
  onChange: (value: string) => void;
  disabled?: boolean;
  testId?: string;
  "aria-invalid"?: boolean;
  "aria-describedby"?: string;
};

export function Select({
  id,
  value,
  options,
  onChange,
  disabled = false,
  testId,
  "aria-invalid": invalid,
  "aria-describedby": describedBy,
}: Props) {
  const listId = useId();
  const box = useRef<HTMLDivElement>(null);
  const menuRef = useRef<HTMLDivElement>(null);
  const buffer = useRef("");
  const timer = useRef<number | null>(null);
  const [open, setOpen] = useState(false);
  const [active, setActive] = useState(-1);
  const current = options.find((option) => option.value === value);
  const empty = options.length === 0;
  const text = current
    ? current.detail
      ? `${current.label} · ${current.detail}`
      : current.label
    : empty
      ? "Sin opciones"
      : "Elige una opción";

  function measure() {
    const root = box.current;
    const menu = menuRef.current;
    if (!root || !menu) return;
    const rect = root.getBoundingClientRect();
    const gap = 6;
    const edge = 12;
    const below = window.innerHeight - rect.bottom - gap - edge;
    const above = rect.top - gap - edge;
    const needed = menu.scrollHeight + 2;
    const opensAbove = below < needed && above > below;
    menu.dataset.side = opensAbove ? "above" : "below";
    menu.style.maxHeight = `${Math.max(0, Math.min(320, opensAbove ? above : below))}px`;
    const dialog = root.closest("dialog");
    if (!dialog) return;
    const origin = dialog.getBoundingClientRect();
    menu.style.position = "absolute";
    menu.style.left = `${rect.left - origin.left}px`;
    menu.style.width = `${rect.width}px`;
    menu.style.right = "auto";
    if (opensAbove) {
      menu.style.top = "auto";
      menu.style.bottom = `${origin.bottom - rect.top + 6}px`;
    } else {
      menu.style.top = `${rect.bottom - origin.top + 6}px`;
      menu.style.bottom = "auto";
    }
  }

  function openMenu() {
    if (disabled || empty) return;
    setActive(Math.max(0, options.findIndex((option) => option.value === value)));
    setOpen(true);
  }

  function choose(option: SelectOption) {
    onChange(option.value);
    setOpen(false);
  }

  useLayoutEffect(() => {
    if (open) measure();
  }, [open, options.length]);

  useEffect(() => {
    if (!open) return;
    function close(event: MouseEvent) {
      const target = event.target as Node;
      if (!box.current?.contains(target) && !menuRef.current?.contains(target)) setOpen(false);
    }
    function follow() {
      measure();
    }
    document.addEventListener("mousedown", close);
    window.addEventListener("resize", follow);
    window.addEventListener("scroll", follow, true);
    return () => {
      document.removeEventListener("mousedown", close);
      window.removeEventListener("resize", follow);
      window.removeEventListener("scroll", follow, true);
    };
  }, [open]);

  useEffect(() => {
    if (!open || active < 0) return;
    document.getElementById(`${listId}-option-${active}`)?.scrollIntoView({ block: "nearest" });
  }, [open, active, listId]);

  useEffect(() => {
    return () => {
      if (timer.current) window.clearTimeout(timer.current);
    };
  }, []);

  function searchByKey(key: string) {
    buffer.current = (buffer.current + key).slice(-32);
    if (timer.current) window.clearTimeout(timer.current);
    timer.current = window.setTimeout(() => {
      buffer.current = "";
    }, 700);
    const query = fold(buffer.current);
    const index = options.findIndex((option) =>
      fold(`${option.label} ${option.detail ?? ""}`).startsWith(query),
    );
    if (index < 0) return;
    setActive(index);
    setOpen(true);
  }

  const menu = open ? (
    <div ref={menuRef} id={listId} className="select-menu" role="listbox">
      {options.map((option, index) => (
        <button
          key={option.value}
          type="button"
          role="option"
          id={`${listId}-option-${index}`}
          aria-selected={option.value === value}
          data-status={option.status}
          className={index === active ? "menu-option active" : "menu-option"}
          tabIndex={-1}
          onMouseEnter={() => setActive(index)}
          onMouseDown={(event) => event.preventDefault()}
          onClick={() => choose(option)}
        >
          {option.status ? <span className="swatch" aria-hidden="true" /> : null}
          <span className="menu-text">
            <span className="menu-label">{option.label}</span>
            {option.detail ? <span className="menu-detail">{option.detail}</span> : null}
          </span>
        </button>
      ))}
    </div>
  ) : null;
  const dialog = box.current?.closest("dialog");

  return (
    <div className="select" ref={box}>
      <button
        id={id}
        type="button"
        className="select-button"
        role="combobox"
        aria-haspopup="listbox"
        aria-expanded={open}
        aria-controls={listId}
        aria-invalid={invalid || undefined}
        aria-describedby={describedBy}
        aria-autocomplete="list"
        aria-activedescendant={open && active >= 0 ? `${listId}-option-${active}` : undefined}
        data-status={current?.status}
        data-testid={testId}
        disabled={disabled || empty}
        onClick={() => (open ? setOpen(false) : openMenu())}
        onKeyDown={(event) => {
          if (disabled || empty) return;
          if (event.key === "ArrowDown") {
            event.preventDefault();
            if (!open) openMenu();
            else setActive((index) => Math.min(index + 1, options.length - 1));
          } else if (event.key === "ArrowUp") {
            event.preventDefault();
            if (!open) openMenu();
            else setActive((index) => Math.max(index - 1, 0));
          } else if (event.key === "Home") {
            event.preventDefault();
            setOpen(true);
            setActive(0);
          } else if (event.key === "End") {
            event.preventDefault();
            setOpen(true);
            setActive(options.length - 1);
          } else if (event.key === "Enter" || event.key === " ") {
            event.preventDefault();
            if (!open) openMenu();
            else if (options[active]) choose(options[active]);
          } else if (event.key === "Escape") {
            if (!open) return;
            event.preventDefault();
            event.stopPropagation();
            setOpen(false);
          } else if (event.key === "Tab") {
            setOpen(false);
          } else if (event.key.length === 1 && !event.metaKey && !event.ctrlKey && !event.altKey) {
            searchByKey(event.key);
          }
        }}
      >
        <span className="select-value">
          {current?.status ? <span className="swatch" aria-hidden="true" /> : null}
          <span className="select-text">{text}</span>
        </span>
        <span className="select-arrow" aria-hidden="true" />
      </button>
      {dialog && menu ? createPortal(menu, dialog) : menu}
    </div>
  );
}
