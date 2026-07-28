import { baseApi } from '../../services/baseApi';
import type { PageRequest, PageResponse, ShareResourceType } from '../../shared/apiTypes';

export interface ShareResponse {
  id: string;
  token: string;
  resourceType: ShareResourceType;
  resourceId: string;
  expiresAt: string;
  revokedAt: string | null;
  accessCount: number;
  createdAt: string;
}

export interface CreateShareRequest {
  resourceType: ShareResourceType;
  resourceId: string;
  expiresAt: string;
}

export const sharesApi = baseApi.injectEndpoints({
  endpoints: (builder) => ({
    listShares: builder.query<PageResponse<ShareResponse>, PageRequest | void>({
      query: (request) => ({
        url: '/shares',
        params: {
          page: request?.page ?? 0,
          size: request?.size ?? 20,
          sort: request?.sort,
        },
      }),
      providesTags: (result) => [
        { type: 'Shares', id: 'LIST' },
        ...(result?.content.map((share) => ({ type: 'Shares' as const, id: share.id })) ?? []),
      ],
    }),
    createShare: builder.mutation<ShareResponse, CreateShareRequest>({
      query: (body) => ({
        url: '/shares',
        method: 'POST',
        body,
      }),
      invalidatesTags: ['Shares', 'Audit'],
    }),
    revokeShare: builder.mutation<void, string>({
      query: (shareId) => ({
        url: `/shares/${shareId}`,
        method: 'DELETE',
      }),
      invalidatesTags: ['Shares', 'Audit', 'Admin'],
    }),
  }),
});

export const { useCreateShareMutation, useListSharesQuery, useRevokeShareMutation } = sharesApi;
