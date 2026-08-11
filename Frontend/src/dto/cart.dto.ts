export interface AddToCartRequest {
  productId: number;
  quantity: number;
  bookingDate: string; // ISO format YYYY-MM-DD
}

export interface UpdateCartRequest {
  quantity: number;
  bookingDate: string; // ISO format YYYY-MM-DD
}

export interface CartItemResponse {
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
