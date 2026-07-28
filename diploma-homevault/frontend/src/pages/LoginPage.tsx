import { zodResolver } from '@hookform/resolvers/zod';
import {
  Alert,
  Box,
  Button,
  CircularProgress,
  Stack,
  TextField,
  Typography,
} from '@mui/material';
import { useForm } from 'react-hook-form';
import { Navigate, useLocation, useNavigate } from 'react-router-dom';
import { z } from 'zod';
import { useAppDispatch, useAppSelector } from '../app/hooks';
import { AuthLayout } from '../components/AuthLayout';
import { useLoginMutation } from '../features/auth/authApi';
import { selectIsAuthenticated, setCredentials } from '../features/auth/authSlice';
import { getApiErrorMessage } from '../shared/apiError';

const loginSchema = z.object({
  email: z.string().trim().email('Введите корректный email').max(320, 'Email слишком длинный'),
  password: z.string().min(8, 'Минимум 8 символов').max(128, 'Пароль слишком длинный'),
});

type LoginFormValues = z.infer<typeof loginSchema>;

interface LocationState {
  from?: {
    pathname?: string;
  };
}

export function LoginPage() {
  const dispatch = useAppDispatch();
  const navigate = useNavigate();
  const location = useLocation();
  const isAuthenticated = useAppSelector(selectIsAuthenticated);
  const [login, { isLoading, error }] = useLoginMutation();
  const {
    formState: { errors, isSubmitting },
    handleSubmit,
    register,
  } = useForm<LoginFormValues>({
    resolver: zodResolver(loginSchema),
    defaultValues: {
      email: '',
      password: '',
    },
  });

  if (isAuthenticated) {
    return <Navigate to="/files" replace />;
  }

  const from = (location.state as LocationState | null)?.from?.pathname ?? '/files';

  const onSubmit = handleSubmit(async (values) => {
    try {
      const response = await login(values).unwrap();
      dispatch(setCredentials(response));
      navigate(from, { replace: true });
    } catch {
      // RTK Query exposes the error state below the form.
    }
  });

  return (
    <AuthLayout
      title="Вход в хранилище"
      subtitle="Доступ к файлам, заметкам и публичным ссылкам в одном приватном рабочем пространстве."
      footerText="Нет аккаунта?"
      footerLinkLabel="Зарегистрироваться"
      footerLinkTo="/register"
    >
      <Box component="form" onSubmit={onSubmit} noValidate>
        <Stack spacing={2.5}>
          <Box>
            <Typography variant="h2" sx={{ mb: 0.75 }}>
              Login
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Используйте email и пароль от HomeVault.
            </Typography>
          </Box>

          {error && <Alert severity="error">{getApiErrorMessage(error, 'Не удалось войти')}</Alert>}

          <TextField
            label="Email"
            autoComplete="email"
            type="email"
            fullWidth
            error={Boolean(errors.email)}
            helperText={errors.email?.message}
            {...register('email')}
          />

          <TextField
            label="Пароль"
            autoComplete="current-password"
            type="password"
            fullWidth
            error={Boolean(errors.password)}
            helperText={errors.password?.message}
            {...register('password')}
          />

          <Button
            type="submit"
            variant="contained"
            size="large"
            disabled={isLoading || isSubmitting}
            startIcon={isLoading ? <CircularProgress color="inherit" size={18} /> : null}
          >
            Войти
          </Button>
        </Stack>
      </Box>
    </AuthLayout>
  );
}
