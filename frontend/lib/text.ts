export function fold(value: string): string {
  return value.normalize("NFD").replace(/[\u0300-\u036f]/g, "").toLowerCase();
}

export function matchSpan(text: string, needle: string): { start: number; end: number } | null {
  const query = fold(needle.trim());
  if (query.length < 2) return null;
  const at = fold(text).indexOf(query);
  if (at < 0) return null;
  return { start: originalIndex(text, at), end: originalIndex(text, at + query.length) };
}

function originalIndex(text: string, foldedIndex: number): number {
  let folded = 0;
  for (let index = 0; index < text.length; ) {
    if (folded >= foldedIndex) return index;
    const char = String.fromCodePoint(text.codePointAt(index) ?? 0);
    folded += fold(char).length;
    index += char.length;
  }
  return text.length;
}
