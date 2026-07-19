import { request } from './request';
import type {
  FamilyForm,
  FamilyMemberResponse,
  FamilyResponse,
  InviteFamilyMemberRequest,
  UpdateFamilyMemberRoleRequest
} from '@/types/family';
/**
 * 功能说明：查询家庭空间数据。
 * @returns 请求结果或格式化后的展示数据
 */
export function getFamilies() {
  return request<FamilyResponse[]>({ url: '/api/families', method: 'get' });
}
/**
 * 功能说明：创建家庭空间数据。
 * @param data 请求数据
 * @returns 请求结果或格式化后的展示数据
 */
export function createFamily(data: FamilyForm) {
  return request<FamilyResponse>({ url: '/api/families', method: 'post', data });
}
/**
 * 功能说明：更新家庭空间数据。
 * @param familyId 家庭空间 ID
 * @param data 请求数据
 * @returns 请求结果或格式化后的展示数据
 */
export function updateFamily(familyId: number, data: FamilyForm) {
  return request<FamilyResponse>({ url: `/api/families/${familyId}`, method: 'put', data });
}
/**
 * 功能说明：查询家庭空间数据。
 * @param familyId 家庭空间 ID
 * @returns 请求结果或格式化后的展示数据
 */
export function getFamilyMembers(familyId: number) {
  return request<FamilyMemberResponse[]>({
    url: `/api/families/${familyId}/members`,
    method: 'get'
  });
}

export function inviteFamilyMember(familyId: number, data: InviteFamilyMemberRequest) {
  return request<FamilyMemberResponse>({
    url: `/api/families/${familyId}/members`,
    method: 'post',
    data
  });
}

export function updateFamilyMemberRole(
  familyId: number,
  memberId: number,
  data: UpdateFamilyMemberRoleRequest
) {
  return request<FamilyMemberResponse>({
    url: `/api/families/${familyId}/members/${memberId}/role`,
    method: 'put',
    data
  });
}

export function removeFamilyMember(familyId: number, memberId: number) {
  return request<boolean>({
    url: `/api/families/${familyId}/members/${memberId}`,
    method: 'delete'
  });
}
