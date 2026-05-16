import type { ConsumableItem, FileResource, MaintenanceRecord, WarrantyRecord } from '@/types/business';

export interface DeviceCategory {
  id: number;
  name: string;
  icon?: string;
  sortOrder: number;
  systemDefault: boolean;
}

export interface DeviceListItem {
  id: number;
  name: string;
  brand?: string;
  model?: string;
  categoryName?: string;
  purchaseDate?: string;
  purchasePrice?: number;
  location?: string;
  status: string;
  warrantyStatus?: string;
  nextReminderDate?: string;
}

export interface DeviceDetail extends DeviceListItem {
  categoryId?: number;
  serialNumber?: string;
  purchaseChannel?: string;
  remark?: string;
  warranties?: WarrantyRecord[];
  consumables?: ConsumableItem[];
  maintenanceRecords?: MaintenanceRecord[];
  files?: FileResource[];
}

export interface DeviceForm {
  categoryId?: number;
  name: string;
  brand?: string;
  model?: string;
  serialNumber?: string;
  purchaseDate?: string;
  purchaseChannel?: string;
  purchasePrice?: number;
  location?: string;
  remark?: string;
}
