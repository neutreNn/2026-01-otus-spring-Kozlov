import { useDeferredValue, useEffect, useMemo, useState } from 'react';
import { zodResolver } from '@hookform/resolvers/zod';
import {
  Alert,
  Box,
  Button,
  Chip,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  IconButton,
  LinearProgress,
  MenuItem,
  Paper,
  Stack,
  TextField,
  Tooltip,
  Typography,
} from '@mui/material';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { ApiErrorAlert } from '../components/ApiErrorAlert';
import { ConfirmDialog } from '../components/ConfirmDialog';
import { EmptyState } from '../components/EmptyState';
import { PageHeader } from '../components/PageHeader';
import { ShareDialog } from '../components/ShareDialog';
import { AddIcon, DeleteIcon, EditIcon, LinkIcon, NotesIcon } from '../components/icons';
import {
  useCreateNoteMutation,
  useDeleteNoteMutation,
  useListNotesQuery,
  useUpdateNoteMutation,
} from '../features/notes/notesApi';
import type { NoteFormPayload, NoteResponse } from '../features/notes/types';
import { formatDateTime } from '../shared/formatters';

const noteSchema = z.object({
  title: z.string().trim().min(1, 'Введите заголовок').max(255, 'Максимум 255 символов'),
  content: z.string().trim().min(1, 'Введите текст заметки'),
  tags: z.string().refine(
    (value) => parseTags(value).length <= 50 && parseTags(value).every((tag) => tag.length <= 80),
    { message: 'До 50 тегов, каждый не длиннее 80 символов' },
  ),
});

interface NoteFormValues {
  title: string;
  content: string;
  tags: string;
}

interface NoteDialogProps {
  open: boolean;
  note: NoteResponse | null;
  loading?: boolean;
  onClose: () => void;
  onSubmit: (payload: NoteFormPayload) => void;
}

function parseTags(value: string) {
  return Array.from(
    new Set(
      value
        .split(',')
        .map((tag) => tag.trim().toLowerCase())
        .filter(Boolean),
    ),
  );
}

function NoteDialog({ open, note, loading = false, onClose, onSubmit }: NoteDialogProps) {
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<NoteFormValues>({
    resolver: zodResolver(noteSchema),
    defaultValues: {
      title: '',
      content: '',
      tags: '',
    },
  });

  useEffect(() => {
    if (open) {
      reset({
        title: note?.title ?? '',
        content: note?.content ?? '',
        tags: note?.tags.join(', ') ?? '',
      });
    }
  }, [note, open, reset]);

  const submit = handleSubmit((values) =>
    onSubmit({
      title: values.title.trim(),
      content: values.content.trim(),
      tags: parseTags(values.tags),
    }),
  );

  return (
    <Dialog open={open} onClose={loading ? undefined : onClose} fullWidth maxWidth="md">
      <DialogTitle>{note ? 'Редактировать заметку' : 'Новая заметка'}</DialogTitle>
      <DialogContent>
        <Stack component="form" id="note-form" spacing={2} sx={{ pt: 1 }} onSubmit={submit}>
          <TextField
            label="Заголовок"
            autoFocus
            error={Boolean(errors.title)}
            helperText={errors.title?.message}
            {...register('title')}
          />
          <TextField
            label="Текст"
            multiline
            minRows={7}
            error={Boolean(errors.content)}
            helperText={errors.content?.message}
            {...register('content')}
          />
          <TextField
            label="Теги"
            placeholder="work, devops, personal"
            error={Boolean(errors.tags)}
            helperText={errors.tags?.message ?? 'Через запятую'}
            {...register('tags')}
          />
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} disabled={loading}>
          Отмена
        </Button>
        <Button type="submit" form="note-form" variant="contained" disabled={loading}>
          {loading ? 'Сохраняется...' : 'Сохранить'}
        </Button>
      </DialogActions>
    </Dialog>
  );
}

export function NotesPage() {
  const [query, setQuery] = useState('');
  const [tag, setTag] = useState('');
  const deferredQuery = useDeferredValue(query);
  const deferredTag = useDeferredValue(tag);
  const [editingNote, setEditingNote] = useState<NoteResponse | null>(null);
  const [noteDialogOpen, setNoteDialogOpen] = useState(false);
  const [deleteTarget, setDeleteTarget] = useState<NoteResponse | null>(null);
  const [shareTarget, setShareTarget] = useState<NoteResponse | null>(null);
  const [actionError, setActionError] = useState<unknown>(null);
  const [notice, setNotice] = useState<string | null>(null);

  const notesQuery = useListNotesQuery({
    query: deferredQuery,
    tag: deferredTag,
    size: 30,
  });
  const [createNote, createNoteState] = useCreateNoteMutation();
  const [updateNote, updateNoteState] = useUpdateNoteMutation();
  const [deleteNote, deleteNoteState] = useDeleteNoteMutation();

  const notes = notesQuery.data?.content ?? [];
  const availableTags = useMemo(
    () => Array.from(new Set(notes.flatMap((note) => note.tags))).sort((left, right) => left.localeCompare(right)),
    [notes],
  );

  const resetFeedback = () => {
    setActionError(null);
    setNotice(null);
  };

  const openCreateDialog = () => {
    resetFeedback();
    setEditingNote(null);
    setNoteDialogOpen(true);
  };

  const openEditDialog = (note: NoteResponse) => {
    resetFeedback();
    setEditingNote(note);
    setNoteDialogOpen(true);
  };

  const handleSaveNote = async (payload: NoteFormPayload) => {
    resetFeedback();
    try {
      if (editingNote) {
        await updateNote({ noteId: editingNote.id, ...payload }).unwrap();
        setNotice('Заметка обновлена');
      } else {
        await createNote(payload).unwrap();
        setNotice('Заметка создана');
      }
      setNoteDialogOpen(false);
      setEditingNote(null);
    } catch (error) {
      setActionError(error);
    }
  };

  const handleDeleteNote = async () => {
    if (!deleteTarget) {
      return;
    }

    resetFeedback();
    try {
      await deleteNote(deleteTarget.id).unwrap();
      setDeleteTarget(null);
      setNotice('Заметка удалена');
    } catch (error) {
      setActionError(error);
    }
  };

  return (
    <Stack spacing={3}>
      <PageHeader
        title="Notes"
        description="Личные заметки с поиском по заголовку, содержимому и тегам."
        actions={
          <Button variant="contained" startIcon={<AddIcon />} onClick={openCreateDialog}>
            Создать заметку
          </Button>
        }
      />

      <Paper elevation={0} sx={{ border: 1, borderColor: 'divider', p: 2 }}>
        <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1.5}>
          <TextField
            label="Поиск"
            value={query}
            onChange={(event) => setQuery(event.target.value)}
            placeholder="Название или текст"
            sx={{ flexGrow: 1 }}
          />
          <TextField
            select
            label="Тег"
            value={tag}
            onChange={(event) => setTag(event.target.value)}
            sx={{ minWidth: { xs: '100%', sm: 220 } }}
          >
            <MenuItem value="">Все теги</MenuItem>
            {availableTags.map((item) => (
              <MenuItem key={item} value={item}>
                {item}
              </MenuItem>
            ))}
          </TextField>
        </Stack>
      </Paper>

      {notice && <Alert severity="success">{notice}</Alert>}
      <ApiErrorAlert error={actionError || notesQuery.error} fallback="Не удалось загрузить заметки" />

      {notesQuery.isFetching && <LinearProgress />}

      {!notesQuery.isLoading && notes.length === 0 ? (
        <Paper elevation={0} sx={{ border: 1, borderColor: 'divider' }}>
          <EmptyState
            title="Заметок пока нет"
            description="Создайте первую заметку и добавьте теги для быстрого поиска."
            actionLabel="Создать заметку"
            actionIcon={<AddIcon />}
            onAction={openCreateDialog}
          />
        </Paper>
      ) : (
        <Box
          sx={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))',
            gap: 2,
          }}
        >
          {notes.map((note) => (
            <Paper
              key={note.id}
              elevation={0}
              sx={{
                border: 1,
                borderColor: 'divider',
                p: 2,
                display: 'flex',
                flexDirection: 'column',
                gap: 1.5,
                minHeight: 220,
              }}
            >
              <Stack direction="row" spacing={1.5} alignItems="flex-start">
                <NotesIcon color="primary" />
                <Box sx={{ minWidth: 0, flexGrow: 1 }}>
                  <Typography variant="h3" sx={{ fontSize: 18 }} noWrap>
                    {note.title}
                  </Typography>
                  <Typography variant="caption" color="text.secondary">
                    Обновлена {formatDateTime(note.updatedAt)}
                  </Typography>
                </Box>
              </Stack>

              <Typography
                variant="body2"
                color="text.secondary"
                sx={{
                  whiteSpace: 'pre-wrap',
                  overflow: 'hidden',
                  display: '-webkit-box',
                  WebkitLineClamp: 4,
                  WebkitBoxOrient: 'vertical',
                }}
              >
                {note.content}
              </Typography>

              <Stack direction="row" flexWrap="wrap" gap={0.75} sx={{ mt: 'auto' }}>
                {note.tags.length > 0 ? (
                  note.tags.map((item) => (
                    <Chip key={item} label={item} size="small" onClick={() => setTag(item)} />
                  ))
                ) : (
                  <Typography variant="caption" color="text.secondary">
                    Без тегов
                  </Typography>
                )}
              </Stack>

              <Stack direction="row" justifyContent="flex-end" spacing={0.5}>
                <Tooltip title="Публичная ссылка">
                  <IconButton
                    size="small"
                    aria-label={`Создать публичную ссылку для заметки ${note.title}`}
                    onClick={() => setShareTarget(note)}
                  >
                    <LinkIcon fontSize="small" />
                  </IconButton>
                </Tooltip>
                <Tooltip title="Редактировать">
                  <IconButton
                    size="small"
                    aria-label={`Редактировать заметку ${note.title}`}
                    onClick={() => openEditDialog(note)}
                  >
                    <EditIcon fontSize="small" />
                  </IconButton>
                </Tooltip>
                <Tooltip title="Удалить">
                  <IconButton
                    size="small"
                    color="error"
                    aria-label={`Удалить заметку ${note.title}`}
                    onClick={() => setDeleteTarget(note)}
                  >
                    <DeleteIcon fontSize="small" />
                  </IconButton>
                </Tooltip>
              </Stack>
            </Paper>
          ))}
        </Box>
      )}

      <NoteDialog
        open={noteDialogOpen}
        note={editingNote}
        loading={createNoteState.isLoading || updateNoteState.isLoading}
        onClose={() => {
          setNoteDialogOpen(false);
          setEditingNote(null);
        }}
        onSubmit={(payload) => void handleSaveNote(payload)}
      />

      <ConfirmDialog
        open={Boolean(deleteTarget)}
        title="Удалить заметку?"
        description={`"${deleteTarget?.title ?? ''}" будет удалена вместе со связанными тегами.`}
        confirmLabel="Удалить"
        confirmColor="error"
        loading={deleteNoteState.isLoading}
        onClose={() => setDeleteTarget(null)}
        onConfirm={() => void handleDeleteNote()}
      />

      <ShareDialog
        open={Boolean(shareTarget)}
        resourceType="NOTE"
        resourceId={shareTarget?.id ?? null}
        resourceName={shareTarget?.title ?? ''}
        onClose={() => setShareTarget(null)}
      />
    </Stack>
  );
}
