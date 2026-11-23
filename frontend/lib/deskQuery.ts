export function deskHref(
  current: { term: string; status: string },
  next: { term?: string; status?: string; notice?: string },
): string {
  const query = new URLSearchParams();
  const term = next.term ?? current.term;
  const status = next.status ?? current.status;
  if (term) query.set("term", term);
  if (status) query.set("status", status);
  if (next.notice) query.set("notice", next.notice);
  const text = query.toString();
  return text ? `/?${text}` : "/";
}
