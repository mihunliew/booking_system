import AppConfig from '@/config';
import type { AddToCartRequest, UpdateCartRequest, CartItemResponse } from '@/dto/cart.dto';
import ApiHelper from '@/helpers/api_helper';

export default class CartApi {
  public static async getCart(): Promise<CartItemResponse[]> {
    const response = await ApiHelper.get(AppConfig.apiGetCartUrl);
    return response.data as CartItemResponse[];
  }

  public static async addToCart(data: AddToCartRequest): Promise<CartItemResponse> {
    const response = await ApiHelper.post(`${AppConfig.apiGetCartUrl}/add`, undefined, data);
    return response.data as CartItemResponse;
  }

  public static async updateCartItem(id: number | string, data: UpdateCartRequest): Promise<CartItemResponse> {
    const response = await ApiHelper.put(`${AppConfig.apiGetCartUrl}/${id}`, undefined, data);
    return response.data as CartItemResponse;
  }

  public static async removeCartItem(id: number | string): Promise<void> {
    await ApiHelper.delete(`${AppConfig.apiGetCartUrl}/${id}`);
  }

  public static async clearCart(): Promise<void> {
    await ApiHelper.delete(`${AppConfig.apiGetCartUrl}/clear`);
  }
}
