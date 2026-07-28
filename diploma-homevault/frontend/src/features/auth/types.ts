export type Role = 'USER' | 'ADMIN';

export type UserStatus = 'ACTIVE' | 'BLOCKED';

export interface UserResponse {
  id: string;
  email: string;
  displayName: string;
  status: UserStatus;
  roles: Role[];
  storageLimitBytes: number | null;
  createdAt: string;
  updatedAt: string;
}

export interface AuthResponse {
  tokenType: 'Bearer';
  accessToken: string;
  accessTokenExpiresAt: string;
  refreshToken: string;
  refreshTokenExpiresAt: string;
  user: UserResponse;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  displayName: string;
}

export interface LogoutRequest {
  refreshToken: string;
}
