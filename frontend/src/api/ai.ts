import { request } from './request';
import type {
  InvoiceParseResponse,
  MaintenanceSummaryResponse,
  TroubleshootingResponse
} from '@/types/ai';

export function parseInvoice(familyId: number, text: string) {
  return request<InvoiceParseResponse>({
    url: `/api/families/${familyId}/ai/invoice-parse`,
    method: 'post',
    data: { text }
  });
}

export function suggestTroubleshooting(
  familyId: number,
  data: { deviceId: number; maintenanceId?: number; faultDescription: string }
) {
  return request<TroubleshootingResponse>({
    url: `/api/families/${familyId}/ai/troubleshooting`,
    method: 'post',
    data
  });
}

export function summarizeMaintenance(familyId: number, deviceId: number) {
  return request<MaintenanceSummaryResponse>({
    url: `/api/families/${familyId}/ai/maintenance-summary`,
    method: 'post',
    data: { deviceId }
  });
}