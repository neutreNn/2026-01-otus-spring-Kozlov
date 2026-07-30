import {
  Box,
  Button,
  Divider,
  Paper,
  Stack,
  TextField,
  Typography,
} from '@mui/material';
import { FolderAddIcon, UploadIcon } from '../components/icons';

interface WorkspacePlaceholderPageProps {
  title: string;
  description: string;
  primaryAction?: string;
  secondaryAction?: string;
  columns: string[];
  emptyText: string;
}

export function WorkspacePlaceholderPage({
  title,
  description,
  primaryAction,
  secondaryAction,
  columns,
  emptyText,
}: WorkspacePlaceholderPageProps) {
  return (
    <Stack spacing={3}>
      <Box
        sx={{
          display: 'flex',
          alignItems: { xs: 'stretch', sm: 'center' },
          justifyContent: 'space-between',
          flexDirection: { xs: 'column', sm: 'row' },
          gap: 2,
        }}
      >
        <Box>
          <Typography variant="h1">{title}</Typography>
          <Typography variant="body1" color="text.secondary" sx={{ mt: 1 }}>
            {description}
          </Typography>
        </Box>

        {(primaryAction || secondaryAction) && (
          <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1.25}>
            {secondaryAction && (
              <Button variant="outlined" startIcon={<FolderAddIcon />}>
                {secondaryAction}
              </Button>
            )}
            {primaryAction && (
              <Button variant="contained" startIcon={<UploadIcon />}>
                {primaryAction}
              </Button>
            )}
          </Stack>
        )}
      </Box>

      <Paper
        elevation={0}
        sx={{
          border: 1,
          borderColor: 'divider',
          overflow: 'hidden',
        }}
      >
        <Box
          sx={{
            p: 2,
            display: 'flex',
            gap: 1.5,
            alignItems: { xs: 'stretch', sm: 'center' },
            flexDirection: { xs: 'column', sm: 'row' },
            justifyContent: 'space-between',
          }}
        >
          <TextField
            label="Поиск"
            placeholder="Название или тег"
            sx={{ width: { xs: '100%', sm: 320 } }}
          />
        </Box>

        <Divider />

        <Box
          sx={{
            display: 'grid',
            gridTemplateColumns: `minmax(160px, 1fr) repeat(${Math.max(columns.length - 1, 0)}, minmax(96px, 160px))`,
            gap: 2,
            px: 2,
            py: 1.5,
            color: 'text.secondary',
            fontSize: 13,
            fontWeight: 700,
            overflowX: 'auto',
          }}
        >
          {columns.map((column) => (
            <Box key={column}>{column}</Box>
          ))}
        </Box>

        <Divider />

        <Box
          sx={{
            minHeight: 240,
            display: 'grid',
            placeItems: 'center',
            p: 3,
            textAlign: 'center',
          }}
        >
          <Stack spacing={1} alignItems="center" sx={{ maxWidth: 420 }}>
            <Typography variant="h3" sx={{ fontSize: 18 }}>
              Нет данных
            </Typography>
            <Typography variant="body2" color="text.secondary">
              {emptyText}
            </Typography>
          </Stack>
        </Box>
      </Paper>
    </Stack>
  );
}
