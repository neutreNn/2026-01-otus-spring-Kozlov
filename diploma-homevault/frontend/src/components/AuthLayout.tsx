import { PropsWithChildren } from 'react';
import { Box, Container, Link, Paper, Stack, Typography } from '@mui/material';
import { Link as RouterLink } from 'react-router-dom';

interface AuthLayoutProps extends PropsWithChildren {
  title: string;
  subtitle: string;
  footerText: string;
  footerLinkLabel: string;
  footerLinkTo: string;
}

export function AuthLayout({
  title,
  subtitle,
  footerText,
  footerLinkLabel,
  footerLinkTo,
  children,
}: AuthLayoutProps) {
  return (
    <Box
      sx={{
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        py: { xs: 4, md: 8 },
        background:
          'linear-gradient(135deg, rgba(23, 126, 137, 0.08), rgba(64, 86, 161, 0.08) 48%, rgba(246, 248, 251, 1) 100%)',
      }}
    >
      <Container maxWidth="lg">
        <Box
          sx={{
            display: 'grid',
            gridTemplateColumns: { xs: '1fr', md: '0.92fr 1fr' },
            gap: { xs: 3, md: 5 },
            alignItems: 'center',
          }}
        >
          <Stack spacing={3} sx={{ px: { xs: 0, md: 2 } }}>
            <Stack direction="row" alignItems="center" spacing={1.5}>
              <Box
                aria-hidden="true"
                sx={{
                  width: 40,
                  height: 40,
                  borderRadius: 2,
                  display: 'grid',
                  placeItems: 'center',
                  color: 'primary.contrastText',
                  bgcolor: 'primary.main',
                  fontWeight: 800,
                }}
              >
                HV
              </Box>
              <Typography variant="h2" component="p">
                HomeVault
              </Typography>
            </Stack>

            <Box>
              <Typography variant="h1" sx={{ mb: 1.5 }}>
                {title}
              </Typography>
              <Typography variant="body1" color="text.secondary" sx={{ maxWidth: 470 }}>
                {subtitle}
              </Typography>
            </Box>
          </Stack>

          <Paper
            elevation={0}
            sx={{
              p: { xs: 3, sm: 4 },
              border: 1,
              borderColor: 'divider',
              boxShadow: '0 16px 48px rgba(23, 32, 42, 0.08)',
            }}
          >
            {children}
            <Typography variant="body2" color="text.secondary" sx={{ mt: 3 }}>
              {footerText}{' '}
              <Link component={RouterLink} to={footerLinkTo} fontWeight={700}>
                {footerLinkLabel}
              </Link>
            </Typography>
          </Paper>
        </Box>
      </Container>
    </Box>
  );
}
