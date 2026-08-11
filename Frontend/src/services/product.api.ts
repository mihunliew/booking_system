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
}
