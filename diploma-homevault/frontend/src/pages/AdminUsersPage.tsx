import { useState } from 'react';
import {
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
  TablePagination,
  TableRow,
  Tooltip,
  Typography,
} from '@mui/material';
import { ApiErrorAlert } from '../components/ApiErrorAlert';
import { ConfirmDialog } from '../components/ConfirmDialog';
import { EmptyState } from '../components/EmptyState';
import { PageHeader } from '../components/PageHeader';
import { UsersIcon } from '../components/icons';
import {
  useListAdminUsersQuery,
  useUpdateUserStatusMutation,
} from '../features/admin/adminApi';
import type { UserResponse } from '../features/auth/types';
import { formatBytes, formatDateTime } from '../shared/formatters';

export function AdminUsersPage() {
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(20);
  const usersQuery = useListAdminUsersQuery({ page, size: rowsPerPage });
  const [updateUserStatus, updateUserStatusState] = useUpdateUserStatusMutation();
  const [target, setTarget] = useState<UserResponse | null>(null);
  const [actionError, setActionError] = useState<unknown>(null);
  const users = usersQuery.data?.content ?? [];

  const nextStatus = target?.status === 'BLOCKED' ? 'ACTIVE' : 'BLOCKED';

  const handleUpdateStatus = async () => {
    if (!target) {
      return;
    }

    setActionError(null);
    try {
      await updateUserStatus({ userId: target.id, status: nextStatus }).unwrap();
      setTarget(null);
    } catch (error) {
      setActionError(error);
    }
  };

  return (
    <Stack spacing={3}>
      <PageHeader title="Admin users" description="Управление пользователями и блокировкой доступа." />
      <ApiErrorAlert error={actionError || usersQuery.error} fallback="Не удалось загрузить пользователей" />

      <TableContainer
        component={Paper}
        elevation={0}
        sx={{ border: 1, borderColor: 'divider', width: '100%', maxWidth: '100%', overflowX: 'auto' }}
      >
        {usersQuery.isFetching && <LinearProgress />}
        <Table size="small" sx={{ minWidth: 920 }}>
          <TableHead>
            <TableRow>
              <TableCell>Пользователь</TableCell>
              <TableCell>Статус</TableCell>
              <TableCell>Роли</TableCell>
              <TableCell>Лимит</TableCell>
              <TableCell>Создан</TableCell>
              <TableCell align="right">Действия</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {users.map((user) => (
              <TableRow key={user.id} hover>
                <TableCell>
                  <Stack spacing={0.25}>
                    <Typography variant="body2" fontWeight={700}>
                      {user.displayName}
                    </Typography>
                    <Typography variant="caption" color="text.secondary">
                      {user.email}
                    </Typography>
                  </Stack>
                </TableCell>
                <TableCell>
                  <Chip
                    size="small"
                    color={user.status === 'ACTIVE' ? 'success' : 'error'}
                    label={user.status === 'ACTIVE' ? 'Активен' : 'Заблокирован'}
                  />
                </TableCell>
                <TableCell>{user.roles.join(', ')}</TableCell>
                <TableCell>{formatBytes(user.storageLimitBytes)}</TableCell>
                <TableCell>{formatDateTime(user.createdAt)}</TableCell>
                <TableCell align="right">
                  <Tooltip title={user.status === 'BLOCKED' ? 'Разблокировать' : 'Заблокировать'}>
                    <IconButton
                      size="small"
                      color={user.status === 'BLOCKED' ? 'primary' : 'error'}
                      aria-label={
                        user.status === 'BLOCKED'
                          ? `Разблокировать пользователя ${user.email}`
                          : `Заблокировать пользователя ${user.email}`
                      }
                      onClick={() => setTarget(user)}
                    >
                      <UsersIcon fontSize="small" />
                    </IconButton>
                  </Tooltip>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>

        {!usersQuery.isLoading && users.length === 0 && (
          <EmptyState
            title="Пользователей пока нет"
            description="После регистрации аккаунты появятся в этой таблице."
            actionIcon={<UsersIcon />}
          />
        )}

        <TablePagination
          component="div"
          count={usersQuery.data?.totalElements ?? 0}
          page={page}
          rowsPerPage={rowsPerPage}
          rowsPerPageOptions={[10, 20, 50]}
          labelRowsPerPage="Строк на странице"
          onPageChange={(_event, nextPage) => setPage(nextPage)}
          onRowsPerPageChange={(event) => {
            setRowsPerPage(Number(event.target.value));
            setPage(0);
          }}
        />
      </TableContainer>

      <ConfirmDialog
        open={Boolean(target)}
        title={nextStatus === 'BLOCKED' ? 'Заблокировать пользователя?' : 'Разблокировать пользователя?'}
        description={`${target?.email ?? ''}: статус будет изменен на ${nextStatus}.`}
        confirmLabel={nextStatus === 'BLOCKED' ? 'Заблокировать' : 'Разблокировать'}
        confirmColor={nextStatus === 'BLOCKED' ? 'error' : 'primary'}
        loading={updateUserStatusState.isLoading}
        onClose={() => setTarget(null)}
        onConfirm={() => void handleUpdateStatus()}
      />
    </Stack>
  );
}
