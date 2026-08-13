import AppConfig from '@/config';
import type { ProductDTO } from '@/dto/product.dto';
import ApiHelper from '@/helpers/api_helper';

export default class ProductApi {
  public static async getAllProducts(category?: string): Promise<ProductDTO[]> {
    const url = category 
      ? `${AppConfig.apiGetProductUrl}?category=${encodeURIComponent(category)}` 
      : AppConfig.apiGetProductUrl;
    const response = await ApiHelper.get(url);
    return response.data as ProductDTO[];
  }

  public static async getProductById(id: number | string): Promise<ProductDTO> {
    const response = await ApiHelper.get(`${AppConfig.apiGetProductUrl}/${id}`);
    return response.data as ProductDTO;
  }

  public static async getProductAvailability(id: number | string, date: string): Promise<any> {
    const url = AppConfig.apiGetProductAvailabilityUrl(Number(id), date);
    const response = await ApiHelper.get(url);
    return response.data;
  }

  public static async getMonthlySchedule(id: number | string, year: number, month: number): Promise<any> {
    const url = AppConfig.apiAdminGetProductScheduleUrl(Number(id), year, month);
    const response = await ApiHelper.get(url);
    return response.data;
  }
}
