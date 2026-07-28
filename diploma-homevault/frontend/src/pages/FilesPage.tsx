import { useEffect, useRef, useState } from 'react';
import { zodResolver } from '@hookform/resolvers/zod';
import {
  Alert,
  Box,
  Breadcrumbs,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  IconButton,
  LinearProgress,
  Paper,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
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
import {
  DeleteIcon,
  DownloadIcon,
  EditIcon,
  FileIcon,
  FolderAddIcon,
  FolderOpenIcon,
  LinkIcon,
  UploadIcon,
} from '../components/icons';
import {
  useCreateFolderMutation,
  useDeleteFileMutation,
  useDeleteFolderMutation,
  useDownloadFileMutation,
  useListFilesQuery,
  useListFoldersQuery,
  useUpdateFileMutation,
  useUpdateFolderMutation,
  useUploadFileMutation,
} from '../features/storage/storageApi';
import type { FileResponse, FolderCrumb, FolderResponse } from '../features/storage/types';
import { formatBytes, formatDateTime, saveBlob } from '../shared/formatters';

const rootCrumb: FolderCrumb = { id: null, name: 'Корень' };

const nameSchema = z.object({
  name: z.string().trim().min(1, 'Введите название').max(255, 'Максимум 255 символов'),
});

interface NameFormValues {
  name: string;
}

interface NameDialogProps {
  open: boolean;
  title: string;
  label: string;
  initialName?: string;
  loading?: boolean;
  onClose: () => void;
  onSubmit: (name: string) => void;
}

type RenameTarget =
  | { kind: 'folder'; item: FolderResponse }
  | { kind: 'file'; item: FileResponse };

type DeleteTarget =
  | { kind: 'folder'; id: string; name: string }
  | { kind: 'file'; id: string; name: string };

function NameDialog({
  open,
  title,
  label,
  initialName = '',
  loading = false,
  onClose,
  onSubmit,
}: NameDialogProps) {
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<NameFormValues>({
    resolver: zodResolver(nameSchema),
    defaultValues: { name: initialName },
  });

  useEffect(() => {
    if (open) {
      reset({ name: initialName });
    }
  }, [initialName, open, reset]);

  return (
    <Dialog open={open} onClose={loading ? undefined : onClose} fullWidth maxWidth="xs">
      <DialogTitle>{title}</DialogTitle>
      <DialogContent>
        <Stack
          component="form"
          id="name-form"
          sx={{ pt: 1 }}
          onSubmit={handleSubmit((values) => onSubmit(values.name.trim()))}
        >
          <TextField
            label={label}
            autoFocus
            error={Boolean(errors.name)}
            helperText={errors.name?.message}
            {...register('name')}
          />
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} disabled={loading}>
          Отмена
        </Button>
        <Button type="submit" form="name-form" variant="contained" disabled={loading}>
          {loading ? 'Сохраняется...' : 'Сохранить'}
        </Button>
      </DialogActions>
    </Dialog>
  );
}

export function FilesPage() {
  const fileInputRef = useRef<HTMLInputElement | null>(null);
  const [breadcrumbs, setBreadcrumbs] = useState<FolderCrumb[]>([rootCrumb]);
  const [createFolderOpen, setCreateFolderOpen] = useState(false);
  const [renameTarget, setRenameTarget] = useState<RenameTarget | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<DeleteTarget | null>(null);
  const [shareTarget, setShareTarget] = useState<FileResponse | null>(null);
  const [actionError, setActionError] = useState<unknown>(null);
  const [notice, setNotice] = useState<string | null>(null);

  const currentFolder = breadcrumbs[breadcrumbs.length - 1] ?? rootCrumb;
  const currentFolderId = currentFolder.id;
  const foldersQuery = useListFoldersQuery({ parentId: currentFolderId, size: 50 });
  const filesQuery = useListFilesQuery({ folderId: currentFolderId, size: 50 });
  const [createFolder, createFolderState] = useCreateFolderMutation();
  const [updateFolder, updateFolderState] = useUpdateFolderMutation();
  const [deleteFolder, deleteFolderState] = useDeleteFolderMutation();
  const [uploadFile, uploadFileState] = useUploadFileMutation();
  const [updateFile, updateFileState] = useUpdateFileMutation();
  const [deleteFile, deleteFileState] = useDeleteFileMutation();
  const [downloadFile, downloadFileState] = useDownloadFileMutation();

  const folders = foldersQuery.data?.content ?? [];
  const files = filesQuery.data?.content ?? [];
  const isLoading = foldersQuery.isLoading || filesQuery.isLoading;
  const isFetching = foldersQuery.isFetching || filesQuery.isFetching;
  const hasRows = folders.length > 0 || files.length > 0;
  const mutationLoading =
    createFolderState.isLoading ||
    updateFolderState.isLoading ||
    deleteFolderState.isLoading ||
    uploadFileState.isLoading ||
    updateFileState.isLoading ||
    deleteFileState.isLoading ||
    downloadFileState.isLoading;

  const resetFeedback = () => {
    setActionError(null);
    setNotice(null);
  };

  const handleCreateFolder = async (name: string) => {
    resetFeedback();
    try {
      await createFolder({ name, parentId: currentFolderId }).unwrap();
      setCreateFolderOpen(false);
      setNotice('Папка создана');
    } catch (error) {
      setActionError(error);
    }
  };

  const handleRename = async (name: string) => {
    if (!renameTarget) {
      return;
    }

    resetFeedback();
    try {
      if (renameTarget.kind === 'folder') {
        await updateFolder({ folderId: renameTarget.item.id, name }).unwrap();
      } else {
        await updateFile({ fileId: renameTarget.item.id, originalName: name }).unwrap();
      }
      setRenameTarget(null);
      setNotice('Название обновлено');
    } catch (error) {
      setActionError(error);
    }
  };

  const handleDelete = async () => {
    if (!deleteTarget) {
      return;
    }

    resetFeedback();
    try {
      if (deleteTarget.kind === 'folder') {
        await deleteFolder(deleteTarget.id).unwrap();
      } else {
        await deleteFile(deleteTarget.id).unwrap();
      }
      setDeleteTarget(null);
      setNotice('Удалено');
    } catch (error) {
      setActionError(error);
    }
  };

  const handleUpload = async (file: File | undefined) => {
    if (!file) {
      return;
    }

    resetFeedback();
    try {
      await uploadFile({ file, folderId: currentFolderId }).unwrap();
      setNotice(`Файл "${file.name}" загружен`);
    } catch (error) {
      setActionError(error);
    }
  };

  const handleDownload = async (file: FileResponse) => {
    resetFeedback();
    try {
      const downloaded = await downloadFile(file).unwrap();
      saveBlob(downloaded.blob, downloaded.filename);
    } catch (error) {
      setActionError(error);
    }
  };

  return (
    <Stack spacing={3}>
      <PageHeader
        title="Files"
        description={
          <Breadcrumbs aria-label="Текущая папка">
            {breadcrumbs.map((crumb, index) => (
              <Button
                key={`${crumb.id ?? 'root'}-${index}`}
                variant="text"
                size="small"
                onClick={() => setBreadcrumbs((items) => items.slice(0, index + 1))}
                sx={{ px: 0.5, minWidth: 0 }}
              >
                {crumb.name}
              </Button>
            ))}
          </Breadcrumbs>
        }
        actions={
          <>
            <Button
              variant="outlined"
              startIcon={<FolderAddIcon />}
              onClick={() => {
                resetFeedback();
                setCreateFolderOpen(true);
              }}
            >
              Создать папку
            </Button>
            <Button
              variant="contained"
              startIcon={<UploadIcon />}
              disabled={uploadFileState.isLoading}
              onClick={() => fileInputRef.current?.click()}
            >
              Загрузить файл
            </Button>
            <input
              ref={fileInputRef}
              type="file"
              hidden
              onChange={(event) => {
                void handleUpload(event.target.files?.[0]);
                event.target.value = '';
              }}
            />
          </>
        }
      />

      {notice && <Alert severity="success">{notice}</Alert>}
      <ApiErrorAlert
        error={actionError || foldersQuery.error || filesQuery.error}
        fallback="Не удалось загрузить файлы"
      />

      <TableContainer
        component={Paper}
        elevation={0}
        sx={{
          border: 1,
          borderColor: 'divider',
          width: '100%',
          maxWidth: '100%',
          overflowX: 'auto',
          display: { xs: 'none', sm: 'block' },
        }}
      >
        {isFetching && <LinearProgress />}
        <Table size="small" sx={{ minWidth: 760 }}>
          <TableHead>
            <TableRow>
              <TableCell>Название</TableCell>
              <TableCell>Тип</TableCell>
              <TableCell>Размер</TableCell>
              <TableCell>Обновлен</TableCell>
              <TableCell align="right">Действия</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {folders.map((folder) => (
              <TableRow key={folder.id} hover>
                <TableCell>
                  <Button
                    variant="text"
                    startIcon={<FolderOpenIcon color="primary" />}
                    onClick={() => setBreadcrumbs((items) => [...items, { id: folder.id, name: folder.name }])}
                    sx={{ justifyContent: 'flex-start', px: 0, maxWidth: 340 }}
                  >
                    <Typography component="span" noWrap>
                      {folder.name}
                    </Typography>
                  </Button>
                </TableCell>
                <TableCell>Папка</TableCell>
                <TableCell>—</TableCell>
                <TableCell>{formatDateTime(folder.updatedAt)}</TableCell>
                <TableCell align="right">
                  <Tooltip title="Переименовать">
                    <IconButton
                      size="small"
                      aria-label={`Переименовать папку ${folder.name}`}
                      onClick={() => setRenameTarget({ kind: 'folder', item: folder })}
                    >
                      <EditIcon fontSize="small" />
                    </IconButton>
                  </Tooltip>
                  <Tooltip title="Удалить">
                    <IconButton
                      size="small"
                      color="error"
                      aria-label={`Удалить папку ${folder.name}`}
                      onClick={() => setDeleteTarget({ kind: 'folder', id: folder.id, name: folder.name })}
                    >
                      <DeleteIcon fontSize="small" />
                    </IconButton>
                  </Tooltip>
                </TableCell>
              </TableRow>
            ))}

            {files.map((file) => (
              <TableRow key={file.id} hover>
                <TableCell>
                  <Stack direction="row" spacing={1.25} alignItems="center" sx={{ minWidth: 0 }}>
                    <FileIcon color="secondary" fontSize="small" />
                    <Typography noWrap>{file.originalName}</Typography>
                  </Stack>
                </TableCell>
                <TableCell>{file.contentType || 'Файл'}</TableCell>
                <TableCell>{formatBytes(file.sizeBytes)}</TableCell>
                <TableCell>{formatDateTime(file.updatedAt)}</TableCell>
                <TableCell align="right">
                  <Tooltip title="Скачать">
                    <IconButton
                      size="small"
                      aria-label={`Скачать файл ${file.originalName}`}
                      onClick={() => void handleDownload(file)}
                    >
                      <DownloadIcon fontSize="small" />
                    </IconButton>
                  </Tooltip>
                  <Tooltip title="Публичная ссылка">
                    <IconButton
                      size="small"
                      aria-label={`Создать публичную ссылку для файла ${file.originalName}`}
                      onClick={() => setShareTarget(file)}
                    >
                      <LinkIcon fontSize="small" />
                    </IconButton>
                  </Tooltip>
                  <Tooltip title="Переименовать">
                    <IconButton
                      size="small"
                      aria-label={`Переименовать файл ${file.originalName}`}
                      onClick={() => setRenameTarget({ kind: 'file', item: file })}
                    >
                      <EditIcon fontSize="small" />
                    </IconButton>
                  </Tooltip>
                  <Tooltip title="Удалить">
                    <IconButton
                      size="small"
                      color="error"
                      aria-label={`Удалить файл ${file.originalName}`}
                      onClick={() => setDeleteTarget({ kind: 'file', id: file.id, name: file.originalName })}
                    >
                      <DeleteIcon fontSize="small" />
                    </IconButton>
                  </Tooltip>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>

        {!isLoading && !hasRows && (
          <EmptyState
            title="В этой папке пусто"
            description="Создайте папку или загрузите первый файл."
            actionLabel="Загрузить файл"
            actionIcon={<UploadIcon />}
            onAction={() => fileInputRef.current?.click()}
          />
        )}
      </TableContainer>

      <Paper
        elevation={0}
        sx={{
          border: 1,
          borderColor: 'divider',
          overflow: 'hidden',
          display: { xs: 'block', sm: 'none' },
        }}
      >
        {isFetching && <LinearProgress />}
        {hasRows ? (
          <Stack divider={<Box sx={{ borderTop: 1, borderColor: 'divider' }} />}>
            {folders.map((folder) => (
              <Box key={folder.id} sx={{ p: 2 }}>
                <Stack spacing={1.5}>
                  <Button
                    variant="text"
                    startIcon={<FolderOpenIcon color="primary" />}
                    onClick={() => setBreadcrumbs((items) => [...items, { id: folder.id, name: folder.name }])}
                    sx={{ justifyContent: 'flex-start', px: 0, minWidth: 0 }}
                  >
                    <Typography component="span" noWrap>
                      {folder.name}
                    </Typography>
                  </Button>
                  <Typography variant="caption" color="text.secondary">
                    Папка · обновлена {formatDateTime(folder.updatedAt)}
                  </Typography>
                  <Stack direction="row" spacing={0.5} justifyContent="flex-end">
                    <Tooltip title="Переименовать">
                      <IconButton
                        size="small"
                        aria-label={`Переименовать папку ${folder.name}`}
                        onClick={() => setRenameTarget({ kind: 'folder', item: folder })}
                      >
                        <EditIcon fontSize="small" />
                      </IconButton>
                    </Tooltip>
                    <Tooltip title="Удалить">
                      <IconButton
                        size="small"
                        color="error"
                        aria-label={`Удалить папку ${folder.name}`}
                        onClick={() => setDeleteTarget({ kind: 'folder', id: folder.id, name: folder.name })}
                      >
                        <DeleteIcon fontSize="small" />
                      </IconButton>
                    </Tooltip>
                  </Stack>
                </Stack>
              </Box>
            ))}

            {files.map((file) => (
              <Box key={file.id} sx={{ p: 2 }}>
                <Stack spacing={1.5}>
                  <Stack direction="row" spacing={1.25} alignItems="center" sx={{ minWidth: 0 }}>
                    <FileIcon color="secondary" fontSize="small" />
                    <Typography fontWeight={700} noWrap>
                      {file.originalName}
                    </Typography>
                  </Stack>
                  <Typography variant="caption" color="text.secondary">
                    {file.contentType || 'Файл'} · {formatBytes(file.sizeBytes)} · обновлен {formatDateTime(file.updatedAt)}
                  </Typography>
                  <Stack direction="row" spacing={0.5} justifyContent="flex-end">
                    <Tooltip title="Скачать">
                      <IconButton
                        size="small"
                        aria-label={`Скачать файл ${file.originalName}`}
                        onClick={() => void handleDownload(file)}
                      >
                        <DownloadIcon fontSize="small" />
                      </IconButton>
                    </Tooltip>
                    <Tooltip title="Публичная ссылка">
                      <IconButton
                        size="small"
                        aria-label={`Создать публичную ссылку для файла ${file.originalName}`}
                        onClick={() => setShareTarget(file)}
                      >
                        <LinkIcon fontSize="small" />
                      </IconButton>
                    </Tooltip>
                    <Tooltip title="Переименовать">
                      <IconButton
                        size="small"
                        aria-label={`Переименовать файл ${file.originalName}`}
                        onClick={() => setRenameTarget({ kind: 'file', item: file })}
                      >
                        <EditIcon fontSize="small" />
                      </IconButton>
                    </Tooltip>
                    <Tooltip title="Удалить">
                      <IconButton
                        size="small"
                        color="error"
                        aria-label={`Удалить файл ${file.originalName}`}
                        onClick={() => setDeleteTarget({ kind: 'file', id: file.id, name: file.originalName })}
                      >
                        <DeleteIcon fontSize="small" />
                      </IconButton>
                    </Tooltip>
                  </Stack>
                </Stack>
              </Box>
            ))}
          </Stack>
        ) : (
          !isLoading && (
            <EmptyState
              title="В этой папке пусто"
              description="Создайте папку или загрузите первый файл."
              actionLabel="Загрузить файл"
              actionIcon={<UploadIcon />}
              onAction={() => fileInputRef.current?.click()}
            />
          )
        )}
      </Paper>

      <NameDialog
        open={createFolderOpen}
        title="Новая папка"
        label="Название папки"
        loading={createFolderState.isLoading}
        onClose={() => setCreateFolderOpen(false)}
        onSubmit={(name) => void handleCreateFolder(name)}
      />

      <NameDialog
        open={Boolean(renameTarget)}
        title="Переименовать"
        label="Новое название"
        initialName={renameTarget?.kind === 'folder' ? renameTarget.item.name : renameTarget?.item.originalName}
        loading={updateFolderState.isLoading || updateFileState.isLoading}
        onClose={() => setRenameTarget(null)}
        onSubmit={(name) => void handleRename(name)}
      />

      <ConfirmDialog
        open={Boolean(deleteTarget)}
        title="Удалить ресурс?"
        description={`"${deleteTarget?.name ?? ''}" будет удален. Для непустой папки backend вернет ошибку.`}
        confirmLabel="Удалить"
        confirmColor="error"
        loading={deleteFolderState.isLoading || deleteFileState.isLoading}
        onClose={() => setDeleteTarget(null)}
        onConfirm={() => void handleDelete()}
      />

      <ShareDialog
        open={Boolean(shareTarget)}
        resourceType="FILE"
        resourceId={shareTarget?.id ?? null}
        resourceName={shareTarget?.originalName ?? ''}
        onClose={() => setShareTarget(null)}
      />

      {mutationLoading && <Box aria-live="polite" sx={{ position: 'absolute', width: 1, height: 1, overflow: 'hidden' }}>Операция выполняется</Box>}
    </Stack>
  );
}
