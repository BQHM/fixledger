import { request } from './request';
import type { ReminderItem } from '@/types/business';
import type { PageResponse } from '@/types/common';

export interface ReminderQuery {
  pageNum?: number;
  pageSize?: number;
  status?: string;
  type?: string;
}

export interface ReminderScanResult {
  warrantyCreated: number;
  consumableCreated: number;
  notificationCreated: number;
  skippedDuplicate: number;
  failedCount: number;
}
/**
 * 功能说明：查询提醒数据。
 * @param familyId 家庭空间 ID
 * @param params 查询参数
 * @returns 请求结果或格式化后的展示数据
 */
export function getReminderPage(familyId: number, params: ReminderQuery = {}) {
  return request<PageResponse<ReminderItem>>({
    url: `/api/families/${familyId}/reminders`,
    method: 'get',
    params
  });
}
/**
 * 功能说明：查询提醒数据。
 * @param familyId 家庭空间 ID
 * @returns 请求结果或格式化后的展示数据
 */
export function getUnreadCount(familyId: number) {
  return request<{ count: number }>({
    url: `/api/families/${familyId}/reminders/unread-count`,
    method: 'get'
  });
}
/**
 * 功能说明：标记提醒数据。
 * @param familyId 家庭空间 ID
 * @param reminderId 提醒 ID
 * @returns 请求结果或格式化后的展示数据
 */
export function markReminderRead(familyId: number, reminderId: number) {
  return request<ReminderItem>({
    url: `/api/families/${familyId}/reminders/${reminderId}/read`,
    method: 'patch'
  });
}
/**
 * 功能说明：忽略提醒数据。
 * @param familyId 家庭空间 ID
 * @param reminderId 提醒 ID
 * @returns 请求结果或格式化后的展示数据
 */
export function ignoreReminder(familyId: number, reminderId: number) {
  return request<ReminderItem>({
    url: `/api/families/${familyId}/reminders/${reminderId}/ignore`,
    method: 'patch'
  });
}
/**
 * 功能说明：扫描提醒数据。
 * @param familyId 家庭空间 ID
 * @returns 请求结果或格式化后的展示数据
 */
export function scanReminders(familyId: number) {
  return request<ReminderScanResult>({
    url: `/api/families/${familyId}/reminders/scan`,
    method: 'post'
  });
}
