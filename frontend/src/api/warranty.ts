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
/**
 * 功能说明：查询保修数据。
 * @param familyId 家庭空间 ID
 * @param deviceId 设备 ID
 * @returns 请求结果或格式化后的展示数据
 */
export function getDeviceWarranties(familyId: number, deviceId: number) {
  return request<WarrantyRecord[]>({
    url: `/api/families/${familyId}/devices/${deviceId}/warranties`,
    method: 'get'
  });
}
/**
 * 功能说明：创建保修数据。
 * @param familyId 家庭空间 ID
 * @param deviceId 设备 ID
 * @param data 请求数据
 * @returns 请求结果或格式化后的展示数据
 */
export function createWarranty(familyId: number, deviceId: number, data: WarrantyForm) {
  return request<WarrantyRecord>({
    url: `/api/families/${familyId}/devices/${deviceId}/warranties`,
    method: 'post',
    data
  });
}
/**
 * 功能说明：更新保修数据。
 * @param familyId 家庭空间 ID
 * @param warrantyId 保修记录 ID
 * @param data 请求数据
 * @returns 请求结果或格式化后的展示数据
 */
export function updateWarranty(familyId: number, warrantyId: number, data: WarrantyForm) {
  return request<WarrantyRecord>({
    url: `/api/families/${familyId}/warranties/${warrantyId}`,
    method: 'put',
    data
  });
}
/**
 * 功能说明：删除保修数据。
 * @param familyId 家庭空间 ID
 * @param warrantyId 保修记录 ID
 * @returns 请求结果或格式化后的展示数据
 */
export function deleteWarranty(familyId: number, warrantyId: number) {
  return request<boolean>({
    url: `/api/families/${familyId}/warranties/${warrantyId}`,
    method: 'delete'
  });
}
/**
 * 功能说明：查询保修数据。
 * @param familyId 家庭空间 ID
 * @param params 查询参数
 * @returns 请求结果或格式化后的展示数据
 */
export function getExpiringWarranties(familyId: number, params: WarrantyQuery = {}) {
  return request<PageResponse<WarrantyRecord>>({
    url: `/api/families/${familyId}/warranties/expiring`,
    method: 'get',
    params
  });
}
