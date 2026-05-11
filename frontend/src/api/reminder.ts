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

export function getReminderPage(familyId: number, params: ReminderQuery = {}) {
  return request<PageResponse<ReminderItem>>({
    url: `/api/families/${familyId}/reminders`,
    method: 'get',
    params
  });
}

export function getUnreadCount(familyId: number) {
  return request<{ count: number }>({
    url: `/api/families/${familyId}/reminders/unread-count`,
    method: 'get'
  });
}

export function markReminderRead(familyId: number, reminderId: number) {
  return request<ReminderItem>({
    url: `/api/families/${familyId}/reminders/${reminderId}/read`,
    method: 'patch'
  });
}

export function ignoreReminder(familyId: number, reminderId: number) {
  return request<ReminderItem>({
    url: `/api/families/${familyId}/reminders/${reminderId}/ignore`,
    method: 'patch'
  });
}

export function scanReminders(familyId: number) {
  return request<ReminderScanResult>({
    url: `/api/families/${familyId}/reminders/scan`,
    method: 'post'
  });
}
