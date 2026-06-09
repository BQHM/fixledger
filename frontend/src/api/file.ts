import { axiosInstance, request } from './request';
import type { CredentialBox, FileResource, ManualSearchResult } from '@/types/business';
/**
 * 功能说明：按设备聚合查询凭证盒数据。
 * @param familyId 家庭空间 ID
 * @param deviceId 设备 ID
 * @returns 设备维度凭证盒聚合结果
 */
export function getCredentialBox(familyId: number, deviceId: number) {
  return request<CredentialBox>({
    url: `/api/families/${familyId}/devices/${deviceId}/credential-box`,
    method: 'get'
  });
}

/**
 * 功能说明：查询凭证盒附件数据。
 * @param familyId 家庭空间 ID
 * @param bizType 业务类型
 * @param bizId 业务 ID
 * @returns 请求结果或格式化后的展示数据
 */
export function getFiles(familyId: number, bizType: string, bizId: number) {
  return request<FileResource[]>({
    url: `/api/families/${familyId}/files`,
    method: 'get',
    params: { bizType, bizId }
  });
}

/**
 * 功能说明：按关键词搜索指定设备已索引的说明书内容。
 * @param familyId 家庭空间 ID
 * @param deviceId 设备 ID
 * @param keyword 搜索关键词
 * @returns 说明书搜索结果短片段
 */
export function searchManuals(familyId: number, deviceId: number, keyword: string) {
  return request<ManualSearchResult[]>({
    url: `/api/families/${familyId}/devices/${deviceId}/manuals/search`,
    method: 'get',
    params: { keyword }
  });
}
/**
 * 功能说明：上传凭证盒附件数据。
 * @param familyId 家庭空间 ID
 * @param bizType 业务类型
 * @param bizId 业务 ID
 * @param file 上传文件
 * @returns 请求结果或格式化后的展示数据
 */
export function uploadFile(familyId: number, bizType: string, bizId: number, file: File) {
  const data = new FormData();
  data.append('file', file);
  data.append('bizType', bizType);
  data.append('bizId', String(bizId));
  return request<FileResource>({
    url: `/api/families/${familyId}/files`,
    method: 'post',
    data
  });
}
/**
 * 功能说明：删除凭证盒附件数据。
 * @param familyId 家庭空间 ID
 * @param fileId 文件 ID
 * @returns 请求结果或格式化后的展示数据
 */
export function deleteFile(familyId: number, fileId: number) {
  return request<boolean>({
    url: `/api/families/${familyId}/files/${fileId}`,
    method: 'delete'
  });
}

/**
 * 功能说明：下载凭证盒附件并触发浏览器保存。
 * @param familyId 家庭空间 ID
 * @param file 附件元数据
 * @returns 下载完成后的空结果
 */
export async function downloadFile(familyId: number, file: FileResource) {
  // 附件下载返回二进制 Blob，不走 request<T> 的 JSON Result<T> 解包流程。
  const response = await axiosInstance.get(`/api/families/${familyId}/files/${file.id}/download`, {
    responseType: 'blob'
  });
  const blob = response.data instanceof Blob ? response.data : new Blob([response.data]);
  const objectUrl = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = objectUrl;
  link.download = file.originalName;
  link.click();
  URL.revokeObjectURL(objectUrl);
}

/**
 * 功能说明：读取凭证盒附件 Blob，用于页面内图片/PDF 预览。
 * @param familyId 家庭空间 ID
 * @param file 附件元数据
 * @returns 可预览的对象 URL，调用方关闭预览后负责 revoke
 */
export async function previewFile(familyId: number, file: FileResource) {
  const response = await axiosInstance.get(`/api/families/${familyId}/files/${file.id}/download`, {
    responseType: 'blob'
  });
  const blob = response.data instanceof Blob
    ? response.data
    : new Blob([response.data], { type: file.contentType });
  return URL.createObjectURL(blob);
}
/**
 * 功能说明：下载凭证盒附件数据。
 * @param familyId 家庭空间 ID
 * @param fileId 文件 ID
 * @returns 请求结果或格式化后的展示数据
 */
export function downloadUrl(familyId: number, fileId: number) {
  return `/api/families/${familyId}/files/${fileId}/download`;
}
