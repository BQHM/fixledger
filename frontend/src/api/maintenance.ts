import { axiosInstance, request } from './request';
import type { MaintenanceRecord } from '@/types/business';
import type { PageResponse } from '@/types/common';
import { saveBlob } from '@/utils/download';

export interface MaintenanceQuery {
  pageNum?: number;
  pageSize?: number;
  deviceId?: number;
  status?: string;
}

export interface MaintenanceForm {
  title: string;
  faultDescription: string;
  occurredAt?: string;
  repairChannel?: string;
  repairContact?: string;
  repairCost?: number;
  resultDescription?: string;
  completedAt?: string;
}

export interface MaintenanceStatusForm {
  status: string;
  resultDescription?: string;
  repairCost?: number;
  completedAt?: string;
  syncDeviceRepaired?: boolean;
}

export interface MaintenanceCostSummary {
  totalCost: number;
  recordCount: number;
}
/**
 * 功能说明：查询维修记录数据。
 * @param familyId 家庭空间 ID
 * @param params 查询参数
 * @returns 请求结果或格式化后的展示数据
 */
export function getMaintenancePage(familyId: number, params: MaintenanceQuery = {}) {
  return request<PageResponse<MaintenanceRecord>>({
    url: `/api/families/${familyId}/maintenance-records`,
    method: 'get',
    params
  });
}
/**
 * 功能说明：创建维修记录数据。
 * @param familyId 家庭空间 ID
 * @param deviceId 设备 ID
 * @param data 请求数据
 * @returns 请求结果或格式化后的展示数据
 */
export function createMaintenance(familyId: number, deviceId: number, data: MaintenanceForm) {
  return request<MaintenanceRecord>({
    url: `/api/families/${familyId}/devices/${deviceId}/maintenance-records`,
    method: 'post',
    data
  });
}
/**
 * 功能说明：查询维修记录数据。
 * @param familyId 家庭空间 ID
 * @param maintenanceId 维修记录 ID
 * @returns 请求结果或格式化后的展示数据
 */
export function getMaintenanceDetail(familyId: number, maintenanceId: number) {
  return request<MaintenanceRecord>({
    url: `/api/families/${familyId}/maintenance-records/${maintenanceId}`,
    method: 'get'
  });
}
/**
 * 功能说明：更新维修记录数据。
 * @returns 请求结果或格式化后的展示数据
 */
export function updateMaintenance(
  familyId: number,
  maintenanceId: number,
  data: MaintenanceForm
) {
  return request<MaintenanceRecord>({
    url: `/api/families/${familyId}/maintenance-records/${maintenanceId}`,
    method: 'put',
    data
  });
}
/**
 * 功能说明：更新维修记录数据。
 * @returns 请求结果或格式化后的展示数据
 */
export function updateMaintenanceStatus(
  familyId: number,
  maintenanceId: number,
  data: MaintenanceStatusForm
) {
  return request<MaintenanceRecord>({
    url: `/api/families/${familyId}/maintenance-records/${maintenanceId}/status`,
    method: 'patch',
    data
  });
}
/**
 * 功能说明：删除维修记录数据。
 * @param familyId 家庭空间 ID
 * @param maintenanceId 维修记录 ID
 * @returns 请求结果或格式化后的展示数据
 */
export function deleteMaintenance(familyId: number, maintenanceId: number) {
  return request<boolean>({
    url: `/api/families/${familyId}/maintenance-records/${maintenanceId}`,
    method: 'delete'
  });
}
/**
 * 功能说明：查询维修记录数据。
 * @returns 请求结果或格式化后的展示数据
 */
export function getMaintenanceCostSummary(
  familyId: number,
  params: { startDate?: string; endDate?: string } = {}
) {
  return request<MaintenanceCostSummary>({
    url: `/api/families/${familyId}/maintenance-records/cost-summary`,
    method: 'get',
    params
  });
}

/**
 * 功能说明：导出家庭维修费用报表 CSV。
 * @param familyId 家庭空间 ID
 * @param params 日期筛选
 * @returns 下载完成后的空结果
 */
export async function exportMaintenanceCostCsv(
  familyId: number,
  params: { startDate?: string; endDate?: string } = {}
) {
  const response = await axiosInstance.get(
    `/api/families/${familyId}/exports/maintenance-costs.csv`,
    {
      params,
      responseType: 'blob'
    }
  );
  const blob = response.data instanceof Blob ? response.data : new Blob([response.data]);
  saveBlob(blob, `fixledger-maintenance-costs-${familyId}.csv`);
}
