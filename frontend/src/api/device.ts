import { request } from './request';
import type { PageResponse } from '@/types/common';
import type { DeviceCategory, DeviceDetail, DeviceForm, DeviceListItem } from '@/types/device';

export interface DeviceQuery {
  pageNum?: number;
  pageSize?: number;
  keyword?: string;
  categoryId?: number;
  status?: string;
  brand?: string;
}

export function getDeviceCategories(familyId: number) {
  return request<DeviceCategory[]>({
    url: `/api/families/${familyId}/device-categories`,
    method: 'get'
  });
}

export function createDeviceCategory(familyId: number, data: Partial<DeviceCategory>) {
  return request<DeviceCategory>({
    url: `/api/families/${familyId}/device-categories`,
    method: 'post',
    data
  });
}

export function getDevicePage(familyId: number, params: DeviceQuery) {
  return request<PageResponse<DeviceListItem>>({
    url: `/api/families/${familyId}/devices`,
    method: 'get',
    params
  });
}

export function createDevice(familyId: number, data: DeviceForm) {
  return request<{ id: number }>({
    url: `/api/families/${familyId}/devices`,
    method: 'post',
    data
  });
}

export function getDeviceDetail(familyId: number, deviceId: number) {
  return request<DeviceDetail>({
    url: `/api/families/${familyId}/devices/${deviceId}`,
    method: 'get'
  });
}

export function updateDevice(familyId: number, deviceId: number, data: DeviceForm) {
  return request<DeviceDetail>({
    url: `/api/families/${familyId}/devices/${deviceId}`,
    method: 'put',
    data
  });
}

export function deleteDevice(familyId: number, deviceId: number) {
  return request<boolean>({
    url: `/api/families/${familyId}/devices/${deviceId}`,
    method: 'delete'
  });
}

export function updateDeviceStatus(familyId: number, deviceId: number, status: string) {
  return request<DeviceDetail>({
    url: `/api/families/${familyId}/devices/${deviceId}/status`,
    method: 'patch',
    data: { status }
  });
}