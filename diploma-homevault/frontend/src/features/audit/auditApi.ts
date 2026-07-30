import { baseApi } from '../../services/baseApi';
import type { PageRequest, PageResponse } from '../../shared/apiTypes';

export interface AuditEventResponse {
  id: string;
  actorUserId: string | null;
  action: string;
  entityType: string;
  entityId: string | null;
  ipAddress: string | null;
  userAgent: string | null;
  details: Record<string, unknown> | null;
  createdAt: string;
}

export const auditApi = baseApi.injectEndpoints({
  endpoints: (builder) => ({
    listAuditEvents: builder.query<PageResponse<AuditEventResponse>, PageRequest | void>({
      query: (request) => ({
        url: '/audit/events',
        params: {
          page: request?.page ?? 0,
          size: request?.size ?? 30,
          sort: request?.sort,
        },
      }),
      providesTags: ['Audit'],
    }),
  }),
});

export const { useListAuditEventsQuery } = auditApi;
