export interface WarrantyRecord {
  id: number;
  deviceId: number;
  deviceName?: string;
  warrantyType: string;
  startDate: string;
  endDate: string;
  remindDaysBefore: number;
  servicePhone?: string;
  serviceAddress?: string;
  serviceNote?: string;
}

export interface ConsumableItem {
  id: number;
  deviceId: number;
  deviceName?: string;
  name: string;
  brand?: string;
  model?: string;
  cycleDays: number;
  lastReplacedDate?: string;
  nextRemindDate?: string;
  remindDaysBefore: number;
  status: string;
  enabled: boolean;
  remark?: string;
}

export interface ReplaceRecord {
  id: number;
  consumableId: number;
  deviceId: number;
  replacedDate: string;
  cost?: number;
  note?: string;
}

export interface MaintenanceRecord {
  id: number;
  deviceId: number;
  deviceName?: string;
  title: string;
  faultDescription: string;
  occurredAt?: string;
  status: string;
  repairChannel?: string;
  repairContact?: string;
  repairCost?: number;
  resultDescription?: string;
  completedAt?: string;
}

export interface ReminderItem {
  id: number;
  reminderType: string;
  bizType: string;
  bizId: number;
  title: string;
  content?: string;
  remindAt: string;
  status: string;
  readAt?: string;
}

export interface FileResource {
  id: number;
  originalName: string;
  contentType: string;
  fileSize: number;
  bizType: string;
  bizId: number;
}
