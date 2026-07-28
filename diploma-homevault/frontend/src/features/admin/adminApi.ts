import { baseApi } from '../../services/baseApi';
import type { UserResponse, UserStatus } from '../auth/types';
import type { PageRequest, PageResponse } from '../../shared/apiTypes';

export interface AdminStatsResponse {
  totalUsers: number;
  activeUsers: number;
  blockedUsers: number;
  foldersCount: number;
  filesCount: number;
  totalStorageBytes: number;
  notesCount: number;
  shareLinksCount: number;
  activeShareLinks: number;
}

interface UpdateUserStatusRequest {
  userId: string;
  status: UserStatus;
}

export const adminApi = baseApi.injectEndpoints({
  endpoints: (builder) => ({
    listAdminUsers: builder.query<PageResponse<UserResponse>, PageRequest | void>({
      query: (request) => ({
        url: '/admin/users',
        params: {
          page: request?.page ?? 0,
          size: request?.size ?? 20,
          sort: request?.sort,
        },
      }),
      providesTags: (result) => [
        { type: 'Admin', id: 'USERS' },
        ...(result?.content.map((user) => ({ type: 'Admin' as const, id: user.id })) ?? []),
      ],
    }),
    updateUserStatus: builder.mutation<UserResponse, UpdateUserStatusRequest>({
      query: ({ userId, status }) => ({
        url: `/admin/users/${userId}/status`,
        method: 'PATCH',
        body: { status },
      }),
      invalidatesTags: ['Admin', 'Audit'],
    }),
    adminStats: builder.query<AdminStatsResponse, void>({
      query: () => '/admin/stats',
      providesTags: [{ type: 'Admin', id: 'STATS' }],
    }),
  }),
});

export const {
  useAdminStatsQuery,
  useListAdminUsersQuery,
  useUpdateUserStatusMutation,
} = adminApi;
