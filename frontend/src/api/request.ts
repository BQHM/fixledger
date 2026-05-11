import axios, { type AxiosRequestConfig } from 'axios';
import { ElMessage } from 'element-plus';

import router from '@/router';
import { useAuthStore } from '@/stores/auth';
import type { Result } from '@/types/common';

const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 15000
});

service.interceptors.request.use((config) => {
  const auth = useAuthStore();
  if (auth.token) {
    config.headers.Authorization = `Bearer ${auth.token}`;
  }
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