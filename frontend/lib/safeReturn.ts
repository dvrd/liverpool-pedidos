export function safeReturn(value: string | null): string {
  if (value == null || !value.startsWith("/") || value.startsWith("//") || value.includes("\\")) {
    return "/";
  }
  return value;
}
