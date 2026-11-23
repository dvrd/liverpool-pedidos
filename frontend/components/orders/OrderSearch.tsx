"use client";

import { isAbort, suggestOrders } from "@/lib/api";
import { SUGGESTION_LABEL } from "@/lib/format";
import { EMPTY_SEARCH_MESSAGE, INVALID_RESPONSE_MESSAGE, SEARCH_FAILED_MESSAGE } from "@/lib/messages";
import { matchSpan } from "@/lib/text";
import type { Suggestion } from "@/lib/types";
import { useEffect, useId, useRef, useState } from "react";

type Props = {
  appliedTerm: string;
  onFilter: (term: string) => void;
};

export function OrderSearch({ appliedTerm, onFilter }: Props) {
  const listId = useId();
  const box = useRef<HTMLDivElement>(null);
  const chosen = useRef<string | null>(null);
  const [draft, setDraft] = useState(appliedTerm);
  const [suggestions, setSuggestions] = useState<Suggestion[]>([]);
  const [open, setOpen] = useState(false);
  const [loading, setLoading] = useState(false);
  const [active, setActive] = useState(-1);
  const [noResults, setNoResults] = useState(false);
  const [problem, setProblem] = useState<string | null>(null);

  useEffect(() => {
    setDraft(appliedTerm);
  }, [appliedTerm]);

  useEffect(() => {
    const term = draft.trim();
    if (chosen.current === term) {
      setOpen(false);
      setLoading(false);
      return;
    }
    if (term.length < 2) {
      setSuggestions([]);
      setNoResults(false);
      setProblem(null);
      setLoading(false);
      return;
    }

    const controller = new AbortController();
    const wait = window.setTimeout(() => {
      setLoading(true);
      setProblem(null);
      suggestOrders(term, controller.signal)
        .then((options) => {
          if (!Array.isArray(options)) {
            setSuggestions([]);
            setNoResults(false);
            setProblem(INVALID_RESPONSE_MESSAGE);
            setOpen(true);
            return;
          }
          setSuggestions(options);
          setNoResults(options.length === 0);
          setProblem(null);
          setActive(options.length > 0 ? 0 : -1);
          setOpen(true);
        })
        .catch((error: unknown) => {
          if (isAbort(error)) return;
          setSuggestions([]);
          setNoResults(false);
          setProblem(SEARCH_FAILED_MESSAGE);
          setOpen(true);
        })
        .finally(() => {
          if (!controller.signal.aborted) setLoading(false);
        });
    }, 220);

    return () => {
      window.clearTimeout(wait);
      controller.abort();
    };
  }, [draft]);

  useEffect(() => {
    function close(event: MouseEvent) {
      if (!box.current?.contains(event.target as Node)) setOpen(false);
    }
    document.addEventListener("mousedown", close);
    return () => document.removeEventListener("mousedown", close);
  }, []);

  useEffect(() => {
    if (active < 0) return;
    document.getElementById(`${listId}-option-${active}`)?.scrollIntoView({ block: "nearest" });
  }, [active, listId]);

  function choose(option: Suggestion) {
    chosen.current = option.value;
    setSuggestions([]);
    setNoResults(false);
    setDraft(option.value);
    setOpen(false);
    onFilter(option.value);
  }

  function confirm() {
    setOpen(false);
    onFilter(draft.trim());
  }

  const termIsLong = draft.trim().length >= 2;
  const showList = open && termIsLong && (loading || suggestions.length > 0 || noResults || problem !== null);
  const optionsId = `${listId}-options`;

  return (
    <form
      className="search"
      role="search"
      onSubmit={(event) => {
        event.preventDefault();
        confirm();
      }}
    >
      <label htmlFor="search">Buscar en la mesa</label>
      <div className="search-box" ref={box}>
        <input
          id="search"
          data-testid="search"
          role="combobox"
          aria-expanded={showList}
          aria-controls={showList ? (suggestions.length > 0 ? optionsId : listId) : undefined}
          aria-autocomplete="list"
          aria-activedescendant={showList && active >= 0 ? `${listId}-option-${active}` : undefined}
          autoComplete="off"
          spellCheck={false}
          placeholder="Cliente, folio, correo o código"
          value={draft}
          onChange={(event) => {
            setDraft(event.target.value);
            setOpen(true);
          }}
          onFocus={() => {
            if (termIsLong) setOpen(true);
          }}
          onKeyDown={(event) => {
            if (event.key === "ArrowDown") {
              event.preventDefault();
              setOpen(true);
              setActive((index) => Math.min(index + 1, suggestions.length - 1));
            } else if (event.key === "ArrowUp") {
              event.preventDefault();
              setActive((index) => Math.max(index - 1, 0));
            } else if (event.key === "Enter" && active >= 0 && suggestions[active] && open) {
              event.preventDefault();
              choose(suggestions[active]);
            } else if (event.key === "Escape") {
              setOpen(false);
            }
          }}
        />
        {draft ? (
          <button
            type="button"
            className="clear"
            onClick={() => {
              chosen.current = null;
              setDraft("");
              setOpen(false);
              onFilter("");
            }}
          >
            Limpiar
          </button>
        ) : null}
        {showList ? (
          <div className="popover" id={listId}>
            {loading && suggestions.length === 0 ? <p className="popover-note">Buscando…</p> : null}
            {suggestions.length > 0 ? (
              <ul id={optionsId} role="listbox" data-testid="suggestions">
                {suggestions.map((option, index) => (
                  <li key={`${option.type}-${option.value}`} role="presentation">
                    <button
                      type="button"
                      role="option"
                      id={`${listId}-option-${index}`}
                      aria-selected={index === active}
                      className={index === active ? "option active" : "option"}
                      onMouseEnter={() => setActive(index)}
                      onClick={() => choose(option)}
                    >
                      <span className="option-kind">{SUGGESTION_LABEL[option.type]}</span>
                      <span className="option-label">{highlight(option.label, draft)}</span>
                      <span className="option-detail">{option.detail}</span>
                    </button>
                  </li>
                ))}
              </ul>
            ) : null}
            {!loading && problem ? (
              <p className="failure" role="alert">{problem}</p>
            ) : null}
            {!loading && noResults && !problem ? (
              <p className="no-results" role="status" data-testid="no-results">
                {EMPTY_SEARCH_MESSAGE}
              </p>
            ) : null}
          </div>
        ) : null}
      </div>
    </form>
  );
}

function highlight(text: string, needle: string) {
  const span = matchSpan(text, needle);
  if (!span) return text;
  return (
    <>
      {text.slice(0, span.start)}
      <mark>{text.slice(span.start, span.end)}</mark>
      {text.slice(span.end)}
    </>
  );
}
