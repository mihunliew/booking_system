import AppConfig from '@/config';
import type { AdminDashboardDTO } from '@/dto/admin.dto';
import type { ProductDTO } from '@/dto/product.dto';
import type { BookingResponse, UpdateStatusRequest } from '@/dto/booking.dto';
import type { UserResponse, AdminPermissionDTO, AdminCreateUserRequest } from '@/dto/auth.dto';
import ApiHelper from '@/helpers/api_helper';

export interface AdminRoleRequest {
  name: string;
  description: string;
  permissions: AdminPermissionDTO[];
}

export interface AdminRoleResponse {
  id: number;
  name: string;
  description: string;
  permissions: AdminPermissionDTO[];
}

export default class AdminApi {
  public static async getDashboard(): Promise<AdminDashboardDTO> {
    const response = await ApiHelper.get(AppConfig.apiAdminDashboardUrl);
    return response.data as AdminDashboardDTO;
  }
  
  public static async getAllProducts(): Promise<ProductDTO[]> {
    const response = await ApiHelper.get(AppConfig.apiAdminProductsUrl);
    return response.data as ProductDTO[];
  }

  public static async createProduct(data: ProductDTO): Promise<ProductDTO> {
    const response = await ApiHelper.post(AppConfig.apiAdminProductsUrl, undefined, data);
    return response.data as ProductDTO;
  }

  public static async updateProduct(id: number | string, data: ProductDTO): Promise<ProductDTO> {
    const response = await ApiHelper.put(`${AppConfig.apiAdminProductsUrl}/${id}`, undefined, data);
    return response.data as ProductDTO;
  }

  public static async deleteProduct(id: number | string): Promise<void> {
    await ApiHelper.delete(`${AppConfig.apiAdminProductsUrl}/${id}`);
  }

  public static async getAllBookings(): Promise<BookingResponse[]> {
    const response = await ApiHelper.get(AppConfig.apiAdminBookingsUrl);
    return response.data as BookingResponse[];
  }

  public static async getBookingById(id: number | string): Promise<BookingResponse> {
    const response = await ApiHelper.get(`${AppConfig.apiAdminBookingsUrl}/${id}`);
    return response.data as BookingResponse;
  }

  public static async updateBookingStatus(id: number | string, data: UpdateStatusRequest): Promise<BookingResponse> {
    const response = await ApiHelper.put(`${AppConfig.apiAdminBookingsUrl}/${id}/status`, undefined, data);
    return response.data as BookingResponse;
  }

  public static async getAllUsers(): Promise<UserResponse[]> {
    const response = await ApiHelper.get(AppConfig.apiAdminUsersUrl);
    return response.data as UserResponse[];
  }

  public static async createUser(data: AdminCreateUserRequest): Promise<UserResponse> {
    const response = await ApiHelper.post(AppConfig.apiAdminUsersUrl, undefined, data);
    return response.data as UserResponse;
  }

  public static async updateUserRole(id: number | string, role: string, adminRoleId?: number): Promise<UserResponse> {
    let url = `${AppConfig.apiAdminUsersUrl}/${id}/role?role=${encodeURIComponent(role)}`;
    if (adminRoleId) {
      url += `&adminRoleId=${adminRoleId}`;
    }
    const response = await ApiHelper.put(url);
    return response.data as UserResponse;
  }

  public static async deleteUser(id: number | string): Promise<void> {
    await ApiHelper.delete(`${AppConfig.apiAdminUsersUrl}/${id}`);
  }

  // Role Management
  public static async getAllRoles(): Promise<AdminRoleResponse[]> {
    const response = await ApiHelper.get(AppConfig.apiAdminRolesUrl);
    return response.data as AdminRoleResponse[];
  }

  public static async createRole(data: AdminRoleRequest): Promise<AdminRoleResponse> {
    const response = await ApiHelper.post(AppConfig.apiAdminRolesUrl, undefined, data);
    return response.data as AdminRoleResponse;
  }

  public static async updateRole(id: number, data: AdminRoleRequest): Promise<AdminRoleResponse> {
    const response = await ApiHelper.put(`${AppConfig.apiAdminRolesUrl}/${id}`, undefined, data);
    return response.data as AdminRoleResponse;
  }

  public static async deleteRole(id: number): Promise<void> {
    await ApiHelper.delete(`${AppConfig.apiAdminRolesUrl}/${id}`);
  }
}
