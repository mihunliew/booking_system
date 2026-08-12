import AppConfig from '../config';
import type {
  ApplyPromoRequest,
  BookingSummaryResponse,
  PromoCodeRequest,
  PromoCodeResponse
} from '../dto/promocode.dto';
import ApiHelper from '../helpers/api_helper';

export default class PromoCodeApi {
  public static async applyPromoCode(data: ApplyPromoRequest): Promise<BookingSummaryResponse> {
    try {
      const response = await ApiHelper.post(AppConfig.apiApplyPromoUrl, undefined, data);
      return response.data as BookingSummaryResponse;
    } catch (error: any) {
      if (error.response && error.response.status === 422 && error.response.data) {
        return error.response.data as BookingSummaryResponse;
      }
      throw error;
    }
  }

  public static async getAllPromoCodes(): Promise<PromoCodeResponse[]> {
    const response = await ApiHelper.get(AppConfig.apiAdminPromoCodesUrl);
    return response.data as PromoCodeResponse[];
  }

  public static async getPromoCodeById(id: number | string): Promise<PromoCodeResponse> {
    const response = await ApiHelper.get(`${AppConfig.apiAdminPromoCodesUrl}/${id}`);
    return response.data as PromoCodeResponse;
  }

  public static async createPromoCode(data: PromoCodeRequest): Promise<PromoCodeResponse> {
    const response = await ApiHelper.post(AppConfig.apiAdminPromoCodesUrl, undefined, data);
    return response.data as PromoCodeResponse;
  }

  public static async updatePromoCode(id: number | string, data: PromoCodeRequest): Promise<PromoCodeResponse> {
    const response = await ApiHelper.put(`${AppConfig.apiAdminPromoCodesUrl}/${id}`, undefined, data);
    return response.data as PromoCodeResponse;
  }

  public static async deletePromoCode(id: number | string): Promise<void> {
    await ApiHelper.delete(`${AppConfig.apiAdminPromoCodesUrl}/${id}`);
  }
}
