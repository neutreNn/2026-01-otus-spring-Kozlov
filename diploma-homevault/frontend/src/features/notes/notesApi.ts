import { baseApi } from '../../services/baseApi';
import type { PageRequest, PageResponse } from '../../shared/apiTypes';
import type { NoteFormPayload, NoteResponse } from './types';

interface NotesListRequest extends PageRequest {
  query?: string;
  tag?: string;
}

interface UpdateNoteRequest extends NoteFormPayload {
  noteId: string;
}

function cleanText(value: string | undefined) {
  const trimmed = value?.trim();
  return trimmed ? trimmed : undefined;
}

export const notesApi = baseApi.injectEndpoints({
  endpoints: (builder) => ({
    listNotes: builder.query<PageResponse<NoteResponse>, NotesListRequest | void>({
      query: (request) => ({
        url: '/notes',
        params: {
          query: cleanText(request?.query),
          tag: cleanText(request?.tag),
          page: request?.page ?? 0,
          size: request?.size ?? 20,
          sort: request?.sort,
        },
      }),
      providesTags: (result) => [
        { type: 'Notes', id: 'LIST' },
        ...(result?.content.map((note) => ({ type: 'Notes' as const, id: note.id })) ?? []),
      ],
    }),
    createNote: builder.mutation<NoteResponse, NoteFormPayload>({
      query: (body) => ({
        url: '/notes',
        method: 'POST',
        body,
      }),
      invalidatesTags: ['Notes', 'Audit', 'Admin'],
    }),
    updateNote: builder.mutation<NoteResponse, UpdateNoteRequest>({
      query: ({ noteId, ...body }) => ({
        url: `/notes/${noteId}`,
        method: 'PUT',
        body,
      }),
      invalidatesTags: ['Notes', 'Audit'],
    }),
    deleteNote: builder.mutation<void, string>({
      query: (noteId) => ({
        url: `/notes/${noteId}`,
        method: 'DELETE',
      }),
      invalidatesTags: ['Notes', 'Shares', 'Audit', 'Admin'],
    }),
  }),
});

export const {
  useCreateNoteMutation,
  useDeleteNoteMutation,
  useListNotesQuery,
  useUpdateNoteMutation,
} = notesApi;
