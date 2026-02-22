const TIME_FORMATTER = new Intl.DateTimeFormat('en-GB', {
  year: 'numeric',
  month: 'short',
  day: '2-digit',
  hour: '2-digit',
  minute: '2-digit',
  second: '2-digit'
});

export function formatTimestamp(iso: string | null): string {
  if (!iso) {
    return 'N/A';
  }

  const date = new Date(iso);
  if (Number.isNaN(date.getTime())) {
    return 'Invalid date';
  }

  return TIME_FORMATTER.format(date);
}

export function toLocalTimeInputValue(date: Date): string {
  const pad = (value: number): string => String(value).padStart(2, '0');
  const hours = pad(date.getHours());
  const minutes = pad(date.getMinutes());
  const seconds = pad(date.getSeconds());

  return `${hours}:${minutes}:${seconds}`;
}

export function toIsoFromDateAndTime(date: Date | null, time: string): string | null {
  if (!date || !time) {
    return null;
  }

  const [hours, minutes, seconds] = time.split(':').map(value => Number(value));
  if ([hours, minutes].some(value => Number.isNaN(value))) {
    return null;
  }

  const resolvedSeconds = Number.isNaN(seconds) ? 0 : seconds;
  const result = new Date(date);
  result.setHours(hours, minutes, resolvedSeconds, 0);

  if (Number.isNaN(result.getTime())) {
    return null;
  }

  return result.toISOString();
}
