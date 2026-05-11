import { request } from './request';
import type { FamilyForm, FamilyMemberResponse, FamilyResponse } from '@/types/family';

export function getFamilies() {
  return request<FamilyResponse[]>({ url: '/api/families', method: 'get' });
}

export function createFamily(data: FamilyForm) {
  return request<FamilyResponse>({ url: '/api/families', method: 'post', data });
}

export function updateFamily(familyId: number, data: FamilyForm) {
  return request<FamilyResponse>({ url: `/api/families/${familyId}`, method: 'put', data });
}

export function getFamilyMembers(familyId: number) {
  return request<FamilyMemberResponse[]>({
    url: `/api/families/${familyId}/members`,
    method: 'get'
  });
}