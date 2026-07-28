import { Alert } from '@mui/material';
import { getApiErrorMessage } from '../shared/apiError';

interface ApiErrorAlertProps {
  error: unknown;
  fallback?: string;
}

export function ApiErrorAlert({ error, fallback }: ApiErrorAlertProps) {
  if (!error) {
    return null;
  }

  return <Alert severity="error">{getApiErrorMessage(error, fallback)}</Alert>;
}
