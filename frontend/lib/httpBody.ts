export type ErrorBody = {
  message?: string;
  details?: Record<string, string>;
};

/** Parsed JSON from a response. Empty text is null. Unreadable text is undefined. */
export function responseData(text: string): unknown {
  if (!text) return null;
  try {
    return JSON.parse(text) as unknown;
  } catch {
    return undefined;
  }
}

export function readJson(text: string): ErrorBody | null {
  const value = responseData(text);
  if (value === undefined) return null;
  return errorFields(value);
}

export function errorFields(value: unknown): ErrorBody | null {
  if (value === null || typeof value !== "object" || Array.isArray(value)) return null;
  const record = value as { message?: unknown; details?: unknown };
  const message = typeof record.message === "string" ? record.message : undefined;
  const details = stringMap(record.details);
  return { message, details };
}

function stringMap(value: unknown): Record<string, string> | undefined {
  if (value === null || typeof value !== "object" || Array.isArray(value)) return undefined;
  const details: Record<string, string> = {};
  for (const [key, item] of Object.entries(value)) {
    if (typeof item === "string") details[key] = item;
  }
  return details;
}
