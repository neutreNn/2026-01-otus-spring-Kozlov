import type { ReactNode } from 'react';
import { Box, Button, Stack, Typography } from '@mui/material';

interface EmptyStateProps {
  title: string;
  description: string;
  actionLabel?: string;
  actionIcon?: ReactNode;
  onAction?: () => void;
}

export function EmptyState({
  title,
  description,
  actionLabel,
  actionIcon,
  onAction,
}: EmptyStateProps) {
  return (
    <Box
      sx={{
        minHeight: 260,
        display: 'grid',
        placeItems: 'center',
        px: 3,
        py: 5,
        textAlign: 'center',
      }}
    >
      <Stack spacing={1.5} alignItems="center" sx={{ maxWidth: 430 }}>
        <Typography variant="h3" sx={{ fontSize: 20 }}>
          {title}
        </Typography>
        <Typography variant="body2" color="text.secondary">
          {description}
        </Typography>
        {actionLabel && onAction && (
          <Button variant="contained" startIcon={actionIcon} onClick={onAction}>
            {actionLabel}
          </Button>
        )}
      </Stack>
    </Box>
  );
}
