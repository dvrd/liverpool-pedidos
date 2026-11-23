import type { ReactNode } from "react";

type Props = {
  index: string;
  title: string;
  id: string;
  children: ReactNode;
};

export function Section({ index, title, id, children }: Props) {
  return (
    <section className="section" id={id} aria-labelledby={`${id}-title`} data-testid={id}>
      <header className="section-head">
        <span className="section-index">{index}</span>
        <h2 id={`${id}-title`}>{title}</h2>
      </header>
      <dl className="facts">{children}</dl>
    </section>
  );
}

export function Fact({ label, value }: { label: string; value: ReactNode }) {
  return (
    <div className="fact">
      <dt>{label}</dt>
      <dd>{value}</dd>
    </div>
  );
}
