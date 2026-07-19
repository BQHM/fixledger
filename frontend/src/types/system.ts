export interface OperationLogResponse {
  id: number;
  userId?: number;
  familyId?: number;
  module: string;
  action: string;
  bizType?: string;
  bizId?: number;
  requestMethod?: string;
  requestUri?: string;
  ipAddress?: string;
  success: boolean;
  errorMessage?: string;
  createdAt: string;
}

export interface OperationLogQuery {
  pageNum?: number;
  pageSize?: number;
  familyId?: number;
  module?: string;
  action?: string;
}
