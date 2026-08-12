export enum DiscountType {
  PERCENTAGE = 'PERCENTAGE',
  FIXED_AMOUNT = 'FIXED_AMOUNT'
}

export interface SummaryItemResponse {
  id: number;
  productId: number;
  productName: string;
  productCategory: string;
  imageUrl?: string;
  unitPrice: number;
  quantity: number;
  bookingDate: string;
  subtotal: number;
}

export interface BookingSummaryResponse {
  valid: boolean;
  message?: string;
  promoCode?: string;
  reservationToken?: string;
  discountType?: DiscountType;
  discountValue?: number;
  subtotal: number;
  discountAmount: number;
  totalAmount: number;
  items: SummaryItemResponse[];
}

export interface ApplyPromoRequest {
  code?: string | null;
  reservationToken?: string | null;
}

export interface PromoCodeRequest {
  code: string;
  discountType: DiscountType;
  discountValue: number;
  minSpend?: number;
  maxDiscount?: number;
  usageLimit?: number;
  startDate?: string;
  endDate?: string;
  isActive?: boolean;
}

export interface PromoCodeResponse {
  id: number;
  code: string;
  discountType: DiscountType;
  discountValue: number;
  minSpend?: number;
  maxDiscount?: number;
  usageLimit?: number;
  usedCount: number;
  startDate?: string;
  endDate?: string;
  isActive: boolean;
  createdAt: string;
}
