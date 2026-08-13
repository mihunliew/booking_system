<template>
  <div class="container page-container">
    <button @click="router.back()" class="btn btn-secondary btn-sm back-btn">
      &larr; Back
    </button>

    <div v-if="loading" class="loading">Loading invoice...</div>

    <div v-else-if="booking" class="invoice-wrapper glass-panel">
      <div class="invoice-header">
        <div class="header-brand">
          <h2>N2N BOOKING INVOICE</h2>
          <p class="booking-no">Order # {{ booking.bookingNo }}</p>
        </div>

        <div class="header-badges">
          <StatusBadge type="booking" :value="booking.status" />
          <StatusBadge type="payment" :value="booking.paymentStatus" />
        </div>
      </div>

      <div class="invoice-meta">
        <div class="meta-box">
          <span class="meta-label">Customer Name</span>
          <span class="meta-value">{{ booking.userFullName || booking.username }}</span>
        </div>
        <div class="meta-box">
          <span class="meta-label">Payment Method</span>
          <span class="meta-value">{{ booking.paymentMethod || 'N/A' }}</span>
        </div>
        <div class="meta-box">
          <span class="meta-label">Created Date</span>
          <span class="meta-value">{{ formatDate(booking.createdAt) }}</span>
        </div>
      </div>

      <div class="items-table-container">
        <table class="custom-table">
          <thead>
            <tr>
              <th>Product / Service</th>
              <th>Booking Date</th>
              <th>Unit Price</th>
              <th>Qty</th>
              <th style="text-align: right;">Subtotal</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in booking.items" :key="item.id">
              <td class="font-bold">{{ item.productName }}</td>
              <td>{{ item.bookingDate }}</td>
              <td>${{ item.price.toFixed(2) }}</td>
              <td>{{ item.quantity }}</td>
              <td style="text-align: right;" class="font-bold">${{ item.subtotal.toFixed(2) }}</td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="invoice-footer">
        <div class="notes-section">
          <p v-if="booking.notes"><strong>Special Notes:</strong> {{ booking.notes }}</p>
        </div>

        <div class="total-summary-box">
          <div class="summary-row">
            <span>Subtotal</span>
            <span>${{ subtotalDisplay.toFixed(2) }}</span>
          </div>

          <div v-if="booking.discountAmount && booking.discountAmount > 0" class="summary-row text-success">
            <span>Discount <small v-if="booking.promoCode">({{ booking.promoCode }})</small></span>
            <span>-${{ booking.discountAmount.toFixed(2) }}</span>
          </div>

          <div class="summary-line grand-total-row">
            <span>Total Payable</span>
            <span class="total-price">${{ booking.totalAmount.toFixed(2) }}</span>
          </div>

          <div class="invoice-actions" v-if="false /* booking.paymentStatus === 'UNPAID' && booking.status !== 'CANCELLED' */">
            <button @click="payNow" class="btn btn-primary btn-full btn-lg" :disabled="submitting">
              {{ submitting ? 'Processing...' : 'Pay Invoice Now' }}
            </button>
            <button @click="cancelOrder" class="btn btn-danger btn-full" :disabled="submitting">
              Cancel Order
            </button>
          </div>
        </div>
      </div>
    </div>

    <Toast ref="toastRef" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { BookingApi, AdminApi } from '../../services'
import type { BookingResponse } from '../../services'
import { isAdmin } from '../../helpers/auth.helper'
import StatusBadge from '../../components/StatusBadge.vue'
import Toast from '../../components/Toast.vue'

const route = useRoute()
const router = useRouter()

const booking = ref<BookingResponse | null>(null)
const loading = ref(true)
const submitting = ref(false)
const toastRef = ref<InstanceType<typeof Toast> | null>(null)

const subtotalDisplay = computed(() => {
  if (!booking.value) return 0
  if (booking.value.subtotalAmount && booking.value.subtotalAmount > 0) {
    return booking.value.subtotalAmount
  }
  return (booking.value.items || []).reduce((acc, item) => acc + (item.subtotal || 0), 0)
})

const fetchBooking = async () => {
  loading.value = true
  try {
    const isAdm = isAdmin() && route.path.includes('/admin')
    const id = route.params.id as string
    
    if (isAdm) {
      booking.value = await AdminApi.getBookingById(id)
    } else {
      booking.value = await BookingApi.getBookingById(id)
    }
  } catch (err) {
    console.error('Failed to load booking:', err)
  } finally {
    loading.value = false
  }
}

const payNow = async () => {
  if (!booking.value) return
  submitting.value = true
  try {
    const updated = await BookingApi.payBooking(booking.value.id, {
      paymentMethod: booking.value.paymentMethod || 'Credit Card'
    })
    booking.value = updated
    toastRef.value?.show('Payment successful!')
  } catch (err) {
    toastRef.value?.show('Payment failed', 'error')
  } finally {
    submitting.value = false
  }
}

const cancelOrder = async () => {
  if (!booking.value) return
  submitting.value = true
  try {
    const updated = await BookingApi.cancelBooking(booking.value.id)
    booking.value = updated
    toastRef.value?.show('Order cancelled')
  } catch (err) {
    toastRef.value?.show('Cancellation failed', 'error')
  } finally {
    submitting.value = false
  }
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString()
}

onMounted(() => {
  fetchBooking()
})
</script>

<style scoped>
.page-container {
  padding-top: 2rem;
}

.back-btn {
  margin-bottom: 1.5rem;
}

.invoice-wrapper {
  padding: 2.5rem;
  max-width: 850px;
  margin: 0 auto;
}

.invoice-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  border-bottom: 1px solid var(--border-glass);
  padding-bottom: 1.5rem;
  margin-bottom: 1.5rem;
}

.header-brand h2 {
  font-size: 1.4rem;
  font-weight: 800;
  color: var(--text-main);
}

.booking-no {
  font-size: 0.9rem;
  color: var(--accent-secondary);
  font-weight: 700;
}

.header-badges {
  display: flex;
  gap: 0.5rem;
}

.invoice-meta {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1.5rem;
  margin-bottom: 2rem;
  background: rgba(255, 255, 255, 0.03);
  padding: 1.25rem;
  border-radius: var(--radius-md);
}

.meta-box {
  display: flex;
  flex-direction: column;
}

.meta-label {
  font-size: 0.75rem;
  color: var(--text-muted);
}

.meta-value {
  font-size: 0.95rem;
  font-weight: 700;
  color: var(--text-main);
}

.items-table-container {
  margin-bottom: 2rem;
}

.font-bold {
  font-weight: 700;
}

.invoice-footer {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  border-top: 1px solid var(--border-glass);
  padding-top: 1.5rem;
}

.notes-section {
  max-width: 400px;
  font-size: 0.85rem;
  color: var(--text-muted);
}

.total-summary-box {
  width: 320px;
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.95rem;
  color: var(--text-muted);
}

.summary-row.text-success {
  color: #34d399;
}

.grand-total-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 800;
  margin-top: 0.4rem;
  padding-top: 0.6rem;
  border-top: 1px solid var(--border-glass);
}

.total-price {
  font-size: 1.5rem;
  color: #38bdf8;
}

.invoice-actions {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  margin-top: 0.75rem;
}
</style>
