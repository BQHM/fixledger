import { request } from './request';
import type { PageResponse } from '@/types/common';
import type { WarrantyRecord } from '@/types/business';

export interface WarrantyQuery {
  pageNum?: number;
  pageSize?: number;
  days?: number;
}

export interface WarrantyForm {
  warrantyType?: string;
  startDate: string;
  endDate: string;
  remindDaysBefore?: number;
  servicePhone?: string;
  serviceAddress?: string;
  serviceNote?: string;
}

export function getDeviceWarranties(familyId: number, deviceId: number) {
  return request<WarrantyRecord[]>({
    url: `/api/families/${familyId}/devices/${deviceId}/warranties`,
    method: 'get'
  });
}

export function createWarranty(familyId: number, deviceId: number, data: WarrantyForm) {
  return request<WarrantyRecord>({
    url: `/api/families/${familyId}/devices/${deviceId}/warranties`,
    method: 'post',
    data
  });
}

export function updateWarranty(familyId: number, warrantyId: number, data: WarrantyForm) {
  return request<WarrantyRecord>({
    url: `/api/families/${familyId}/warranties/${warrantyId}`,
    method: 'put',
    data
  });
}

export function deleteWarranty(familyId: number, warrantyId: number) {
  return request<boolean>({
    url: `/api/families/${familyId}/warranties/${warrantyId}`,
    method: 'delete'
  });
}

export function getExpiringWarranties(familyId: number, params: WarrantyQuery = {}) {
  return request<PageResponse<WarrantyRecord>>({
    url: `/api/families/${familyId}/warranties/expiring`,
    method: 'get',
    params
  });
}
