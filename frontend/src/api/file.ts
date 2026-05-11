import { axiosInstance, request } from './request';
import type { FileResource } from '@/types/business';

export function getFiles(familyId: number, bizType: string, bizId: number) {
  return request<FileResource[]>({
    url: `/api/families/${familyId}/files`,
    method: 'get',
    params: { bizType, bizId }
  });
}

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

export function deleteFile(familyId: number, fileId: number) {
  return request<boolean>({
    url: `/api/families/${familyId}/files/${fileId}`,
    method: 'delete'
  });
}

export async function downloadFile(familyId: number, file: FileResource) {
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

export function downloadUrl(familyId: number, fileId: number) {
  return `/api/families/${familyId}/files/${fileId}/download`;
}
