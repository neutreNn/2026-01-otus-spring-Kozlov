import { useState } from 'react';
import {
  Alert,
  Button,
  Chip,
  IconButton,
  LinearProgress,
  Paper,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Tooltip,
  Typography,
} from '@mui/material';
import { ApiErrorAlert } from '../components/ApiErrorAlert';
import { ConfirmDialog } from '../components/ConfirmDialog';
import { EmptyState } from '../components/EmptyState';
import { PageHeader } from '../components/PageHeader';
import { CopyIcon, DeleteIcon, LinkIcon } from '../components/icons';
import {
  type ShareResponse,
  useListSharesQuery,
  useRevokeShareMutation,
} from '../features/shares/sharesApi';
import { formatDateTime, getPublicShareUrl } from '../shared/formatters';

export function SharesPage() {
  const sharesQuery = useListSharesQuery({ size: 50 });
  const [revokeShare, revokeShareState] = useRevokeShareMutation();
  const [revokeTarget, setRevokeTarget] = useState<ShareResponse | null>(null);
  const [actionError, setActionError] = useState<unknown>(null);
  const [notice, setNotice] = useState<string | null>(null);
  const shares = sharesQuery.data?.content ?? [];

  const handleCopy = async (token: string) => {
    await navigator.clipboard.writeText(getPublicShareUrl(token));
    setNotice('URL скопирован');
    setActionError(null);
  };

  const handleRevoke = async () => {
    if (!revokeTarget) {
      return;
    }

    setActionError(null);
    setNotice(null);
    try {
      await revokeShare(revokeTarget.id).unwrap();
      setRevokeTarget(null);
      setNotice('Публичная ссылка отозвана');
    } catch (error) {
      setActionError(error);
    }
  };

  return (
    <Stack spacing={3}>
      <PageHeader
        title="Shares"
        description="Публичные ссылки на ваши файлы и заметки."
      />

      {notice && <Alert severity="success">{notice}</Alert>}
      <ApiErrorAlert error={actionError || sharesQuery.error} fallback="Не удалось загрузить ссылки" />

      <TableContainer
        component={Paper}
        elevation={0}
        sx={{ border: 1, borderColor: 'divider', width: '100%', maxWidth: '100%', overflowX: 'auto' }}
      >
        {sharesQuery.isFetching && <LinearProgress />}
        <Table size="small" sx={{ minWidth: 900 }}>
          <TableHead>
            <TableRow>
              <TableCell>Token / URL</TableCell>
              <TableCell>Ресурс</TableCell>
              <TableCell>Истекает</TableCell>
              <TableCell>Статус</TableCell>
              <TableCell>Открытия</TableCell>
              <TableCell align="right">Действия</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {shares.map((share) => {
              const revoked = Boolean(share.revokedAt);

              return (
                <TableRow key={share.id} hover>
                  <TableCell>
                    <Stack spacing={0.5} sx={{ maxWidth: 360 }}>
                      <Typography variant="body2" fontWeight={700} noWrap>
                        {share.token}
                      </Typography>
                      <Typography variant="caption" color="text.secondary" noWrap>
                        {getPublicShareUrl(share.token)}
                      </Typography>
                    </Stack>
                  </TableCell>
                  <TableCell>{share.resourceType}</TableCell>
                  <TableCell>{formatDateTime(share.expiresAt)}</TableCell>
                  <TableCell>
                    <Chip
                      size="small"
                      color={revoked ? 'default' : 'success'}
                      label={revoked ? `Отозвана ${formatDateTime(share.revokedAt)}` : 'Активна'}
                    />
                  </TableCell>
                  <TableCell>{share.accessCount}</TableCell>
                  <TableCell align="right">
                    <Tooltip title="Скопировать URL">
                      <IconButton
                        size="small"
                        aria-label={`Скопировать URL ${share.token}`}
                        onClick={() => void handleCopy(share.token)}
                      >
                        <CopyIcon fontSize="small" />
                      </IconButton>
                    </Tooltip>
                    <Tooltip title="Отозвать">
                      <span>
                        <IconButton
                          size="small"
                          color="error"
                          disabled={revoked}
                          aria-label={`Отозвать ссылку ${share.token}`}
                          onClick={() => setRevokeTarget(share)}
                        >
                          <DeleteIcon fontSize="small" />
                        </IconButton>
                      </span>
                    </Tooltip>
                  </TableCell>
                </TableRow>
              );
            })}
          </TableBody>
        </Table>

        {!sharesQuery.isLoading && shares.length === 0 && (
          <EmptyState
            title="Ссылок пока нет"
            description="Создайте публичную ссылку из таблицы файлов или карточки заметки."
            actionIcon={<LinkIcon />}
          />
        )}
      </TableContainer>

      <ConfirmDialog
        open={Boolean(revokeTarget)}
        title="Отозвать ссылку?"
        description="После отзыва публичный доступ по этому token будет закрыт."
        confirmLabel="Отозвать"
        confirmColor="error"
        loading={revokeShareState.isLoading}
        onClose={() => setRevokeTarget(null)}
        onConfirm={() => void handleRevoke()}
      />
    </Stack>
  );
}
