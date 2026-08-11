<template>
  <div class="container page-container">
    <div class="header-section">
      <h1 class="page-title">My Booking Orders</h1>
      <p class="page-subtitle">Track status, payment history, and booking receipts</p>
    </div>

    <div v-if="loading" class="loading">Loading your bookings...</div>

    <div v-else-if="bookings.length === 0" class="glass-panel empty-bookings">
      <h2>No Bookings Found</h2>
      <p>You haven't placed any bookings yet.</p>
      <router-link to="/" class="btn btn-primary" style="margin-top: 1rem;">Explore Products</router-link>
    </div>

    <div v-else class="bookings-list">
      <div v-for="booking in bookings" :key="booking.id" class="glass-panel booking-card">
        <div class="booking-card-header">
          <div class="header-left">
            <span class="booking-no">{{ booking.bookingNo }}</span>
            <span class="booking-date">Created on {{ formatDate(booking.createdAt) }}</span>
          </div>

          <div class="header-badges">
            <StatusBadge type="booking" :value="booking.status" />
            <StatusBadge type="payment" :value="booking.paymentStatus" />
          </div>
        </div>

        <div class="booking-card-body">
          <div class="booking-items-mini">
            <div v-for="item in booking.items" :key="item.id" class="mini-item">
              <span class="item-name">{{ item.productName }}</span>
              <span class="item-meta">Date: {{ item.bookingDate }} | Qty: {{ item.quantity }}</span>
              <span class="item-price">${{ item.subtotal.toFixed(2) }}</span>
            </div>
          </div>
        </div>

        <div class="booking-card-footer">
          <div class="total-info">
            <span>Total Amount:</span>
            <span class="amount-text">${{ booking.totalAmount.toFixed(2) }}</span>
          </div>

          <div class="action-buttons">
            <router-link :to="`/bookings/${booking.id}`" class="btn btn-secondary btn-sm">
              View Invoice & Payment
            </router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { BookingApi } from '../../services'
import type { BookingResponse } from '../../services'
import StatusBadge from '../../components/StatusBadge.vue'

const bookings = ref<BookingResponse[]>([])
const loading = ref(true)

const fetchBookings = async () => {
  loading.value = true
  try {
    bookings.value = await BookingApi.getUserBookings()
  } catch (err) {
    console.error('Failed to load bookings:', err)
  } finally {
    loading.value = false
  }
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  })
}

onMounted(() => {
  fetchBookings()
})
</script>

<style scoped>
.page-container {
  padding-top: 2rem;
}

.header-section {
  margin-bottom: 2rem;
}

.page-title {
  font-size: 2rem;
  font-weight: 800;
}

.page-subtitle {
  color: var(--text-muted);
}

.empty-bookings {
  padding: 4rem;
  text-align: center;
}

.bookings-list {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.booking-card {
  padding: 1.5rem;
}

.booking-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid var(--border-glass);
  padding-bottom: 1rem;
  margin-bottom: 1rem;
}

.header-left {
  display: flex;
  flex-direction: column;
}

.booking-no {
  font-size: 1.1rem;
  font-weight: 800;
  color: var(--text-main);
}

.booking-date {
  font-size: 0.8rem;
  color: var(--text-muted);
}

.header-badges {
  display: flex;
  gap: 0.5rem;
}

.booking-items-mini {
  display: flex;
  flex-direction: column;
  gap: 0.65rem;
  margin-bottom: 1.25rem;
}

.mini-item {
  display: flex;
  justify-content: space-between;
  font-size: 0.9rem;
  color: var(--text-muted);
}

.item-name {
  font-weight: 600;
  color: var(--text-main);
}

.item-price {
  font-weight: 700;
  color: #38bdf8;
}

.booking-card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1px solid var(--border-glass);
  padding-top: 1rem;
}

.total-info {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-weight: 700;
}

.amount-text {
  font-size: 1.25rem;
  color: #38bdf8;
}

.action-buttons {
  display: flex;
  gap: 0.75rem;
}
</style>
