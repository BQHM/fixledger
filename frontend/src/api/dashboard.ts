import { request } from './request';
import type {
  CategoryDistribution,
  DashboardSummary,
  MaintenanceCostTrend,
  ReminderCalendarDay
} from '@/types/dashboard';
/**
 * 功能说明：查询首页看板数据。
 * @param familyId 家庭空间 ID
 * @returns 请求结果或格式化后的展示数据
 */
export function getDashboardSummary(familyId: number) {
  return request<DashboardSummary>({
    url: `/api/families/${familyId}/dashboard/summary`,
    method: 'get'
  });
}
/**
 * 功能说明：查询首页看板数据。
 * @param familyId 家庭空间 ID
 * @returns 请求结果或格式化后的展示数据
 */
export function getCategoryDistribution(familyId: number) {
  return request<CategoryDistribution[]>({
    url: `/api/families/${familyId}/dashboard/device-category-distribution`,
    method: 'get'
  });
}
/**
 * 功能说明：查询首页看板数据。
 * @param familyId 家庭空间 ID
 * @param months  6 months  6 参数
 * @returns 请求结果或格式化后的展示数据
 */
export function getMaintenanceCostTrend(familyId: number, months = 6) {
  return request<MaintenanceCostTrend[]>({
    url: `/api/families/${familyId}/dashboard/maintenance-cost-trend`,
    method: 'get',
    params: { months }
  });
}
/**
 * 功能说明：查询首页看板数据。
 * @param familyId 家庭空间 ID
 * @param startDate 开始日期
 * @param endDate 结束日期
 * @returns 请求结果或格式化后的展示数据
 */
export function getReminderCalendar(familyId: number, startDate?: string, endDate?: string) {
  return request<ReminderCalendarDay[]>({
    url: `/api/families/${familyId}/dashboard/reminder-calendar`,
    method: 'get',
    params: { startDate, endDate }
  });
}
