import { Box, Button, Container, Paper, Stack, Typography } from '@mui/material';
import { Link as RouterLink } from 'react-router-dom';

export function NotFoundPage() {
  return (
    <Box
      sx={{
        minHeight: '100vh',
        display: 'grid',
        placeItems: 'center',
        bgcolor: 'background.default',
        px: 2,
      }}
    >
      <Container maxWidth="sm">
        <Paper
          elevation={0}
          sx={{
            p: { xs: 3, sm: 4 },
            border: 1,
            borderColor: 'divider',
            textAlign: 'center',
          }}
        >
          <Stack spacing={2} alignItems="center">
            <Typography variant="h1">404</Typography>
            <Typography variant="h2">Страница не найдена</Typography>
            <Typography variant="body1" color="text.secondary">
              Адрес не совпадает с маршрутами HomeVault.
            </Typography>
            <Button component={RouterLink} to="/files" variant="contained">
              Открыть Files
            </Button>
          </Stack>
        </Paper>
      </Container>
    </Box>
  );
}
