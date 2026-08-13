<template>
  <div class="admin-payments-page">
    <div class="page-header">
      <h1 class="page-title">Payments & Financial Transactions</h1>
      <p class="page-subtitle">Audit customer payments, monitor revenue streams, and process full or partial refunds</p>
    </div>

    <!-- Summary Metrics -->
    <div class="metrics-grid">
      <div class="glass-panel metric-card">
        <div class="metric-icon revenue">💰</div>
        <div class="metric-info">
          <div class="metric-label">Total Revenue</div>
          <div class="metric-value">${{ (summary.totalRevenue || 0).toFixed(2) }}</div>
        </div>
      </div>
      <div class="glass-panel metric-card">
        <div class="metric-icon paid">✅</div>
        <div class="metric-info">
          <div class="metric-label">Paid (Successful)</div>
          <div class="metric-value">{{ summary.paidCount || 0 }}</div>
        </div>
      </div>
      <div class="glass-panel metric-card">
        <div class="metric-icon pending">⏳</div>
        <div class="metric-info">
          <div class="metric-label">Pending / Unpaid</div>
          <div class="metric-value">{{ summary.pendingCount || 0 }}</div>
        </div>
      </div>
      <div class="glass-panel metric-card">
        <div class="metric-icon refund">🔄</div>
        <div class="metric-info">
          <div class="metric-label">Refunded / Partial</div>
          <div class="metric-value">{{ (summary.refundedCount || 0) + (summary.partiallyRefundedCount || 0) }}</div>
        </div>
      </div>
    </div>

    <!-- Main Content Area -->
    <div class="glass-panel content-card">
      <div class="controls-bar">
        <!-- Status Tabs -->
        <div class="status-tabs">
          <button 
            v-for="tab in filterTabs" 
            :key="tab.value" 
            class="tab-btn" 
            :class="{ active: activeTab === tab.value }"
            @click="selectTab(tab.value)"
          >
            {{ tab.label }}
            <span class="tab-badge" v-if="tab.count !== undefined">{{ tab.count }}</span>
          </button>
        </div>

        <!-- Search Input -->
        <div class="search-wrapper">
          <input 
            v-model="searchQuery" 
            type="text" 
            class="form-input search-input" 
            placeholder="Search booking #, customer, email..." 
          />
        </div>
      </div>

      <div v-if="loading" class="loading">Loading financial transactions...</div>

      <div v-else class="table-container">
        <table class="custom-table">
          <thead>
            <tr>
              <th>Booking</th>
              <th>Customer</th>
              <th>Amount</th>
              <th>Payment</th>
              <th>Status</th>
              <th>Date</th>
              <th style="text-align: right;">Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="filteredPayments.length === 0">
              <td colspan="7" class="empty-state">No payment records found matching the criteria.</td>
            </tr>
            <tr v-for="p in filteredPayments" :key="p.id">
              <td class="font-bold">
                <router-link :to="`/admin/bookings/${p.id}`" class="booking-link">
                  #{{ p.bookingNo }}
                </router-link>
              </td>
              <td>
                <div class="customer-info">
                  <span class="customer-name">{{ p.userFullName || p.username }}</span>
                  <span class="customer-email">{{ p.userEmail }}</span>
                </div>
              </td>
              <td class="font-bold">
                <div class="amount-group">
                  <span>${{ p.totalAmount.toFixed(2) }}</span>
                  <span v-if="p.refundedAmount > 0" class="refund-subtext">
                    (Refunded: ${{ p.refundedAmount.toFixed(2) }})
                  </span>
                </div>
              </td>
              <td>
                <span class="payment-method-badge">
                  {{ formatPaymentMethod(p.paymentMethod) }}
                </span>
              </td>
              <td><StatusBadge type="payment" :value="p.paymentStatus" /></td>
              <td class="date-col">{{ formatDate(p.createdAt) }}</td>
              <td style="text-align: right;">
                <div class="action-btn-group">
                  <button @click="openInspectModal(p)" class="btn btn-secondary btn-sm">
                    Inspect
                  </button>
                  <button 
                    v-if="hasPermission('PAYMENTS', 'UPDATE') && canRefund(p)" 
                    @click="openRefundModal(p)" 
                    class="btn btn-warning btn-sm"
                  >
                    Refund
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Refund Dialog -->
    <DialogPlus 
      :isOpen="isRefundModalOpen" 
      title="Process Payment Refund" 
      cancelText="Cancel"
      :cancelAction="() => isRefundModalOpen = false"
      continueText="Confirm Refund"
      :continueAction="submitRefund"
      :submitting="submitting"
      :continueDisabled="refundForm.amount <= 0 || refundForm.amount > maxRefundable"
      @close="isRefundModalOpen = false"
    >
      <div v-if="selectedPayment">
        <div class="modal-info-box">
          <p><strong>Booking #:</strong> #{{ selectedPayment.bookingNo }}</p>
          <p><strong>Customer:</strong> {{ selectedPayment.userFullName }} ({{ selectedPayment.userEmail }})</p>
          <p><strong>Total Amount:</strong> ${{ selectedPayment.totalAmount.toFixed(2) }}</p>
          <p v-if="selectedPayment.refundedAmount > 0" class="text-warning">
            <strong>Previously Refunded:</strong> ${{ selectedPayment.refundedAmount.toFixed(2) }}
          </p>
          <p><strong>Max Refundable:</strong> ${{ maxRefundable.toFixed(2) }}</p>
        </div>

        <div class="form-group" style="margin-top: 1.25rem;">
          <label class="form-label">Refund Amount ($)</label>
          <input 
            v-model.number="refundForm.amount" 
            type="number" 
            step="0.01" 
            :max="maxRefundable" 
            min="0.01" 
            class="form-input" 
            placeholder="0.00" 
          />
          <div class="quick-amount-buttons">
            <button @click="refundForm.amount = maxRefundable" class="btn btn-secondary btn-xs">
              Full Remaining (${{ maxRefundable.toFixed(2) }})
            </button>
          </div>
        </div>

        <div class="form-group">
          <label class="form-label">Refund Reason (Optional)</label>
          <input 
            v-model="refundForm.reason" 
            type="text" 
            class="form-input" 
            placeholder="e.g. Customer cancellation, service dispute" 
          />
        </div>
      </div>
    </DialogPlus>

    <!-- Inspect Dialog -->
    <DialogPlus 
      :isOpen="isInspectModalOpen" 
      title="Transaction Record Details" 
      continueText="Close"
      :continueAction="() => isInspectModalOpen = false"
      @close="isInspectModalOpen = false"
    >
      <div v-if="selectedPayment" class="transaction-inspect-details">
        <div class="detail-row">
          <span class="detail-label">Booking Number</span>
          <span class="detail-value font-bold">#{{ selectedPayment.bookingNo }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">Customer</span>
          <span class="detail-value">{{ selectedPayment.userFullName }} ({{ selectedPayment.userEmail }})</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">Payment Method</span>
          <span class="detail-value">{{ selectedPayment.paymentMethod || 'Card' }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">Payment Status</span>
          <span class="detail-value"><StatusBadge type="payment" :value="selectedPayment.paymentStatus" /></span>
        </div>
        <div class="detail-row">
          <span class="detail-label">Total Amount</span>
          <span class="detail-value font-bold">${{ selectedPayment.totalAmount.toFixed(2) }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">Amount Paid</span>
          <span class="detail-value text-success font-bold">${{ selectedPayment.amountPaid.toFixed(2) }}</span>
        </div>
        <div class="detail-row" v-if="selectedPayment.refundedAmount > 0">
          <span class="detail-label">Refunded Amount</span>
          <span class="detail-value text-warning font-bold">${{ selectedPayment.refundedAmount.toFixed(2) }}</span>
        </div>
        <div class="detail-row" v-if="selectedPayment.stripePaymentIntentId">
          <span class="detail-label">Stripe Reference</span>
          <span class="detail-value code-font">{{ selectedPayment.stripePaymentIntentId }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">Created Date</span>
          <span class="detail-value">{{ formatDate(selectedPayment.createdAt) }}</span>
        </div>
      </div>
    </DialogPlus>

    <Toast ref="toastRef" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { PaymentApi } from '@/services'
import type { PaymentResponse, PaymentSummaryResponse } from '@/dto/payment.dto'
import StatusBadge from '@/components/StatusBadge.vue'
import DialogPlus from '@/components/DialogPlus.vue'
import Toast from '@/components/Toast.vue'
import { PaymentStatus } from '@/constants/enums'
import { hasPermission } from '@/helpers/auth.helper'

const payments = ref<PaymentResponse[]>([])
const summary = ref<PaymentSummaryResponse>({
  totalRevenue: 0,
  paidCount: 0,
  pendingCount: 0,
  failedCount: 0,
  refundedCount: 0,
  partiallyRefundedCount: 0
})

const loading = ref(true)
const submitting = ref(false)
const searchQuery = ref('')
const activeTab = ref<string>('ALL')

const isRefundModalOpen = ref(false)
const isInspectModalOpen = ref(false)
const selectedPayment = ref<PaymentResponse | null>(null)
const refundForm = ref({ amount: 0, reason: '' })
const toastRef = ref<any>(null)

const filterTabs = computed(() => [
  { label: 'All', value: 'ALL', count: payments.value.length },
  { label: 'Successful', value: PaymentStatus.PAID, count: summary.value.paidCount },
  { label: 'Pending', value: 'PENDING_GROUP', count: summary.value.pendingCount },
  { label: 'Failed', value: PaymentStatus.FAILED, count: summary.value.failedCount },
  { label: 'Refunded', value: PaymentStatus.REFUNDED, count: summary.value.refundedCount },
  { label: 'Partially Refunded', value: PaymentStatus.PARTIALLY_REFUNDED, count: summary.value.partiallyRefundedCount }
])

const filteredPayments = computed(() => {
  return payments.value.filter(p => {
    // Tab filtering
    if (activeTab.value !== 'ALL') {
      if (activeTab.value === 'PENDING_GROUP') {
        if (p.paymentStatus !== PaymentStatus.PENDING && p.paymentStatus !== PaymentStatus.UNPAID) {
          return false
        }
      } else if (p.paymentStatus !== activeTab.value) {
        return false
      }
    }

    // Search query filtering
    if (searchQuery.value.trim()) {
      const q = searchQuery.value.toLowerCase()
      const matchBooking = p.bookingNo.toLowerCase().includes(q)
      const matchCustomer = (p.userFullName || p.username || '').toLowerCase().includes(q)
      const matchEmail = (p.userEmail || '').toLowerCase().includes(q)
      const matchRef = (p.stripePaymentIntentId || '').toLowerCase().includes(q)
      return matchBooking || matchCustomer || matchEmail || matchRef
    }

    return true
  })
})

const maxRefundable = computed(() => {
  if (!selectedPayment.value) return 0
  const total = selectedPayment.value.totalAmount || 0
  const refunded = selectedPayment.value.refundedAmount || 0
  return Math.max(0, total - refunded)
})

onMounted(() => {
  fetchData()
})

const fetchData = async () => {
  loading.value = true
  try {
    const [paymentsData, summaryData] = await Promise.all([
      PaymentApi.getAllPayments(),
      PaymentApi.getPaymentSummary()
    ])
    payments.value = paymentsData
    summary.value = summaryData
  } catch (err: any) {
    toastRef.value?.show(err.message || 'Failed to load payments data', 'error')
  } finally {
    loading.value = false
  }
}

const selectTab = (tabValue: string) => {
  activeTab.value = tabValue
}

const canRefund = (p: PaymentResponse) => {
  const isPaid = p.paymentStatus === PaymentStatus.PAID || p.paymentStatus === PaymentStatus.PARTIALLY_REFUNDED
  const hasBalance = (p.totalAmount - (p.refundedAmount || 0)) > 0.01
  return isPaid && hasBalance
}

const openInspectModal = (p: PaymentResponse) => {
  selectedPayment.value = p
  isInspectModalOpen.value = true
}

const openRefundModal = (p: PaymentResponse) => {
  selectedPayment.value = p
  const remaining = p.totalAmount - (p.refundedAmount || 0)
  refundForm.value = {
    amount: parseFloat(remaining.toFixed(2)),
    reason: ''
  }
  isRefundModalOpen.value = true
}

const submitRefund = async () => {
  if (!selectedPayment.value) return
  submitting.value = true
  try {
    await PaymentApi.processRefund(selectedPayment.value.id, {
      amount: refundForm.value.amount,
      reason: refundForm.value.reason
    })
    toastRef.value?.show(`Refund of $${refundForm.value.amount.toFixed(2)} processed successfully`, 'success')
    isRefundModalOpen.value = false
    await fetchData()
  } catch (err: any) {
    toastRef.value?.show(err.message || 'Failed to process refund', 'error')
  } finally {
    submitting.value = false
  }
}

const formatPaymentMethod = (method?: string) => {
  if (!method) return '💳 Card'
  const m = method.toLowerCase()
  if (m.includes('fpx')) return '🏦 FPX'
  if (m.includes('grab')) return '📱 GrabPay'
  if (m.includes('card')) return '💳 Card'
  return method
}

const formatDate = (dateStr?: string) => {
  if (!dateStr) return 'N/A'
  return new Date(dateStr).toLocaleString()
}
</script>

<style scoped>
.admin-payments-page {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.page-header {
  margin-bottom: 0.5rem;
}

.page-title {
  font-size: 1.8rem;
  font-weight: 800;
  color: var(--text-main);
}

.page-subtitle {
  color: var(--text-muted);
  font-size: 0.95rem;
}

/* Metric Cards */
.metrics-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 1.25rem;
}

.metric-card {
  display: flex;
  align-items: center;
  gap: 1.25rem;
  padding: 1.25rem;
}

.metric-icon {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  background: rgba(255, 255, 255, 0.05);
}

.metric-icon.revenue { background: rgba(16, 185, 129, 0.15); }
.metric-icon.paid { background: rgba(59, 130, 246, 0.15); }
.metric-icon.pending { background: rgba(245, 158, 11, 0.15); }
.metric-icon.refund { background: rgba(168, 85, 247, 0.15); }

.metric-label {
  font-size: 0.8rem;
  color: var(--text-muted);
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.metric-value {
  font-size: 1.4rem;
  font-weight: 800;
  color: var(--text-main);
}

/* Controls Bar & Tabs */
.content-card {
  padding: 1.5rem;
}

.controls-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.status-tabs {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.tab-btn {
  padding: 0.5rem 1rem;
  border-radius: var(--radius-md);
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid var(--border-glass);
  color: var(--text-muted);
  font-weight: 600;
  font-size: 0.85rem;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.tab-btn:hover {
  background: rgba(255, 255, 255, 0.08);
  color: var(--text-main);
}

.tab-btn.active {
  background: var(--accent-gradient);
  color: #fff;
  border-color: transparent;
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
}

.tab-badge {
  background: rgba(255, 255, 255, 0.2);
  padding: 0.1rem 0.4rem;
  border-radius: 999px;
  font-size: 0.75rem;
}

.search-wrapper {
  min-width: 250px;
}

.search-input {
  width: 100%;
  padding: 0.55rem 1rem;
  font-size: 0.85rem;
}

/* Table Styling */
.table-container {
  overflow-x: auto;
}

.booking-link {
  color: var(--primary-color, #6366f1);
  text-decoration: none;
}

.booking-link:hover {
  text-decoration: underline;
}

.customer-info {
  display: flex;
  flex-direction: column;
}

.customer-name {
  font-weight: 600;
  color: var(--text-main);
}

.customer-email {
  font-size: 0.75rem;
  color: var(--text-muted);
}

.amount-group {
  display: flex;
  flex-direction: column;
}

.refund-subtext {
  font-size: 0.75rem;
  color: #fb923c;
  font-weight: normal;
}

.payment-method-badge {
  display: inline-block;
  padding: 0.25rem 0.6rem;
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.05);
  font-size: 0.8rem;
  font-weight: 600;
}

.date-col {
  font-size: 0.82rem;
  color: var(--text-muted);
}

.empty-state {
  text-align: center;
  padding: 3rem;
  color: var(--text-muted);
}

.action-btn-group {
  display: flex;
  gap: 0.5rem;
  justify-content: flex-end;
}

.btn-xs {
  padding: 0.25rem 0.5rem;
  font-size: 0.75rem;
  margin-top: 0.35rem;
}

/* Modals */
.modal-info-box {
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid var(--border-glass);
  padding: 1rem;
  border-radius: var(--radius-md);
  margin-bottom: 1rem;
  font-size: 0.9rem;
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
}

.quick-amount-buttons {
  margin-top: 0.25rem;
}

.transaction-inspect-details {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  padding-bottom: 0.5rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

.detail-label {
  color: var(--text-muted);
  font-size: 0.88rem;
}

.detail-value {
  font-size: 0.9rem;
  color: var(--text-main);
}

.code-font {
  font-family: monospace;
  font-size: 0.8rem;
  background: rgba(0, 0, 0, 0.2);
  padding: 0.15rem 0.4rem;
  border-radius: 4px;
}

.modal-actions {
  display: flex;
  gap: 1rem;
  margin-top: 1.5rem;
}

.modal-actions .btn {
  flex: 1;
}
</style>
