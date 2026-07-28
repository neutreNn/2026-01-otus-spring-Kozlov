import { useEffect, useRef } from 'react';
import { zodResolver } from '@hookform/resolvers/zod';
import {
  Alert,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Stack,
  TextField,
} from '@mui/material';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { useCreateShareMutation } from '../features/shares/sharesApi';
import type { ShareResourceType } from '../shared/apiTypes';
import { defaultShareExpiration, getPublicShareUrl } from '../shared/formatters';
import { getApiErrorMessage } from '../shared/apiError';
import { CopyIcon, LinkIcon } from './icons';

const shareSchema = z.object({
  expiresAt: z.string().min(1, 'Укажите дату окончания').refine(
    (value) => {
      const expiresAt = new Date(value);
      return !Number.isNaN(expiresAt.getTime()) && expiresAt.getTime() > Date.now();
    },
    { message: 'Дата должна быть в будущем' },
  ),
});

interface ShareFormValues {
  expiresAt: string;
}

interface ShareDialogProps {
  open: boolean;
  resourceType: ShareResourceType;
  resourceId: string | null;
  resourceName: string;
  onClose: () => void;
}

export function ShareDialog({
  open,
  resourceType,
  resourceId,
  resourceName,
  onClose,
}: ShareDialogProps) {
  const [createShare, { data, error, isLoading, reset: resetMutation }] = useCreateShareMutation();
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<ShareFormValues>({
    resolver: zodResolver(shareSchema),
    defaultValues: {
      expiresAt: defaultShareExpiration(),
    },
  });
  const wasOpenRef = useRef(false);

  useEffect(() => {
    if (open && !wasOpenRef.current) {
      reset({ expiresAt: defaultShareExpiration() });
      resetMutation();
    }
    wasOpenRef.current = open;
  }, [open, reset, resetMutation]);

  const publicUrl = data ? getPublicShareUrl(data.token) : '';

  const handleCopy = async () => {
    if (publicUrl) {
      await navigator.clipboard.writeText(publicUrl);
    }
  };

  const handleCreate = handleSubmit(async (values) => {
    if (!resourceId) {
      return;
    }

    await createShare({
      resourceType,
      resourceId,
      expiresAt: new Date(values.expiresAt).toISOString(),
    }).unwrap();
  });

  return (
    <Dialog open={open} onClose={isLoading ? undefined : onClose} fullWidth maxWidth="sm">
      <DialogTitle>Публичная ссылка</DialogTitle>
      <DialogContent>
        <Stack component="form" id="share-form" spacing={2.5} sx={{ pt: 1 }} onSubmit={handleCreate}>
          <TextField label="Ресурс" value={resourceName} InputProps={{ readOnly: true }} />
          <TextField
            label="Действует до"
            type="datetime-local"
            InputLabelProps={{ shrink: true }}
            error={Boolean(errors.expiresAt)}
            helperText={errors.expiresAt?.message}
            {...register('expiresAt')}
          />
          {error && <Alert severity="error">{getApiErrorMessage(error)}</Alert>}
          {data && (
            <Alert severity="success" icon={<LinkIcon />}>
              Ссылка создана. Ее можно открыть без авторизации.
            </Alert>
          )}
          {data && (
            <TextField
              label="URL"
              value={publicUrl}
              InputProps={{ readOnly: true }}
              multiline
              minRows={2}
            />
          )}
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} disabled={isLoading}>
          Закрыть
        </Button>
        {data && (
          <Button variant="outlined" startIcon={<CopyIcon />} onClick={handleCopy}>
            Скопировать
          </Button>
        )}
        <Button
          type="button"
          variant="contained"
          disabled={isLoading || !resourceId}
          onClick={() => void handleCreate()}
        >
          {isLoading ? 'Создается...' : 'Создать'}
        </Button>
      </DialogActions>
    </Dialog>
  );
}
