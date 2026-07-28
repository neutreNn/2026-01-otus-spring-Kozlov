import {
  Chip,
  LinearProgress,
  Paper,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Typography,
} from '@mui/material';
import { ApiErrorAlert } from '../components/ApiErrorAlert';
import { EmptyState } from '../components/EmptyState';
import { PageHeader } from '../components/PageHeader';
import { AuditIcon } from '../components/icons';
import { useListAuditEventsQuery } from '../features/audit/auditApi';
import { formatDateTime } from '../shared/formatters';

function stringifyDetails(details: Record<string, unknown> | null) {
  if (!details || Object.keys(details).length === 0) {
    return '—';
  }

  return Object.entries(details)
    .map(([key, value]) => `${key}: ${String(value)}`)
    .join(', ');
}

export function AuditPage() {
  const auditQuery = useListAuditEventsQuery({ size: 50 });
  const events = auditQuery.data?.content ?? [];

  return (
    <Stack spacing={3}>
      <PageHeader title="Audit" description="История действий текущего пользователя." />
      <ApiErrorAlert error={auditQuery.error} fallback="Не удалось загрузить аудит" />

      <TableContainer
        component={Paper}
        elevation={0}
        sx={{ border: 1, borderColor: 'divider', width: '100%', maxWidth: '100%', overflowX: 'auto' }}
      >
        {auditQuery.isFetching && <LinearProgress />}
        <Table size="small" sx={{ minWidth: 860 }}>
          <TableHead>
            <TableRow>
              <TableCell>Действие</TableCell>
              <TableCell>Entity</TableCell>
              <TableCell>Дата</TableCell>
              <TableCell>IP</TableCell>
              <TableCell>Детали</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {events.map((event) => (
              <TableRow key={event.id} hover>
                <TableCell>
                  <Chip size="small" color="primary" label={event.action} />
                </TableCell>
                <TableCell>
                  <Stack spacing={0.25}>
                    <Typography variant="body2">{event.entityType}</Typography>
                    <Typography variant="caption" color="text.secondary" noWrap>
                      {event.entityId ?? '—'}
                    </Typography>
                  </Stack>
                </TableCell>
                <TableCell>{formatDateTime(event.createdAt)}</TableCell>
                <TableCell>{event.ipAddress ?? '—'}</TableCell>
                <TableCell>
                  <Typography variant="caption" color="text.secondary">
                    {stringifyDetails(event.details)}
                  </Typography>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>

        {!auditQuery.isLoading && events.length === 0 && (
          <EmptyState
            title="Событий пока нет"
            description="Аудит появится после входа, загрузки файлов, удаления или создания публичных ссылок."
            actionIcon={<AuditIcon />}
          />
        )}
      </TableContainer>
    </Stack>
  );
}
