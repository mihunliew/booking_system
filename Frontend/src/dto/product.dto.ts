export interface ProductDTO {
  id: number;
  name: string;
  description?: string;
  price: number;
  category: string;
  capacity: number;
  stockQuantity?: number;
  imageUrl?: string;
  status?: string;
}

export interface DayScheduleDTO {
  date: string;
  stockQuantity: number;
  bookedCount: number;
  heldCount: number;
  availableSlots: number;
  soldOut: boolean;
}

export interface ProductMonthlyScheduleResponse {
  productId: number;
  productName: string;
  year: number;
  month: number;
  totalStockQuantity: number;
  days: DayScheduleDTO[];
}
