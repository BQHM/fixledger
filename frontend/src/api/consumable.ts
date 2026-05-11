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

export function getDeviceConsumables(familyId: number, deviceId: number) {
  return request<ConsumableItem[]>({
    url: `/api/families/${familyId}/devices/${deviceId}/consumables`,
    method: 'get'
  });
}

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

export function deleteConsumable(familyId: number, consumableId: number) {
  return request<boolean>({
    url: `/api/families/${familyId}/consumables/${consumableId}`,
    method: 'delete'
  });
}

export function getDueSoonConsumables(familyId: number, params: ConsumableQuery = {}) {
  return request<PageResponse<ConsumableItem>>({
    url: `/api/families/${familyId}/consumables/due-soon`,
    method: 'get',
    params
  });
}

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

export function getReplaceRecords(familyId: number, consumableId: number) {
  return request<ReplaceRecord[]>({
    url: `/api/families/${familyId}/consumables/${consumableId}/replace-records`,
    method: 'get'
  });
}
