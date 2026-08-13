import { PaymentStatus } from '@/constants/enums';

export interface PaymentResponse {
  id: number;
  bookingNo: string;
  userId: number;
  username: string;
  userFullName: string;
  userEmail: string;
  totalAmount: number;
  amountPaid: number;
  refundedAmount: number;
  paymentStatus: PaymentStatus;
  paymentMethod: string;
  stripePaymentIntentId?: string;
  createdAt: string;
  updatedAt: string;
}

export interface PaymentSummaryResponse {
  totalRevenue: number;
  paidCount: number;
  pendingCount: number;
  failedCount: number;
  refundedCount: number;
  partiallyRefundedCount: number;
}

export interface RefundRequest {
  amount: number;
  reason?: string;
}
