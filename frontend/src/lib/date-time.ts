/** Parse API LocalDateTime values as UTC; preserve explicit offsets when present. */
export function parseApiDateTime(value: string): Date {
  const includesTimezone = /(?:Z|[+-]\d{2}:?\d{2})$/i.test(value);
  return new Date(includesTimezone ? value : `${value}Z`);
}

export function formatApiDateTime(value: string, options?: Intl.DateTimeFormatOptions): string {
  return parseApiDateTime(value).toLocaleString(undefined, options);
}

export function formatApiDate(value: string, options?: Intl.DateTimeFormatOptions): string {
  return parseApiDateTime(value).toLocaleDateString(undefined, options);
}
