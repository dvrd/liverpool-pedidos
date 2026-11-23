import { cloneElement, isValidElement, type ReactElement, type ReactNode } from "react";

type Props = {
  id: string;
  label: string;
  error?: string;
  className?: string;
  children: ReactNode;
};

type ControlProps = {
  "aria-invalid"?: boolean;
  "aria-describedby"?: string;
};

export function Field({ id, label, error, className, children }: Props) {
  const classes = ["field", className].filter(Boolean).join(" ");
  const describedBy = error ? `${id}-error` : undefined;
  const control = isValidElement(children)
    ? cloneElement(children as ReactElement<ControlProps>, {
        "aria-invalid": error ? true : undefined,
        "aria-describedby": describedBy,
      })
    : children;
  return (
    <div className={classes}>
      <label htmlFor={id}>{label}</label>
      {control}
      {error ? (
        <p className="field-error" id={`${id}-error`}>
          {error}
        </p>
      ) : null}
    </div>
  );
}
