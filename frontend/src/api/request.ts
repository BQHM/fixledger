import axios, { type AxiosRequestConfig } from 'axios';
import { ElMessage } from 'element-plus';

import router from '@/router';
import { useAuthStore } from '@/stores/auth';
import type { Result } from '@/types/common';

const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 15000
});

const MIN_PAGE_NUM = 1;
const MIN_PAGE_SIZE = 1;
const MAX_PAGE_SIZE = 100;

function normalizePaginationParams(params: AxiosRequestConfig['params']) {
  if (!params) return params;

  if (params instanceof URLSearchParams) {
    clampSearchParam(params, 'pageNum', MIN_PAGE_NUM);
    clampSearchParam(params, 'pageSize', MIN_PAGE_SIZE, MAX_PAGE_SIZE);
    return params;
  }

  if (typeof params !== 'object') return params;

  const normalized = { ...params } as Record<string, unknown>;
  normalized.pageNum = clampNumber(normalized.pageNum, MIN_PAGE_NUM);
  normalized.pageSize = clampNumber(normalized.pageSize, MIN_PAGE_SIZE, MAX_PAGE_SIZE);
  return normalized;
}

function clampSearchParam(params: URLSearchParams, key: string, min: number, max?: number) {
  const value = params.get(key);
  if (value === null) return;
  const clamped = clampNumber(value, min, max);
  if (clamped !== value) {
    params.set(key, String(clamped));
  }
}

function clampNumber(value: unknown, min: number, max?: number) {
  const numberValue = Number(value);
  if (!Number.isFinite(numberValue)) return value;
  if (max !== undefined && numberValue > max) return max;
  if (numberValue < min) return min;
  return value;
}

service.interceptors.request.use((config) => {
  const auth = useAuthStore();
  if (auth.token) {
    config.headers.Authorization = `Bearer ${auth.token}`;
  }
  config.params = normalizePaginationParams(config.params);
  return config;
});

service.interceptors.response.use(
  (response) => response,
  (error) => {
    const message = error.response?.data?.message || error.message || '请求失败';
    if (error.response?.status === 401) {
      const auth = useAuthStore();
      auth.clearSession();
      router.push('/login');
    }
    ElMessage.error(message);
    return Promise.reject(error);
  }
);

export async function request<T>(config: AxiosRequestConfig): Promise<T> {
  const response = await service.request<Result<T>>(config);
  if (response.data.code !== 0) {
    ElMessage.error(response.data.message || '业务处理失败');
    throw new Error(response.data.message || 'Business error');
  }
  return response.data.data;
}

export { service as axiosInstance };
