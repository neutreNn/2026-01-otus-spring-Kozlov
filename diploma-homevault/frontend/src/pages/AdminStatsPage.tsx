import { Card, CardContent, LinearProgress, Stack, Typography } from '@mui/material';
import { ApiErrorAlert } from '../components/ApiErrorAlert';
import { PageHeader } from '../components/PageHeader';
import { useAdminStatsQuery } from '../features/admin/adminApi';
import { formatBytes, formatNumber } from '../shared/formatters';

export function AdminStatsPage() {
  const statsQuery = useAdminStatsQuery();
  const stats = statsQuery.data;
  const cards = [
    { label: 'Всего пользователей', value: formatNumber(stats?.totalUsers) },
    { label: 'Активных пользователей', value: formatNumber(stats?.activeUsers) },
    { label: 'Заблокированных', value: formatNumber(stats?.blockedUsers) },
    { label: 'Папок', value: formatNumber(stats?.foldersCount) },
    { label: 'Файлов', value: formatNumber(stats?.filesCount) },
    { label: 'Объем хранилища', value: formatBytes(stats?.totalStorageBytes) },
    { label: 'Заметок', value: formatNumber(stats?.notesCount) },
    { label: 'Публичных ссылок', value: formatNumber(stats?.shareLinksCount) },
    { label: 'Активных ссылок', value: formatNumber(stats?.activeShareLinks) },
  ];

  return (
    <Stack spacing={3}>
      <PageHeader title="Admin stats" description="Сводка пользователей, файлов, заметок и публичных ссылок." />
      <ApiErrorAlert error={statsQuery.error} fallback="Не удалось загрузить статистику" />
      {statsQuery.isFetching && <LinearProgress />}

      <Stack
        sx={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))',
          gap: 2,
        }}
      >
        {cards.map((card) => (
          <Card key={card.label} elevation={0} sx={{ border: 1, borderColor: 'divider' }}>
            <CardContent>
              <Typography variant="body2" color="text.secondary">
                {card.label}
              </Typography>
              <Typography variant="h2" sx={{ mt: 1 }}>
                {card.value}
              </Typography>
            </CardContent>
          </Card>
        ))}
      </Stack>
    </Stack>
  );
}
