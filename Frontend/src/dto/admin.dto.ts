import { BookingResponse } from './booking.dto'

export interface AdminDashboardDTO {
  totalUsers: number;
  totalProducts: number;
  totalBookings: number;
  pendingBookings: number;
  totalRevenue: number;
  recentBookings: BookingResponse[];
}
