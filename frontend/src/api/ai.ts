import { request } from './request';
import type {
  InvoiceParseResponse,
  MaintenanceSummaryResponse,
  TroubleshootingResponse
} from '@/types/ai';
/**
 * 功能说明：解析AI 辅助数据。
 * @param familyId 家庭空间 ID
 * @param text text 参数
 * @returns 请求结果或格式化后的展示数据
 */
export function parseInvoice(familyId: number, text: string) {
  return request<InvoiceParseResponse>({
    url: `/api/families/${familyId}/ai/invoice-parse`,
    method: 'post',
    data: { text }
  });
}
/**
 * 功能说明：生成建议AI 辅助数据。
 * @returns 请求结果或格式化后的展示数据
 */
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
/**
 * 功能说明：生成总结AI 辅助数据。
 * @param familyId 家庭空间 ID
 * @param deviceId 设备 ID
 * @returns 请求结果或格式化后的展示数据
 */
export function summarizeMaintenance(familyId: number, deviceId: number) {
  return request<MaintenanceSummaryResponse>({
    url: `/api/families/${familyId}/ai/maintenance-summary`,
    method: 'post',
    data: { deviceId }
  });
}
