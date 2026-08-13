import AppConfig from '@/config';
import ApiHelper from '@/helpers/api_helper';
import type { PaymentResponse, PaymentSummaryResponse, RefundRequest } from '@/dto/payment.dto';
import { PaymentStatus } from '@/constants/enums';

export default class PaymentApi {
  public static async getAllPayments(status?: PaymentStatus): Promise<PaymentResponse[]> {
    let url = AppConfig.apiAdminPaymentsUrl;
    if (status) {
      url += `?status=${encodeURIComponent(status)}`;
    }
    const response = await ApiHelper.get(url);
    return response.data as PaymentResponse[];
  }

  public static async getPaymentById(id: number | string): Promise<PaymentResponse> {
    const response = await ApiHelper.get(`${AppConfig.apiAdminPaymentsUrl}/${id}`);
    return response.data as PaymentResponse;
  }

  public static async getPaymentSummary(): Promise<PaymentSummaryResponse> {
    const response = await ApiHelper.get(`${AppConfig.apiAdminPaymentsUrl}/summary`);
    return response.data as PaymentSummaryResponse;
  }

  public static async processRefund(id: number | string, data: RefundRequest): Promise<PaymentResponse> {
    const response = await ApiHelper.post(`${AppConfig.apiAdminPaymentsUrl}/${id}/refund`, undefined, data);
    return response.data as PaymentResponse;
  }
}
