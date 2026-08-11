import { Role } from '@/constants/enums'

export interface AdminPermissionDTO {
  moduleName: string;
  canCreate: boolean;
  canRead: boolean;
  canUpdate: boolean;
  canDelete: boolean;
}

export interface LoginRequest {
  username?: string;
  password?: string;
}

export interface SignupRequest {
  username?: string;
  email?: string;
  password?: string;
  fullName?: string;
  phone?: string;
}

export interface AdminCreateUserRequest {
  username?: string;
  email?: string;
  password?: string;
  fullName?: string;
  phone?: string;
  role?: Role | string;
  adminRoleId?: number | null;
}

export interface JwtResponse {
  token: string;
  type: string;
  id: number;
  username: string;
  email: string;
  fullName: string;
  role: Role;
  customRole?: string;
  permissions?: AdminPermissionDTO[];
}

export interface UserResponse {
  id: number;
  username: string;
  email: string;
  fullName: string;
  phone: string;
  role: Role;
  customRole?: string;
  permissions?: AdminPermissionDTO[];
  createdAt: string;
}
