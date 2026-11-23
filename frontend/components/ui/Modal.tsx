"use client";

import { useEffect, useRef, type ReactNode, type SyntheticEvent } from "react";

export function Modal({
  labelId,
  onClose,
  children,
}: {
  labelId: string;
  onClose: () => void;
  children: ReactNode;
}) {
  const dialogRef = useRef<HTMLDialogElement>(null);
  const closeRef = useRef(onClose);
  closeRef.current = onClose;

  useEffect(() => {
    const dialog = dialogRef.current;
    if (!dialog) return;
    if (!dialog.open) dialog.showModal();
    return () => {
      if (dialog.open) dialog.close();
    };
  }, []);

  function requestClose(event?: SyntheticEvent | Event) {
    event?.preventDefault();
    closeRef.current();
  }

  return (
    <dialog
      ref={dialogRef}
      className="modal"
      aria-labelledby={labelId}
      onCancel={requestClose}
      onMouseDown={(event) => {
        if (event.target === event.currentTarget) requestClose(event);
      }}
    >
      <div className="modal-body">{children}</div>
    </dialog>
  );
}
