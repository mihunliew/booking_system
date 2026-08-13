import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import { isAuthenticated, isAdmin } from '../helpers/auth.helper'

import LoginView from '../views/auth/LoginView.vue'
import SignupView from '../views/auth/SignupView.vue'

import ProductListView from '../views/user/ProductListView.vue'
import ProductDetailView from '../views/user/ProductDetailView.vue'
import CartView from '../views/user/CartView.vue'
import CheckoutView from '../views/user/CheckoutView.vue'
import BookingListView from '../views/user/BookingListView.vue'
import BookingDetailView from '../views/user/BookingDetailView.vue'

import UserLayout from '../layouts/UserLayout.vue'
import AdminLayout from '../layouts/AdminLayout.vue'

import AdminDashboardView from '../views/admin/AdminDashboardView.vue'
import AdminUserView from '../views/admin/AdminUserView.vue'
import AdminProductView from '../views/admin/AdminProductView.vue'
import AdminBookingView from '../views/admin/AdminBookingView.vue'
import AdminRoleView from '../views/admin/AdminRoleView.vue'
import AdminSettingView from '../views/admin/AdminSettingView.vue'
import AdminPromoCodeView from '../views/admin/AdminPromoCodeView.vue'
import AdminPaymentView from '../views/admin/AdminPaymentView.vue'
import { isSuperAdmin } from '../helpers/auth.helper'

const routes: Array<RouteRecordRaw> = [
  {
    path: '/',
    component: UserLayout,
    children: [
      { path: '', name: 'Home', component: ProductListView },
      { path: 'products/:id', name: 'ProductDetail', component: ProductDetailView },
      { path: 'login', name: 'Login', component: LoginView },
      { path: 'signup', name: 'Signup', component: SignupView },
      { path: 'cart', name: 'Cart', component: CartView, meta: { requiresAuth: true } },
      { path: 'checkout', name: 'Checkout', component: CheckoutView, meta: { requiresAuth: true } },
      { path: 'bookings', name: 'BookingList', component: BookingListView, meta: { requiresAuth: true } },
      { path: 'bookings/:id', name: 'BookingDetail', component: BookingDetailView, meta: { requiresAuth: true } },
    ]
  },
  {
    path: '/admin',
    component: AdminLayout,
    meta: { requiresAdmin: true },
    children: [
      { path: '', name: 'AdminDashboard', component: AdminDashboardView },
      { path: 'users', name: 'AdminUsers', component: AdminUserView },
      { path: 'products', name: 'AdminProducts', component: AdminProductView },
      { path: 'bookings', name: 'AdminBookings', component: AdminBookingView },
      { path: 'bookings/:id', name: 'AdminBookingDetail', component: BookingDetailView },
      { path: 'payments', name: 'AdminPayments', component: AdminPaymentView },
      { path: 'promocodes', name: 'AdminPromoCodes', component: AdminPromoCodeView },
      { path: 'roles', name: 'AdminRoles', component: AdminRoleView },
      { path: 'settings', name: 'AdminSettings', component: AdminSettingView, meta: { requiresSuperAdmin: true } }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  if (to.meta.requiresAuth && !isAuthenticated()) {
    return next({ name: 'Login' })
  }
  if (to.meta.requiresAdmin && (!isAuthenticated() || !isAdmin())) {
    return next({ name: 'Home' })
  }
  if (to.meta.requiresSuperAdmin && (!isAuthenticated() || !isSuperAdmin())) {
    return next({ name: 'AdminDashboard' })
  }
  next()
})

export default router
