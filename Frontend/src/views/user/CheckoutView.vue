<template>
  <div class="container page-container">
    <h1 class="page-title">Checkout & Booking Confirmation</h1>
    <p class="page-subtitle">Select your payment method to finalize booking</p>

    <div v-if="loading" class="loading">Preparing checkout...</div>

    <div v-else-if="cartItems.length === 0" class="glass-panel empty-checkout">
      <p>No items in cart to checkout.</p>
      <router-link to="/" class="btn btn-primary" style="margin-top: 1rem;">Go to Home</router-link>
    </div>

    <div v-else class="checkout-layout">
      <div class="checkout-form-container glass-panel">
        <h2>Payment Details</h2>

        <div class="payment-methods">
          <label
            v-for="method in paymentMethods"
            :key="method.id"
            class="payment-card glass-card"
            :class="{ active: selectedMethod === method.id }"
          >
            <input type="radio" v-model="selectedMethod" :value="method.id" class="hidden-radio" />
            <div class="method-icon">{{ method.icon }}</div>
            <div class="method-info">
              <span class="method-name">{{ method.name }}</span>
              <span class="method-desc">{{ method.description }}</span>
            </div>
          </label>
        </div>

        <div class="form-group" style="margin-top: 1.5rem;">
          <label class="form-label">Special Notes / Requests (Optional)</label>
          <textarea v-model="notes" class="form-textarea" rows="3" placeholder="e.g., Equipment setup preferences, arrival times..."></textarea>
        </div>

        <button @click="confirmCheckout" class="btn btn-primary btn-full btn-lg" :disabled="submitting">
          {{ submitting ? 'Processing Order...' : `Confirm & Book ($${totalSubtotal.toFixed(2)})` }}
        </button>
      </div>

      <div class="items-summary glass-panel">
        <h2>Booking Summary</h2>
        <div class="summary-item-list">
          <div v-for="item in cartItems" :key="item.id" class="summary-item">
            <div class="summary-item-info">
              <span class="summary-name">{{ item.productName }}</span>
              <span class="summary-meta">Date: {{ item.bookingDate }} | Qty: {{ item.quantity }}</span>
            </div>
            <span class="summary-price">${{ item.subtotal.toFixed(2) }}</span>
          </div>
        </div>

        <div class="total-bar">
          <span>Total Payable</span>
          <span class="total-price">${{ totalSubtotal.toFixed(2) }}</span>
        </div>
      </div>
    </div>

    <Toast ref="toastRef" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { CartApi, BookingApi, SettingApi } from '../../services'
import type { CartItemResponse } from '../../services'
import type { SettingResponse } from '../../services'
import Toast from '../../components/Toast.vue'

const router = useRouter()
const cartItems = ref<CartItemResponse[]>([])
const loading = ref(true)
const submitting = ref(false)
const toastRef = ref<InstanceType<typeof Toast> | null>(null)

const selectedMethod = ref<number | null>(null)
const notes = ref('')

const paymentMethods = ref<SettingResponse[]>([])

const totalSubtotal = computed(() => {
  return cartItems.value.reduce((acc, item) => acc + (item.subtotal || 0), 0)
})

const fetchCartAndSettings = async () => {
  loading.value = true
  try {
    const [cartData, settingsData] = await Promise.all([
      CartApi.getCart(),
      SettingApi.getActivePaymentMethods()
    ])
    cartItems.value = cartData
    paymentMethods.value = settingsData
    if (paymentMethods.value.length > 0) {
      selectedMethod.value = paymentMethods.value[0].id
    }
  } catch (err) {
    console.error('Failed to load checkout data:', err)
  } finally {
    loading.value = false
  }
}

const confirmCheckout = async () => {
  if (!selectedMethod.value) {
    toastRef.value?.show('Please select a payment method', 'error')
    return
  }
  submitting.value = true
  try {
    const booking = await BookingApi.checkout({
      paymentSettingId: selectedMethod.value,
      notes: notes.value
    })
    
    if (booking.checkoutUrl) {
      toastRef.value?.show('Redirecting to payment...')
      setTimeout(() => {
        window.location.href = booking.checkoutUrl!
      }, 800)
    } else {
      toastRef.value?.show('Booking created successfully!')
      setTimeout(() => {
        router.push(`/bookings/${booking.id}`)
      }, 800)
    }
  } catch (err: any) {
    toastRef.value?.show(err.response?.data?.message || 'Checkout failed', 'error')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchCartAndSettings()
})
</script>

<style scoped>
.page-container {
  padding-top: 2rem;
}

.page-title {
  font-size: 2rem;
  font-weight: 800;
}

.page-subtitle {
  color: var(--text-muted);
  margin-bottom: 2rem;
}

.checkout-layout {
  display: grid;
  grid-template-columns: 1fr 380px;
  gap: 2rem;
}

.checkout-form-container {
  padding: 2rem;
}

.checkout-form-container h2 {
  font-size: 1.25rem;
  margin-bottom: 1.5rem;
}

.payment-methods {
  display: flex;
  flex-direction: column;
  gap: 0.85rem;
}

.payment-card {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem;
  cursor: pointer;
  border: 1px solid var(--border-glass);
}

.payment-card.active {
  border-color: var(--accent-primary);
  background: var(--bg-surface-elevated);
  box-shadow: 0 0 15px var(--shadow-light);
}

.hidden-radio {
  display: none;
}

.method-icon {
  font-size: 1.5rem;
}

.method-info {
  display: flex;
  flex-direction: column;
}

.method-name {
  font-weight: 700;
  color: var(--text-main);
  font-size: 0.95rem;
}

.method-desc {
  font-size: 0.8rem;
  color: var(--text-muted);
}

.items-summary {
  padding: 1.75rem;
  height: fit-content;
}

.items-summary h2 {
  font-size: 1.25rem;
  margin-bottom: 1.25rem;
  border-bottom: 1px solid var(--border-glass);
  padding-bottom: 0.75rem;
}

.summary-item-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.875rem;
}

.summary-item-info {
  display: flex;
  flex-direction: column;
}

.summary-name {
  font-weight: 600;
  color: var(--text-main);
}

.summary-meta {
  font-size: 0.75rem;
  color: var(--text-muted);
}

.summary-price {
  font-weight: 700;
  color: #38bdf8;
}

.total-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1px solid var(--border-glass);
  padding-top: 1rem;
  font-weight: 800;
  font-size: 1.1rem;
}

.total-price {
  font-size: 1.5rem;
  color: #38bdf8;
}
</style>
