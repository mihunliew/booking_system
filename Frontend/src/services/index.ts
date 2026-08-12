import AuthApi from './auth.api'
import AdminApi from './admin.api'
import BookingApi from './booking.api'
import ProductApi from './product.api'
import CartApi from './cart.api'
import SettingApi from './setting.api'
import PromoCodeApi from './promocode.api'

export {
  AuthApi,
  AdminApi,
  BookingApi,
  ProductApi,
  CartApi,
  SettingApi,
  PromoCodeApi
}

// Re-export all DTOs for convenience
export * from '@/dto/auth.dto'
export * from '@/dto/admin.dto'
export * from '@/dto/booking.dto'
export * from '@/dto/product.dto'
export * from '@/dto/cart.dto'
export * from '@/dto/setting.dto'
export * from '@/dto/promocode.dto'
