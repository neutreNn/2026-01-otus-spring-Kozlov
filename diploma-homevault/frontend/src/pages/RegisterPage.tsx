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
import { Navigate, useNavigate } from 'react-router-dom';
import { z } from 'zod';
import { useAppDispatch, useAppSelector } from '../app/hooks';
import { AuthLayout } from '../components/AuthLayout';
import { useRegisterMutation } from '../features/auth/authApi';
import { selectIsAuthenticated, setCredentials } from '../features/auth/authSlice';
import { getApiErrorMessage } from '../shared/apiError';

const registerSchema = z.object({
  displayName: z
    .string()
    .trim()
    .min(1, 'Введите имя')
    .max(120, 'Имя слишком длинное'),
  email: z.string().trim().email('Введите корректный email').max(320, 'Email слишком длинный'),
  password: z.string().min(8, 'Минимум 8 символов').max(128, 'Пароль слишком длинный'),
});

type RegisterFormValues = z.infer<typeof registerSchema>;

export function RegisterPage() {
  const dispatch = useAppDispatch();
  const navigate = useNavigate();
  const isAuthenticated = useAppSelector(selectIsAuthenticated);
  const [registerUser, { isLoading, error }] = useRegisterMutation();
  const {
    formState: { errors, isSubmitting },
    handleSubmit,
    register,
  } = useForm<RegisterFormValues>({
    resolver: zodResolver(registerSchema),
    defaultValues: {
      displayName: '',
      email: '',
      password: '',
    },
  });

  if (isAuthenticated) {
    return <Navigate to="/files" replace />;
  }

  const onSubmit = handleSubmit(async (values) => {
    try {
      const response = await registerUser(values).unwrap();
      dispatch(setCredentials(response));
      navigate('/files', { replace: true });
    } catch {
      // RTK Query exposes the error state below the form.
    }
  });

  return (
    <AuthLayout
      title="Создание аккаунта"
      subtitle="Новый пользователь получает личное пространство для файлов и заметок."
      footerText="Уже есть аккаунт?"
      footerLinkLabel="Войти"
      footerLinkTo="/login"
    >
      <Box component="form" onSubmit={onSubmit} noValidate>
        <Stack spacing={2.5}>
          <Box>
            <Typography variant="h2" sx={{ mb: 0.75 }}>
              Register
            </Typography>
            <Typography variant="body2" color="text.secondary">
              После регистрации откроется раздел Files.
            </Typography>
          </Box>

          {error && (
            <Alert severity="error">{getApiErrorMessage(error, 'Не удалось зарегистрироваться')}</Alert>
          )}

          <TextField
            label="Имя"
            autoComplete="name"
            fullWidth
            error={Boolean(errors.displayName)}
            helperText={errors.displayName?.message}
            {...register('displayName')}
          />

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
            autoComplete="new-password"
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
            Зарегистрироваться
          </Button>
        </Stack>
      </Box>
    </AuthLayout>
  );
}
