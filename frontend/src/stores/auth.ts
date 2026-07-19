import { defineStore } from 'pinia';

import { getCurrentUser, login, logout, register } from '@/api/auth';
import { getFamilies } from '@/api/family';
import type { LoginRequest, RegisterRequest, UserProfile } from '@/types/auth';
import type { FamilyResponse } from '@/types/family';

/**
 * 功能说明：保存登录令牌、当前用户、家庭空间和当前家庭上下文。
 */
interface AuthState {
  token: string;
  user?: UserProfile;
  families: FamilyResponse[];
  currentFamilyId?: number;
}

const TOKEN_KEY = 'fixledger_token';
const FAMILY_KEY = 'fixledger_family_id';

export function resolveCurrentFamilyId(
  currentFamilyId: number | undefined,
  families: FamilyResponse[]
) {
  if (
    currentFamilyId !== undefined
    && families.some((family) => family.id === currentFamilyId)
  ) {
    return currentFamilyId;
  }
  return families[0]?.id;
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    token: localStorage.getItem(TOKEN_KEY) || '',
    user: undefined,
    families: [],
    currentFamilyId: Number(localStorage.getItem(FAMILY_KEY)) || undefined
  }),
  getters: {
    isAuthenticated: (state) => Boolean(state.token),
    currentFamily: (state) => state.families.find((item) => item.id === state.currentFamilyId)
  },
  actions: {
    /**
     * 功能说明：完成登录并初始化用户资料与家庭空间上下文。
     * @param data 登录请求数据
     */
    async login(data: LoginRequest) {
      const response = await login(data);
      this.token = response.accessToken;
      this.user = response.user;
      this.currentFamilyId = response.currentFamilyId;
      localStorage.setItem(TOKEN_KEY, response.accessToken);
      if (response.currentFamilyId) {
        localStorage.setItem(FAMILY_KEY, String(response.currentFamilyId));
      } else {
        localStorage.removeItem(FAMILY_KEY);
      }
      await this.loadFamilies();
    },
    /**
     * 功能说明：注册成功后立即使用新账号登录。
     * @param data 注册请求数据
     */
    async registerAndLogin(data: RegisterRequest) {
      await register(data);
      await this.login({ account: data.username, password: data.password });
    },
    /**
     * 功能说明：刷新页面后根据本地令牌恢复登录态。
     */
    async bootstrap() {
      if (!this.token) {
        return;
      }
      try {
        this.user = await getCurrentUser();
        await this.loadFamilies();
      } catch {
        this.clearSession();
      }
    },
    /**
     * 功能说明：加载当前用户可访问的家庭空间列表。
     */
    async loadFamilies() {
      this.families = await getFamilies();
      const nextFamilyId = resolveCurrentFamilyId(this.currentFamilyId, this.families);
      if (nextFamilyId !== undefined) {
        this.setCurrentFamily(nextFamilyId);
      } else {
        this.clearCurrentFamily();
      }
    },
    /**
     * 功能说明：切换当前操作的家庭空间。
     * @param familyId 家庭空间 ID
     */
    setCurrentFamily(familyId: number) {
      this.currentFamilyId = familyId;
      localStorage.setItem(FAMILY_KEY, String(familyId));
    },
    /**
     * 功能说明：清理失效的当前家庭上下文，避免业务页继续使用脏 ID。
     */
    clearCurrentFamily() {
      this.currentFamilyId = undefined;
      localStorage.removeItem(FAMILY_KEY);
    },
    /**
     * 功能说明：退出登录并清理本地会话。
     */
    async logout() {
      if (this.token) {
        await logout().catch(() => undefined);
      }
      this.clearSession();
    },
    /**
     * 功能说明：清空令牌、用户资料和家庭上下文。
     */
    clearSession() {
      this.token = '';
      this.user = undefined;
      this.families = [];
      localStorage.removeItem(TOKEN_KEY);
      this.clearCurrentFamily();
    }
  }
});
