import { request } from './request';
import type {
  LoginRequest,
  LoginResponse,
  RegisterRequest,
  RegisterResponse,
  UserProfile
} from '@/types/auth';

export function login(data: LoginRequest) {
  return request<LoginResponse>({ url: '/api/auth/login', method: 'post', data });
}

export function register(data: RegisterRequest) {
  return request<RegisterResponse>({ url: '/api/auth/register', method: 'post', data });
}

export function logout() {
  return request<boolean>({ url: '/api/auth/logout', method: 'post' });
}

export function getCurrentUser() {
  return request<UserProfile>({ url: '/api/auth/me', method: 'get' });
}