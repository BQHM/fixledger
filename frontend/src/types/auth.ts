export interface LoginRequest {
  account: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email?: string;
  nickname?: string;
  password: string;
}

export interface UserProfile {
  id: number;
  username: string;
  nickname?: string;
  email?: string;
}

export interface LoginResponse {
  accessToken: string;
  expiresIn: number;
  user: UserProfile;
  currentFamilyId?: number;
}

export interface RegisterResponse {
  userId: number;
  username: string;
  nickname?: string;
}
