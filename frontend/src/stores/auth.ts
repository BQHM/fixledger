import { defineStore } from 'pinia';

import { getCurrentUser, login, logout, register } from '@/api/auth';
import { getFamilies } from '@/api/family';
import type { LoginRequest, RegisterRequest, UserProfile } from '@/types/auth';
import type { FamilyResponse } from '@/types/family';

interface AuthState {
  token: string;
  user?: UserProfile;
  families: FamilyResponse[];
  currentFamilyId?: number;
}

const TOKEN_KEY = 'fixledger_token';
const FAMILY_KEY = 'fixledger_family_id';

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
    async login(data: LoginRequest) {
      const response = await login(data);
      this.token = response.accessToken;
      this.user = response.user;
      this.currentFamilyId = response.currentFamilyId;
      localStorage.setItem(TOKEN_KEY, response.accessToken);
      if (response.currentFamilyId) {
        localStorage.setItem(FAMILY_KEY, String(response.currentFamilyId));
      }
      await this.loadFamilies();
    },
    async registerAndLogin(data: RegisterRequest) {
      await register(data);
      await this.login({ account: data.username, password: data.password });
    },
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
    async loadFamilies() {
      this.families = await getFamilies();
      if (!this.currentFamilyId && this.families.length > 0) {
        this.setCurrentFamily(this.families[0].id);
      }
    },
    setCurrentFamily(familyId: number) {
      this.currentFamilyId = familyId;
      localStorage.setItem(FAMILY_KEY, String(familyId));
    },
    async logout() {
      if (this.token) {
        await logout().catch(() => undefined);
      }
      this.clearSession();
    },
    clearSession() {
      this.token = '';
      this.user = undefined;
      this.families = [];
      this.currentFamilyId = undefined;
      localStorage.removeItem(TOKEN_KEY);
      localStorage.removeItem(FAMILY_KEY);
    }
  }
});