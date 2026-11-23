export const PAGE_SIZE = 50;

export function ordersPath(filter: { term?: string; status?: string; page?: number }): string {
  const params = new URLSearchParams();
  params.set("page", String(filter.page ?? 0));
  params.set("size", String(PAGE_SIZE));
  if (filter.term) params.set("term", filter.term);
  if (filter.status) params.set("status", filter.status);
  return `/api/v1/orders?${params.toString()}`;
}

export function hasAnotherPage(page: number, totalPages: number): boolean {
  return page >= 0 && page + 1 < totalPages;
}
