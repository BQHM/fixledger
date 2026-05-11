import type { SelectOption } from '@/types/common';

export const deviceStatusOptions: SelectOption[] = [
  { label: '正常使用', value: 'NORMAL' },
  { label: '待维修', value: 'PENDING_REPAIR' },
  { label: '维修中', value: 'REPAIRING' },
  { label: '已维修', value: 'REPAIRED' },
  { label: '闲置', value: 'IDLE' },
  { label: '已报废', value: 'SCRAPPED' }
];

export const maintenanceStatusOptions: SelectOption[] = [
  { label: '待处理', value: 'PENDING' },
  { label: '已报修', value: 'REPORTED' },
  { label: '维修中', value: 'REPAIRING' },
  { label: '已完成', value: 'COMPLETED' },
  { label: '已取消', value: 'CANCELED' }
];

export const warrantyTypeOptions: SelectOption[] = [
  { label: '官方保修', value: 'OFFICIAL' },
  { label: '延保', value: 'EXTENDED' },
  { label: '店铺保修', value: 'STORE' },
  { label: '其他', value: 'OTHER' }
];

export const consumableStatusOptions: SelectOption[] = [
  { label: '正常', value: 'NORMAL' },
  { label: '即将到期', value: 'DUE_SOON' },
  { label: '已逾期', value: 'OVERDUE' },
  { label: '停用', value: 'DISABLED' }
];

export const reminderStatusOptions: SelectOption[] = [
  { label: '待提醒', value: 'PENDING' },
  { label: '已发送', value: 'SENT' },
  { label: '已读', value: 'READ' },
  { label: '已忽略', value: 'IGNORED' },
  { label: '发送失败', value: 'FAILED' }
];

export const reminderTypeOptions: SelectOption[] = [
  { label: '保修即将到期', value: 'WARRANTY_EXPIRE_SOON' },
  { label: '保修已到期', value: 'WARRANTY_EXPIRED' },
  { label: '耗材即将更换', value: 'CONSUMABLE_REPLACE_SOON' },
  { label: '耗材已逾期', value: 'CONSUMABLE_OVERDUE' },
  { label: '维修待跟进', value: 'MAINTENANCE_FOLLOW_UP' }
];

export const fileBizTypeOptions: SelectOption[] = [
  { label: '设备', value: 'DEVICE' },
  { label: '保修', value: 'WARRANTY' },
  { label: '维修', value: 'MAINTENANCE' },
  { label: '耗材', value: 'CONSUMABLE' },
  { label: '说明书', value: 'MANUAL' }
];

export function labelOf(options: SelectOption[], value?: string) {
  return options.find((item) => item.value === value)?.label ?? value ?? '-';
}

export function statusType(value?: string) {
  if (!value) return 'info';
  if (['NORMAL', 'COMPLETED', 'READ', 'SENT', 'REPAIRED'].includes(value)) return 'success';
  if (['DUE_SOON', 'REPORTED', 'REPAIRING', 'PENDING', 'WARRANTY_EXPIRE_SOON'].includes(value)) {
    return 'warning';
  }
  if (['OVERDUE', 'CANCELED', 'SCRAPPED', 'FAILED', 'WARRANTY_EXPIRED'].includes(value)) {
    return 'danger';
  }
  return 'info';
}

export function formatFileSize(size?: number) {
  if (!size) return '0 B';
  if (size < 1024) return `${size} B`;
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`;
  return `${(size / 1024 / 1024).toFixed(1)} MB`;
}
