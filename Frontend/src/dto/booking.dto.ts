import { BookingStatus, PaymentStatus } from '@/constants/enums'

export interface CheckoutRequest {
  paymentSettingId: number;
  notes?: string;
}

export interface PayBookingRequest {
  paymentMethod: string;
}

export interface UpdateStatusRequest {
  status: BookingStatus;
  paymentStatus: PaymentStatus;
}

export interface BookingItemResponse {
  id: number;
  productId: number;
  productName: string;
  price: number;
  quantity: number;
  bookingDate: string;
  subtotal: number;
}

export interface BookingResponse {
  id: number;
  bookingNo: string;
  userId: number;
  username: string;
  userFullName: string;
  totalAmount: number;
  status: BookingStatus;
  paymentStatus: PaymentStatus;
  paymentMethod?: string;
  notes?: string;
  createdAt: string;
  checkoutUrl?: string;
  stripePaymentIntentId?: string;
  amountPaid?: number;
  items: BookingItemResponse[];
}
