<template>
  <div class="container page-container">
    <h1 class="page-title">Checkout & Booking Confirmation</h1>
    <p class="page-subtitle">Select your payment method to finalize booking</p>

    <div v-if="loading" class="loading">Preparing checkout summary...</div>

    <div v-else-if="!summary || !summary.items || summary.items.length === 0" class="glass-panel empty-checkout">
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
          {{ submitting ? 'Processing Order...' : `Confirm & Book ($${summary.totalAmount.toFixed(2)})` }}
        </button>
      </div>

      <div class="items-summary glass-panel">
        <h2>Booking Summary</h2>
        <div class="summary-item-list">
          <div v-for="item in summary.items" :key="item.id" class="summary-item">
            <div class="summary-item-info">
              <span class="summary-name">{{ item.productName }}</span>
              <span class="summary-meta">Date: {{ item.bookingDate }} | Qty: {{ item.quantity }}</span>
            </div>
            <span class="summary-price">${{ item.subtotal.toFixed(2) }}</span>
          </div>
        </div>

        <!-- Promo Code Input / Applied Badge Section -->
        <div class="promo-section">
          <label class="form-label">Promo Code</label>
          <div v-if="!summary.promoCode" class="promo-input-group">
            <input
              type="text"
              v-model="promoInput"
              placeholder="Enter promo code (e.g. SUMMER10)"
              class="form-input promo-input"
              @keyup.enter="applyPromoCode"
              :disabled="applyingPromo"
            />
            <button @click="applyPromoCode" class="btn btn-secondary" :disabled="applyingPromo || !promoInput.trim()">
              {{ applyingPromo ? 'Checking...' : 'Apply' }}
            </button>
          </div>

          <div v-else class="applied-promo-box glass-card">
            <div class="promo-badge-info">
              <span class="promo-tag">🏷️ {{ summary.promoCode }}</span>
              <span class="promo-desc">
                {{ summary.discountType === 'PERCENTAGE' ? summary.discountValue + '% Off' : '$' + summary.discountValue + ' Off' }}
                (Locked 5m)
              </span>
            </div>
            <button @click="removePromoCode" class="btn btn-danger btn-sm" :disabled="applyingPromo">
              Remove
            </button>
          </div>
        </div>

        <!-- Price Breakdown -->
        <div class="breakdown-section">
          <div class="breakdown-row">
            <span>Subtotal</span>
            <span>${{ summary.subtotal.toFixed(2) }}</span>
          </div>

          <div v-if="summary.discountAmount > 0" class="breakdown-row discount-row">
            <span>Discount ({{ summary.promoCode }})</span>
            <span class="discount-val">-${{ summary.discountAmount.toFixed(2) }}</span>
          </div>

          <div class="total-bar">
            <span>Total Payable</span>
            <span class="total-price">${{ summary.totalAmount.toFixed(2) }}</span>
          </div>
        </div>
      </div>
    </div>

    <Toast ref="toastRef" />

    <!-- Persistent Out of Stock / Availability Error Dialog -->
    <DialogPlus
      :is-open="showErrorDialog"
      title="Slot Availability Alert"
      continue-text="Okay"
      :continue-action="handleDialogOkay"
      :close-on-backdrop="false"
      @close="handleDialogOkay"
    >
      <div class="availability-error-body">
        <p class="error-msg-text">{{ errorMessage }}</p>
      </div>
    </DialogPlus>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { BookingApi, SettingApi, PromoCodeApi } from '../../services'
import type { BookingSummaryResponse, SettingResponse } from '../../services'
import Toast from '../../components/Toast.vue'
import DialogPlus from '../../components/DialogPlus.vue'
import { extractErrorMessage } from '../../helpers/error.helper'

const router = useRouter()
const summary = ref<BookingSummaryResponse | null>(null)
const loading = ref(true)
const submitting = ref(false)
const applyingPromo = ref(false)
const promoInput = ref('')
const toastRef = ref<InstanceType<typeof Toast> | null>(null)

const showErrorDialog = ref(false)
const errorMessage = ref('')

const handleDialogOkay = () => {
  showErrorDialog.value = false
  router.push('/cart')
}

const selectedMethod = ref<number | null>(null)
const notes = ref('')

const paymentMethods = ref<SettingResponse[]>([])

const fetchCheckoutData = async () => {
  loading.value = true
  try {
    const [summaryData, settingsData] = await Promise.all([
      BookingApi.getCheckoutSummary(),
      SettingApi.getActivePaymentMethods()
    ])
    summary.value = summaryData
    paymentMethods.value = settingsData
    if (paymentMethods.value.length > 0) {
      selectedMethod.value = paymentMethods.value[0].id
    }
  } catch (err: any) {
    console.error('Failed to load checkout summary:', err)
    const msg = extractErrorMessage(err, 'Failed to load checkout summary')
    errorMessage.value = msg
    showErrorDialog.value = true
  } finally {
    loading.value = false
  }
}

const applyPromoCode = async () => {
  if (!promoInput.value.trim()) return
  applyingPromo.value = true
  try {
    const result = await PromoCodeApi.applyPromoCode({ code: promoInput.value })
    summary.value = result
    if (result.valid) {
      toastRef.value?.show(result.message || 'Promo code applied successfully!')
      promoInput.value = ''
    } else {
      toastRef.value?.show(result.message || 'Invalid promo code', 'error')
    }
  } catch (err: any) {
    const errorMsg = extractErrorMessage(err, 'Failed to apply promo code')
    toastRef.value?.show(errorMsg, 'error')
  } finally {
    applyingPromo.value = false
  }
}

const removePromoCode = async () => {
  applyingPromo.value = true
  try {
    const result = await PromoCodeApi.applyPromoCode({
      code: null,
      reservationToken: summary.value?.reservationToken
    })
    summary.value = result
    toastRef.value?.show('Promo code removed')
  } catch (err: any) {
    toastRef.value?.show(extractErrorMessage(err, 'Failed to remove promo code'), 'error')
  } finally {
    applyingPromo.value = false
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
      promoCode: summary.value?.promoCode,
      reservationToken: summary.value?.reservationToken,
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
    const errorMsg = extractErrorMessage(err, 'Checkout failed')
    errorMessage.value = errorMsg
    showErrorDialog.value = true
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchCheckoutData()
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
  grid-template-columns: 1fr 400px;
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

.promo-section {
  border-top: 1px solid var(--border-glass);
  padding-top: 1.25rem;
  margin-top: 1.25rem;
  margin-bottom: 1.25rem;
}

.promo-input-group {
  display: flex;
  gap: 0.5rem;
  margin-top: 0.5rem;
}

.promo-input {
  flex: 1;
  text-transform: uppercase;
}

.applied-promo-box {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.85rem 1rem;
  margin-top: 0.5rem;
  background: var(--bg-surface-elevated);
  border-color: var(--status-completed);
}

.promo-badge-info {
  display: flex;
  flex-direction: column;
}

.promo-tag {
  font-weight: 800;
  color: var(--status-completed);
  font-size: 0.95rem;
}

.promo-desc {
  font-size: 0.75rem;
  color: var(--text-muted);
}

.breakdown-section {
  border-top: 1px solid var(--border-glass);
  padding-top: 1rem;
}

.breakdown-row {
  display: flex;
  justify-content: space-between;
  font-size: 0.9rem;
  color: var(--text-muted);
  margin-bottom: 0.5rem;
}

.discount-row {
  color: var(--status-completed);
  font-weight: 700;
}

.discount-val {
  color: var(--status-completed);
}

.total-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1px dashed var(--border-glass);
  padding-top: 0.85rem;
  margin-top: 0.85rem;
  font-weight: 800;
  font-size: 1.1rem;
}

.total-price {
  font-size: 1.5rem;
  color: #38bdf8;
}

.availability-error-body {
  padding: 1rem 0;
  text-align: center;
}

.error-msg-text {
  font-size: 1.05rem;
  color: var(--text-main, #ffffff);
  line-height: 1.6;
}
</style>
