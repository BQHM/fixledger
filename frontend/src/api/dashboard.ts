import { request } from './request';
import type {
  CategoryDistribution,
  DashboardSummary,
  MaintenanceCostTrend,
  ReminderCalendarDay
} from '@/types/dashboard';

export function getDashboardSummary(familyId: number) {
  return request<DashboardSummary>({
    url: `/api/families/${familyId}/dashboard/summary`,
    method: 'get'
  });
}

export function getCategoryDistribution(familyId: number) {
  return request<CategoryDistribution[]>({
    url: `/api/families/${familyId}/dashboard/device-category-distribution`,
    method: 'get'
  });
}

export function getMaintenanceCostTrend(familyId: number, months = 6) {
  return request<MaintenanceCostTrend[]>({
    url: `/api/families/${familyId}/dashboard/maintenance-cost-trend`,
    method: 'get',
    params: { months }
  });
}

export function getReminderCalendar(familyId: number, startDate?: string, endDate?: string) {
  return request<ReminderCalendarDay[]>({
    url: `/api/families/${familyId}/dashboard/reminder-calendar`,
    method: 'get',
    params: { startDate, endDate }
  });
}