import { request } from './request';
import type { MaintenanceRecord } from '@/types/business';
import type { PageResponse } from '@/types/common';

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

export function getMaintenancePage(familyId: number, params: MaintenanceQuery = {}) {
  return request<PageResponse<MaintenanceRecord>>({
    url: `/api/families/${familyId}/maintenance-records`,
    method: 'get',
    params
  });
}

export function createMaintenance(familyId: number, deviceId: number, data: MaintenanceForm) {
  return request<MaintenanceRecord>({
    url: `/api/families/${familyId}/devices/${deviceId}/maintenance-records`,
    method: 'post',
    data
  });
}

export function getMaintenanceDetail(familyId: number, maintenanceId: number) {
  return request<MaintenanceRecord>({
    url: `/api/families/${familyId}/maintenance-records/${maintenanceId}`,
    method: 'get'
  });
}

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

export function deleteMaintenance(familyId: number, maintenanceId: number) {
  return request<boolean>({
    url: `/api/families/${familyId}/maintenance-records/${maintenanceId}`,
    method: 'delete'
  });
}

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
