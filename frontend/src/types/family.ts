export interface FamilyResponse {
  id: number;
  name: string;
  description?: string;
  role: string;
  ownerUserId: number;
}

export interface FamilyMemberResponse {
  id: number;
  userId: number;
  username: string;
  nickname?: string;
  role: string;
  joinedAt: string;
}

export interface FamilyForm {
  name: string;
  description?: string;
}
