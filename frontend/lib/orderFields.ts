export function quantityError(raw: string): string | undefined {
  const text = raw.trim();
  if (!/^\d+$/.test(text)) return "La cantidad mínima es 1.";
  const quantity = Number(text);
  if (quantity < 1) return "La cantidad mínima es 1.";
  if (quantity > 99) return "La cantidad máxima es 99.";
  return undefined;
}

export function parseMoney(raw: string): number | undefined {
  const text = raw.trim().replace(",", ".");
  const match = /^(\d{1,8})(?:\.(\d{1,2}))?$/.exec(text);
  if (!match) return undefined;
  const cents = Number(match[1]) * 100 + Number((match[2] ?? "").padEnd(2, "0"));
  if (!Number.isSafeInteger(cents) || cents <= 0) return undefined;
  return cents / 100;
}

export function priceError(raw: string): string | undefined {
  return parseMoney(raw) === undefined ? "El precio debe ser mayor a cero." : undefined;
}

export function selectedCustomerId(current: string, customers: { id: string }[]): string {
  if (current && customers.some((customer) => customer.id === current)) return current;
  return customers[0]?.id ?? "";
}
