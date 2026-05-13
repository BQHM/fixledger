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
/**
 * 功能说明：查询设备护照数据。
 * @param familyId 家庭空间 ID
 * @returns 请求结果或格式化后的展示数据
 */
export function getDeviceCategories(familyId: number) {
  return request<DeviceCategory[]>({
    url: `/api/families/${familyId}/device-categories`,
    method: 'get'
  });
}
/**
 * 功能说明：创建设备护照数据。
 * @param familyId 家庭空间 ID
 * @param data 请求数据
 * @returns 请求结果或格式化后的展示数据
 */
export function createDeviceCategory(familyId: number, data: Partial<DeviceCategory>) {
  return request<DeviceCategory>({
    url: `/api/families/${familyId}/device-categories`,
    method: 'post',
    data
  });
}
/**
 * 功能说明：查询设备护照数据。
 * @param familyId 家庭空间 ID
 * @param params 查询参数
 * @returns 请求结果或格式化后的展示数据
 */
export function getDevicePage(familyId: number, params: DeviceQuery) {
  return request<PageResponse<DeviceListItem>>({
    url: `/api/families/${familyId}/devices`,
    method: 'get',
    params
  });
}
/**
 * 功能说明：创建设备护照数据。
 * @param familyId 家庭空间 ID
 * @param data 请求数据
 * @returns 请求结果或格式化后的展示数据
 */
export function createDevice(familyId: number, data: DeviceForm) {
  return request<{ id: number }>({
    url: `/api/families/${familyId}/devices`,
    method: 'post',
    data
  });
}
/**
 * 功能说明：查询设备护照数据。
 * @param familyId 家庭空间 ID
 * @param deviceId 设备 ID
 * @returns 请求结果或格式化后的展示数据
 */
export function getDeviceDetail(familyId: number, deviceId: number) {
  return request<DeviceDetail>({
    url: `/api/families/${familyId}/devices/${deviceId}`,
    method: 'get'
  });
}
/**
 * 功能说明：更新设备护照数据。
 * @param familyId 家庭空间 ID
 * @param deviceId 设备 ID
 * @param data 请求数据
 * @returns 请求结果或格式化后的展示数据
 */
export function updateDevice(familyId: number, deviceId: number, data: DeviceForm) {
  return request<DeviceDetail>({
    url: `/api/families/${familyId}/devices/${deviceId}`,
    method: 'put',
    data
  });
}
/**
 * 功能说明：删除设备护照数据。
 * @param familyId 家庭空间 ID
 * @param deviceId 设备 ID
 * @returns 请求结果或格式化后的展示数据
 */
export function deleteDevice(familyId: number, deviceId: number) {
  return request<boolean>({
    url: `/api/families/${familyId}/devices/${deviceId}`,
    method: 'delete'
  });
}
/**
 * 功能说明：更新设备护照数据。
 * @param familyId 家庭空间 ID
 * @param deviceId 设备 ID
 * @param status 目标状态
 * @returns 请求结果或格式化后的展示数据
 */
export function updateDeviceStatus(familyId: number, deviceId: number, status: string) {
  return request<DeviceDetail>({
    url: `/api/families/${familyId}/devices/${deviceId}/status`,
    method: 'patch',
    data: { status }
  });
}
