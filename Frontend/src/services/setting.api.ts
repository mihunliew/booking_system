import AppConfig from '@/config';
import type { SettingRequest, SettingResponse } from '@/dto/setting.dto';
import ApiHelper from '@/helpers/api_helper';

export default class SettingApi {
  // Public
  public static async getActivePaymentMethods(): Promise<SettingResponse[]> {
    const response = await ApiHelper.get(AppConfig.apiSettingsPaymentMethodsUrl);
    return response.data as SettingResponse[];
  }

  // Admin
  public static async getAllSettings(): Promise<SettingResponse[]> {
    const response = await ApiHelper.get(AppConfig.apiAdminSettingsUrl);
    return response.data as SettingResponse[];
  }

  public static async createSetting(data: SettingRequest): Promise<SettingResponse> {
    const response = await ApiHelper.post(AppConfig.apiAdminSettingsUrl, undefined, data);
    return response.data as SettingResponse;
  }

  public static async updateSetting(id: number, data: SettingRequest): Promise<SettingResponse> {
    const response = await ApiHelper.put(`${AppConfig.apiAdminSettingsUrl}/${id}`, undefined, data);
    return response.data as SettingResponse;
  }

  public static async deleteSetting(id: number): Promise<void> {
    await ApiHelper.delete(`${AppConfig.apiAdminSettingsUrl}/${id}`);
  }
}
