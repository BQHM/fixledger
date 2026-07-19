import { request } from './request';
import type { PageResponse } from '@/types/common';
import type { OperationLogQuery, OperationLogResponse } from '@/types/system';

export function getOperationLogs(params: OperationLogQuery) {
  return request<PageResponse<OperationLogResponse>>({
    url: '/api/system/operation-logs',
    method: 'get',
    params
  });
}
