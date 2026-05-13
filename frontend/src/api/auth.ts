import { request } from './request';
import type {
  LoginRequest,
  LoginResponse,
  RegisterRequest,
  RegisterResponse,
  UserProfile
} from '@/types/auth';
/**
 * 功能说明：登录认证数据。
 * @param data 请求数据
 * @returns 请求结果或格式化后的展示数据
 */
export function login(data: LoginRequest) {
  return request<LoginResponse>({ url: '/api/auth/login', method: 'post', data });
}
/**
 * 功能说明：注册认证数据。
 * @param data 请求数据
 * @returns 请求结果或格式化后的展示数据
 */
export function register(data: RegisterRequest) {
  return request<RegisterResponse>({ url: '/api/auth/register', method: 'post', data });
}
/**
 * 功能说明：退出登录认证数据。
 * @returns 请求结果或格式化后的展示数据
 */
export function logout() {
  return request<boolean>({ url: '/api/auth/logout', method: 'post' });
}
/**
 * 功能说明：查询认证数据。
 * @returns 请求结果或格式化后的展示数据
 */
export function getCurrentUser() {
  return request<UserProfile>({ url: '/api/auth/me', method: 'get' });
}
