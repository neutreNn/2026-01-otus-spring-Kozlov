import type { FetchBaseQueryError } from '@reduxjs/toolkit/query';
import type { SerializedError } from '@reduxjs/toolkit';

interface ApiErrorPayload {
  message?: string;
  error?: string;
}

function isFetchBaseQueryError(error: unknown): error is FetchBaseQueryError {
  return typeof error === 'object' && error !== null && 'status' in error;
}

function isSerializedError(error: unknown): error is SerializedError {
  return typeof error === 'object' && error !== null && 'message' in error;
}

export function getApiErrorMessage(error: unknown, fallback = 'Не удалось выполнить запрос') {
  if (isFetchBaseQueryError(error)) {
    if (typeof error.data === 'object' && error.data !== null) {
      const payload = error.data as ApiErrorPayload;
      return payload.message || payload.error || fallback;
    }

    if (typeof error.data === 'string') {
      return error.data;
    }
  }

  if (isSerializedError(error) && error.message) {
    return error.message;
  }

  return fallback;
}
