import { useEffect } from 'react';
import {
  Alert,
  Box,
  Chip,
  LinearProgress,
  Paper,
  Stack,
  Typography,
} from '@mui/material';
import { useAppDispatch, useAppSelector } from '../app/hooks';
import { useMeQuery } from '../features/auth/authApi';
import {
  selectAccessToken,
  selectCurrentUser,
  setCurrentUser,
} from '../features/auth/authSlice';
import { getApiErrorMessage } from '../shared/apiError';

export function ProfilePage() {
  const dispatch = useAppDispatch();
  const accessToken = useAppSelector(selectAccessToken);
  const storedUser = useAppSelector(selectCurrentUser);
  const { data, error, isFetching } = useMeQuery(undefined, {
    skip: !accessToken,
  });
  const user = data ?? storedUser;

  useEffect(() => {
    if (data) {
      dispatch(setCurrentUser(data));
    }
  }, [data, dispatch]);

  return (
    <Stack spacing={3}>
      <Box>
        <Typography variant="h1">Profile</Typography>
        <Typography variant="body1" color="text.secondary" sx={{ mt: 1 }}>
          Аккаунт и роли текущего пользователя.
        </Typography>
      </Box>

      {isFetching && <LinearProgress aria-label="Загрузка профиля" />}
      {error && <Alert severity="error">{getApiErrorMessage(error, 'Не удалось загрузить профиль')}</Alert>}

      <Paper
        elevation={0}
        sx={{
          p: { xs: 2.5, sm: 3 },
          border: 1,
          borderColor: 'divider',
        }}
      >
        <Stack spacing={2.5}>
          <Box>
            <Typography variant="body2" color="text.secondary">
              Имя
            </Typography>
            <Typography variant="h2" sx={{ fontSize: 22 }}>
              {user?.displayName ?? 'User'}
            </Typography>
          </Box>

          <Box
            sx={{
              display: 'grid',
              gap: 2,
              gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr' },
            }}
          >
            <Box>
              <Typography variant="body2" color="text.secondary">
                Email
              </Typography>
              <Typography variant="body1" fontWeight={700} sx={{ wordBreak: 'break-word' }}>
                {user?.email ?? '-'}
              </Typography>
            </Box>
            <Box>
              <Typography variant="body2" color="text.secondary">
                Статус
              </Typography>
              <Chip
                size="small"
                color={user?.status === 'ACTIVE' ? 'success' : 'warning'}
                label={user?.status ?? 'UNKNOWN'}
                sx={{ mt: 0.5, fontWeight: 700 }}
              />
            </Box>
          </Box>

          <Stack direction="row" flexWrap="wrap" gap={1}>
            {(user?.roles ?? []).map((role) => (
              <Chip key={role} label={role} size="small" variant="outlined" />
            ))}
          </Stack>
        </Stack>
      </Paper>
    </Stack>
  );
}
