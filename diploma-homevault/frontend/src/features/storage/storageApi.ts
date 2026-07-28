import { baseApi } from '../../services/baseApi';
import type { PageRequest, PageResponse } from '../../shared/apiTypes';
import type { FileResponse, FolderResponse } from './types';

interface FolderListRequest extends PageRequest {
  parentId?: string | null;
}

interface FileListRequest extends PageRequest {
  folderId?: string | null;
}

interface CreateFolderRequest {
  name: string;
  parentId?: string | null;
}

interface UpdateFolderRequest {
  folderId: string;
  name?: string;
  parentId?: string | null;
}

interface UploadFileRequest {
  file: File;
  folderId?: string | null;
}

interface UpdateFileRequest {
  fileId: string;
  originalName?: string;
  folderId?: string | null;
}

interface DownloadedFile {
  blob: Blob;
  filename: string;
}

function listParams(folderKey: 'parentId' | 'folderId', id: string | null | undefined, request: PageRequest) {
  return {
    ...(id ? { [folderKey]: id } : {}),
    page: request.page ?? 0,
    size: request.size ?? 50,
    sort: request.sort,
  };
}

function listTag(id: string | null | undefined) {
  return `LIST-${id ?? 'root'}`;
}

function filenameFromHeaders(meta: unknown, fallback: string) {
  const response = typeof meta === 'object' && meta !== null && 'response' in meta
    ? (meta as { response?: Response }).response
    : undefined;
  const disposition = response?.headers.get('content-disposition');
  if (!disposition) {
    return fallback;
  }

  const encoded = /filename\*=UTF-8''([^;]+)/i.exec(disposition);
  if (encoded?.[1]) {
    return decodeURIComponent(encoded[1].replace(/"/g, ''));
  }

  const basic = /filename="?([^";]+)"?/i.exec(disposition);
  return basic?.[1] ? basic[1] : fallback;
}

export const storageApi = baseApi.injectEndpoints({
  endpoints: (builder) => ({
    listFolders: builder.query<PageResponse<FolderResponse>, FolderListRequest | void>({
      query: (request) => ({
        url: '/folders',
        params: listParams('parentId', request?.parentId, request ?? {}),
      }),
      providesTags: (result, _error, request) => [
        { type: 'Folders', id: listTag(request?.parentId) },
        ...(result?.content.map((folder) => ({ type: 'Folders' as const, id: folder.id })) ?? []),
      ],
    }),
    createFolder: builder.mutation<FolderResponse, CreateFolderRequest>({
      query: (body) => ({
        url: '/folders',
        method: 'POST',
        body: {
          name: body.name,
          parentId: body.parentId || null,
        },
      }),
      invalidatesTags: ['Folders', 'Audit', 'Admin'],
    }),
    updateFolder: builder.mutation<FolderResponse, UpdateFolderRequest>({
      query: ({ folderId, ...body }) => ({
        url: `/folders/${folderId}`,
        method: 'PATCH',
        body,
      }),
      invalidatesTags: ['Folders'],
    }),
    deleteFolder: builder.mutation<void, string>({
      query: (folderId) => ({
        url: `/folders/${folderId}`,
        method: 'DELETE',
      }),
      invalidatesTags: ['Folders', 'Audit', 'Admin'],
    }),
    listFiles: builder.query<PageResponse<FileResponse>, FileListRequest | void>({
      query: (request) => ({
        url: '/files',
        params: listParams('folderId', request?.folderId, request ?? {}),
      }),
      providesTags: (result, _error, request) => [
        { type: 'Files', id: listTag(request?.folderId) },
        ...(result?.content.map((file) => ({ type: 'Files' as const, id: file.id })) ?? []),
      ],
    }),
    uploadFile: builder.mutation<FileResponse, UploadFileRequest>({
      query: ({ file, folderId }) => {
        const body = new FormData();
        body.append('file', file);
        if (folderId) {
          body.append('folderId', folderId);
        }

        return {
          url: '/files',
          method: 'POST',
          body,
        };
      },
      invalidatesTags: ['Files', 'Audit', 'Admin'],
    }),
    updateFile: builder.mutation<FileResponse, UpdateFileRequest>({
      query: ({ fileId, ...body }) => ({
        url: `/files/${fileId}`,
        method: 'PATCH',
        body,
      }),
      invalidatesTags: ['Files'],
    }),
    deleteFile: builder.mutation<void, string>({
      query: (fileId) => ({
        url: `/files/${fileId}`,
        method: 'DELETE',
      }),
      invalidatesTags: ['Files', 'Audit', 'Admin'],
    }),
    downloadFile: builder.mutation<DownloadedFile, FileResponse>({
      query: (file) => ({
        url: `/files/${file.id}/download`,
        responseHandler: async (response) => {
          const contentType = response.headers.get('content-type') ?? '';
          if (contentType.includes('application/json')) {
            return response.json();
          }

          return response.blob();
        },
      }),
      transformResponse: (response: Blob, meta, file) => ({
        blob: response,
        filename: filenameFromHeaders(meta, file.originalName),
      }),
      invalidatesTags: ['Audit'],
    }),
  }),
});

export const {
  useCreateFolderMutation,
  useDeleteFileMutation,
  useDeleteFolderMutation,
  useDownloadFileMutation,
  useListFilesQuery,
  useListFoldersQuery,
  useUpdateFileMutation,
  useUpdateFolderMutation,
  useUploadFileMutation,
} = storageApi;
