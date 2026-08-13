export interface ProductAvailabilityResponse {
  productId: number
  bookingDate: string
  capacity: number
  stockQuantity: number
  bookedCount: number
  heldCount: number
  availableSlots: number
}
