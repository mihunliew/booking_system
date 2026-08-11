import AppConfig from '@/config';
import type { CheckoutRequest, PayBookingRequest, BookingResponse } from '@/dto/booking.dto';
import ApiHelper from '@/helpers/api_helper';

export default class BookingApi {
  public static async checkout(data: CheckoutRequest): Promise<BookingResponse> {
    const response = await ApiHelper.post(AppConfig.apiCheckoutUrl, undefined, data);
    return response.data as BookingResponse;
  }

  public static async getUserBookings(): Promise<BookingResponse[]> {
    const response = await ApiHelper.get(AppConfig.apiGetBookingUrl);
    return response.data as BookingResponse[];
  }

  public static async getBookingById(id: number | string): Promise<BookingResponse> {
    const response = await ApiHelper.get(`${AppConfig.apiGetBookingUrl}/${id}`);
    return response.data as BookingResponse;
  }

  public static async payBooking(id: number | string, data: PayBookingRequest): Promise<BookingResponse> {
    const response = await ApiHelper.post(`${AppConfig.apiGetBookingUrl}/${id}/pay`, undefined, data);
    return response.data as BookingResponse;
  }

  public static async cancelBooking(id: number | string): Promise<BookingResponse> {
    const response = await ApiHelper.post(`${AppConfig.apiGetBookingUrl}/${id}/cancel`);
    return response.data as BookingResponse;
  }
}
