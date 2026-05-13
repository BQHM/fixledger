export interface DashboardSummary {
  deviceTotal: number;
  warrantyExpiringCount: number;
  warrantyExpiredCount: number;
  consumableDueSoonCount: number;
  consumableOverdueCount: number;
  repairingCount: number;
  monthlyMaintenanceCost: number;
}

export interface CategoryDistribution {
  categoryName: string;
  count: number;
}

export interface MaintenanceCostTrend {
  month: string;
  cost: number;
}

export interface ReminderCalendarItem {
  id: number;
  reminderType: string;
  title: string;
  status: string;
  bizType: string;
  bizId: number;
  remindAt: string;
}

export interface ReminderCalendarDay {
  date: string;
  count: number;
  reminders: ReminderCalendarItem[];
}
