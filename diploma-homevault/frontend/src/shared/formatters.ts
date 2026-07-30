const dateTimeFormatter = new Intl.DateTimeFormat('ru-RU', {
  day: '2-digit',
  month: '2-digit',
  year: 'numeric',
  hour: '2-digit',
  minute: '2-digit',
});

const numberFormatter = new Intl.NumberFormat('ru-RU');

export function formatDateTime(value: string | null | undefined) {
  if (!value) {
    return '—';
  }

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return '—';
  }

  return dateTimeFormatter.format(date);
}

export function formatBytes(bytes: number | null | undefined) {
  if (bytes === null || bytes === undefined) {
    return '—';
  }

  if (bytes === 0) {
    return '0 Б';
  }

  const units = ['Б', 'КБ', 'МБ', 'ГБ', 'ТБ'];
  const exponent = Math.min(Math.floor(Math.log(bytes) / Math.log(1024)), units.length - 1);
  const value = bytes / 1024 ** exponent;

  return `${value >= 10 ? value.toFixed(0) : value.toFixed(1)} ${units[exponent]}`;
}

export function formatNumber(value: number | null | undefined) {
  return value === null || value === undefined ? '—' : numberFormatter.format(value);
}

export function toLocalDateTimeInput(date: Date) {
  const pad = (value: number) => value.toString().padStart(2, '0');

  return [
    date.getFullYear(),
    pad(date.getMonth() + 1),
    pad(date.getDate()),
  ].join('-') + `T${pad(date.getHours())}:${pad(date.getMinutes())}`;
}

export function defaultShareExpiration() {
  const date = new Date();
  date.setDate(date.getDate() + 7);
  date.setMinutes(0, 0, 0);
  return toLocalDateTimeInput(date);
}

export function getPublicShareUrl(token: string) {
  const baseUrl = import.meta.env.VITE_API_BASE_URL || '/api/v1';
  const apiUrl = baseUrl.startsWith('http')
    ? baseUrl.replace(/\/$/, '')
    : new URL(baseUrl, window.location.origin).toString().replace(/\/$/, '');

  return `${apiUrl}/public/shares/${token}`;
}

export function saveBlob(blob: Blob, filename: string) {
  const href = URL.createObjectURL(blob);
  const link = document.createElement('a');

  link.href = href;
  link.download = filename;
  link.rel = 'noopener';
  document.body.appendChild(link);
  link.click();
  link.remove();
  URL.revokeObjectURL(href);
}
