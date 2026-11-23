const SEGMENT = /^[A-Za-z0-9._-]+$/;

export function apiUrl(base: string, segments: string[], search: string): string | null {
  if (segments.length === 0 || segments.some((segment) => segment === "." || segment === ".." || !SEGMENT.test(segment))) {
    return null;
  }
  const root = (base.trim() || "http://127.0.0.1:8080").replace(/\/$/, "");
  return `${root}/api/${segments.join("/")}${search}`;
}
