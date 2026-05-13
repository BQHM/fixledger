import { request } from './request';
import type { ConsumableItem, ReplaceRecord } from '@/types/business';
import type { PageResponse } from '@/types/common';

export interface ConsumableQuery {
  pageNum?: number;
  pageSize?: number;
  days?: number;
}

export interface CreateConsumableForm {
  name: string;
  brand?: string;
  model?: string;
  cycleDays: number;
  lastReplacedDate?: string;
  remindDaysBefore?: number;
  remark?: string;
}

export interface ConsumableForm extends CreateConsumableForm {
  enabled?: boolean;
}

export interface ReplaceRecordForm {
  replacedDate: string;
  cost?: number;
  note?: string;
}
/**
 * 功能说明：查询耗材数据。
 * @param familyId 家庭空间 ID
 * @param deviceId 设备 ID
 * @returns 请求结果或格式化后的展示数据
 */
export function getDeviceConsumables(familyId: number, deviceId: number) {
  return request<ConsumableItem[]>({
    url: `/api/families/${familyId}/devices/${deviceId}/consumables`,
    method: 'get'
  });
}
/**
 * 功能说明：创建耗材数据。
 * @returns 请求结果或格式化后的展示数据
 */
export function createConsumable(
  familyId: number,
  deviceId: number,
  data: CreateConsumableForm
) {
  return request<ConsumableItem>({
    url: `/api/families/${familyId}/devices/${deviceId}/consumables`,
    method: 'post',
    data
  });
}
/**
 * 功能说明：更新耗材数据。
 * @returns 请求结果或格式化后的展示数据
 */
export function updateConsumable(
  familyId: number,
  consumableId: number,
  data: ConsumableForm
) {
  return request<ConsumableItem>({
    url: `/api/families/${familyId}/consumables/${consumableId}`,
    method: 'put',
    data
  });
}
/**
 * 功能说明：删除耗材数据。
 * @param familyId 家庭空间 ID
 * @param consumableId 耗材 ID
 * @returns 请求结果或格式化后的展示数据
 */
export function deleteConsumable(familyId: number, consumableId: number) {
  return request<boolean>({
    url: `/api/families/${familyId}/consumables/${consumableId}`,
    method: 'delete'
  });
}
/**
 * 功能说明：查询耗材数据。
 * @param familyId 家庭空间 ID
 * @param params 查询参数
 * @returns 请求结果或格式化后的展示数据
 */
export function getDueSoonConsumables(familyId: number, params: ConsumableQuery = {}) {
  return request<PageResponse<ConsumableItem>>({
    url: `/api/families/${familyId}/consumables/due-soon`,
    method: 'get',
    params
  });
}
/**
 * 功能说明：创建耗材数据。
 * @returns 请求结果或格式化后的展示数据
 */
export function createReplaceRecord(
  familyId: number,
  consumableId: number,
  data: ReplaceRecordForm
) {
  return request<ReplaceRecord>({
    url: `/api/families/${familyId}/consumables/${consumableId}/replace-records`,
    method: 'post',
    data
  });
}
/**
 * 功能说明：查询耗材数据。
 * @param familyId 家庭空间 ID
 * @param consumableId 耗材 ID
 * @returns 请求结果或格式化后的展示数据
 */
export function getReplaceRecords(familyId: number, consumableId: number) {
  return request<ReplaceRecord[]>({
    url: `/api/families/${familyId}/consumables/${consumableId}/replace-records`,
    method: 'get'
  });
}
