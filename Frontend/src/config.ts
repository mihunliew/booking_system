class AppConfig {
  // Use VITE_BACKEND_URL from .env, or fallback to the proxy/localhost
  static backendUrl: string = import.meta.env.VITE_BACKEND_URL || "http://localhost:8080/api";

  // Global Info
  static currencyMark: string = "RM";
  static companyName: string = 'N2N Booking System';
  static phoneNo: string = '+6012 345 6789';
  static email: string = 'support@n2n.com.my';
  static copyRight: string = "© All Rights Reserved by N2N Booking System";

  // Auth URLs
  static apiLoginUrl: string = `${this.backendUrl}/auth/login`;
  static apiRegisterUrl: string = `${this.backendUrl}/auth/signup`;
  static apiProfileUrl: string = `${this.backendUrl}/auth/me`;

  // Product URLs
  static apiGetProductUrl: string = `${this.backendUrl}/products`;
  static apiGetProductAvailabilityUrl = (id: number, date: string) => `${this.backendUrl}/products/${id}/availability?date=${date}`;

  // Cart URLs
  static apiGetCartUrl: string = `${this.backendUrl}/cart`;
  static apiAddToCartUrl: string = `${this.backendUrl}/cart/items`;
  static apiCheckoutUrl: string = `${this.backendUrl}/bookings/checkout`;

  // Booking URLs
  static apiGetBookingUrl: string = `${this.backendUrl}/bookings`;

  // Admin URLs
  static apiAdminUsersUrl: string = `${this.backendUrl}/admin/users`;
  static apiAdminProductsUrl: string = `${this.backendUrl}/admin/products`;
  static apiAdminGetProductScheduleUrl = (id: number, year: number, month: number) => `${this.backendUrl}/admin/products/${id}/schedule?year=${year}&month=${month}`;
  static apiAdminBookingsUrl: string = `${this.backendUrl}/admin/bookings`;
  static apiAdminDashboardUrl: string = `${this.backendUrl}/admin/dashboard`;
  static apiAdminRolesUrl: string = `${this.backendUrl}/admin/roles`;
  static apiAdminSettingsUrl: string = `${this.backendUrl}/admin/settings`;
  static apiAdminPaymentsUrl: string = `${this.backendUrl}/admin/payments`;

  // Public Settings
  static apiSettingsPaymentMethodsUrl: string = `${this.backendUrl}/settings/payment-methods`;

  // Promo Code URLs
  static apiApplyPromoUrl: string = `${this.backendUrl}/promocodes/apply`;
  static apiAdminPromoCodesUrl: string = `${this.backendUrl}/admin/promocodes`;
}

export default AppConfig;
