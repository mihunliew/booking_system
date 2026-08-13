<template>
  <div class="admin-bookings-page">
    <div class="page-header">
      <h1 class="page-title">Booking Orders & Reservations</h1>
      <p class="page-subtitle">Track customer reservations, inspect booked items and dates, and manage order fulfillment</p>
    </div>

    <!-- Summary Metrics Cards -->
    <div class="metrics-grid">
      <div class="glass-panel metric-card">
        <div class="metric-icon total">📑</div>
        <div class="metric-info">
          <div class="metric-label">Total Bookings</div>
          <div class="metric-value">{{ bookings.length }}</div>
        </div>
      </div>
      <div class="glass-panel metric-card">
        <div class="metric-icon pending">⏳</div>
        <div class="metric-info">
          <div class="metric-label">Pending Confirmation</div>
          <div class="metric-value">{{ statusCounts[BookingStatus.PENDING] || 0 }}</div>
        </div>
      </div>
      <div class="glass-panel metric-card">
        <div class="metric-icon confirmed">🔵</div>
        <div class="metric-info">
          <div class="metric-label">Confirmed</div>
          <div class="metric-value">{{ statusCounts[BookingStatus.CONFIRMED] || 0 }}</div>
        </div>
      </div>
      <div class="glass-panel metric-card">
        <div class="metric-icon completed">✅</div>
        <div class="metric-info">
          <div class="metric-label">Completed</div>
          <div class="metric-value">{{ statusCounts[BookingStatus.COMPLETED] || 0 }}</div>
        </div>
      </div>
    </div>

    <!-- Content Card -->
    <div class="glass-panel content-card">
      <div class="controls-bar">
        <!-- Status Filter Tabs -->
        <div class="status-tabs">
          <button 
            v-for="tab in filterTabs" 
            :key="tab.value" 
            class="tab-btn" 
            :class="{ active: activeTab === tab.value }"
            @click="activeTab = tab.value"
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
            placeholder="Search booking #, customer, or booked item..." 
          />
        </div>
      </div>

      <div v-if="loading" class="loading">Loading booking orders...</div>

      <div v-else class="table-container">
        <table class="custom-table">
          <thead>
            <tr>
              <th>Booking #</th>
              <th>Customer</th>
              <th>Booked Services / Items</th>
              <th>Total Amount</th>
              <th>Booking Status</th>
              <th>Order Date</th>
              <th style="text-align: right;">Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="filteredBookings.length === 0">
              <td colspan="7" class="empty-state">No booking orders found matching the filter criteria.</td>
            </tr>
            <tr v-for="b in filteredBookings" :key="b.id">
              <td class="font-bold">
                <a @click.prevent="openInspectModal(b)" href="#" class="booking-link">
                  #{{ b.bookingNo }}
                </a>
              </td>
              <td>
                <div class="customer-info">
                  <span class="customer-name">{{ b.userFullName || b.username }}</span>
                </div>
              </td>
              <td>
                <!-- Booked Items Preview -->
                <div class="items-badge-container">
                  <div v-for="item in (b.items || []).slice(0, 2)" :key="item.id || item.productName" class="item-pill">
                    <span class="item-name">{{ item.productName }}</span>
                    <span class="item-meta">
                      📅 {{ formatDateOnly(item.bookingDate) }} &bull; {{ item.quantity }}x
                    </span>
                  </div>
                  <div v-if="(b.items || []).length > 2" class="item-more-pill">
                    +{{ (b.items || []).length - 2 }} more items
                  </div>
                  <div v-if="!b.items || b.items.length === 0" class="text-muted text-xs">
                    No item breakdown
                  </div>
                </div>
              </td>
              <td class="font-bold">${{ b.totalAmount.toFixed(2) }}</td>
              <td><StatusBadge type="booking" :value="b.status" /></td>
              <td class="date-col">{{ formatDate(b.createdAt) }}</td>
              <td style="text-align: right;">
                <div class="action-btn-group">
                  <button @click="openInspectModal(b)" class="btn btn-secondary btn-sm">
                    Inspect Order
                  </button>
                  <button 
                    v-if="hasPermission('BOOKINGS', 'UPDATE')" 
                    @click="openManageModal(b)" 
                    class="btn btn-primary btn-sm"
                  >
                    Manage
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Quick Status Update Dialog -->
    <DialogPlus 
      :isOpen="isManageModalOpen" 
      title="Update Booking Status" 
      cancelText="Cancel"
      :cancelAction="() => isManageModalOpen = false"
      continueText="Save Changes"
      :continueAction="submitStatusUpdate"
      :submitting="submitting"
      @close="isManageModalOpen = false"
    >
      <div v-if="selectedBooking">
        <div class="modal-info-box">
          <p><strong>Booking #:</strong> #{{ selectedBooking.bookingNo }}</p>
          <p><strong>Customer:</strong> {{ selectedBooking.userFullName || selectedBooking.username }}</p>
          <p><strong>Total Amount:</strong> ${{ selectedBooking.totalAmount.toFixed(2) }}</p>
        </div>

        <div class="form-group" style="margin-top: 1.25rem;">
          <label class="form-label">Booking Status</label>
          <select v-model="statusForm.status" class="form-select">
            <option :value="BookingStatus.PENDING">PENDING (Awaiting Confirmation)</option>
            <option :value="BookingStatus.CONFIRMED">CONFIRMED (Ready / In Progress)</option>
            <option :value="BookingStatus.COMPLETED">COMPLETED (Fulfilled)</option>
            <option :value="BookingStatus.CANCELLED">CANCELLED (Order Cancelled)</option>
          </select>
        </div>

        <div class="form-group">
          <label class="form-label">Payment Status Sync</label>
          <select v-model="statusForm.paymentStatus" class="form-select">
            <option :value="PaymentStatus.UNPAID">UNPAID</option>
            <option :value="PaymentStatus.PENDING">PENDING</option>
            <option :value="PaymentStatus.PAID">PAID</option>
            <option :value="PaymentStatus.FAILED">FAILED</option>
            <option :value="PaymentStatus.REFUNDED">REFUNDED</option>
            <option :value="PaymentStatus.PARTIALLY_REFUNDED">PARTIALLY REFUNDED</option>
          </select>
        </div>
      </div>
    </DialogPlus>

    <!-- Full Order Inspect Dialog -->
    <DialogPlus 
      :isOpen="isInspectModalOpen" 
      title="Booking Order Breakdown" 
      continueText="Close"
      :continueAction="() => isInspectModalOpen = false"
      @close="isInspectModalOpen = false"
    >
      <div v-if="selectedBooking" class="order-inspect-container">
        <div class="inspect-header-info">
          <div class="info-group">
            <span class="info-label">Booking Reference</span>
            <span class="info-value font-bold">#{{ selectedBooking.bookingNo }}</span>
          </div>
          <div class="info-group">
            <span class="info-label">Customer</span>
            <span class="info-value">{{ selectedBooking.userFullName || selectedBooking.username }}</span>
          </div>
          <div class="info-group">
            <span class="info-label">Booking Status</span>
            <span class="info-value"><StatusBadge type="booking" :value="selectedBooking.status" /></span>
          </div>
          <div class="info-group">
            <span class="info-label">Payment Status</span>
            <span class="info-value"><StatusBadge type="payment" :value="selectedBooking.paymentStatus" /></span>
          </div>
        </div>

        <h4 class="section-subtitle">Booked Items & Schedules</h4>
        <div class="items-table-wrapper">
          <table class="items-table">
            <thead>
              <tr>
                <th>Service / Product</th>
                <th>Reserved Date</th>
                <th>Price</th>
                <th>Qty</th>
                <th style="text-align: right;">Subtotal</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in selectedBooking.items" :key="item.id || item.productName">
                <td class="font-bold">{{ item.productName }}</td>
                <td>📅 {{ formatDateOnly(item.bookingDate) }}</td>
                <td>${{ item.price.toFixed(2) }}</td>
                <td>x{{ item.quantity }}</td>
                <td style="text-align: right;" class="font-bold">${{ item.subtotal.toFixed(2) }}</td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="order-totals-summary">
          <div v-if="selectedBooking.subtotalAmount" class="total-row">
            <span>Subtotal:</span>
            <span>${{ selectedBooking.subtotalAmount.toFixed(2) }}</span>
          </div>
          <div v-if="selectedBooking.discountAmount && selectedBooking.discountAmount > 0" class="total-row text-success">
            <span>Discount ({{ selectedBooking.promoCode }}):</span>
            <span>-${{ selectedBooking.discountAmount.toFixed(2) }}</span>
          </div>
          <div class="total-row grand-total">
            <span>Total Amount:</span>
            <span>${{ selectedBooking.totalAmount.toFixed(2) }}</span>
          </div>
        </div>

        <div v-if="selectedBooking.notes" class="notes-box">
          <strong>Customer Notes:</strong> {{ selectedBooking.notes }}
        </div>
      </div>
    </DialogPlus>

    <Toast ref="toastRef" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { AdminApi } from '@/services'
import type { BookingResponse } from '@/services'
import StatusBadge from '@/components/StatusBadge.vue'
import DialogPlus from '@/components/DialogPlus.vue'
import Toast from '@/components/Toast.vue'
import { BookingStatus, PaymentStatus } from '@/constants/enums'
import { hasPermission } from '@/helpers/auth.helper'

const bookings = ref<BookingResponse[]>([])
const loading = ref(true)
const submitting = ref(false)
const searchQuery = ref('')
const activeTab = ref<string>('ALL')

const isManageModalOpen = ref(false)
const isInspectModalOpen = ref(false)
const selectedBooking = ref<BookingResponse | null>(null)
const toastRef = ref<any>(null)

const statusForm = ref({
  status: BookingStatus.PENDING,
  paymentStatus: PaymentStatus.UNPAID
})

const statusCounts = computed(() => {
  const counts: Record<string, number> = {}
  bookings.value.forEach(b => {
    counts[b.status] = (counts[b.status] || 0) + 1
  })
  return counts
})

const filterTabs = computed(() => [
  { label: 'All Orders', value: 'ALL', count: bookings.value.length },
  { label: 'Pending', value: BookingStatus.PENDING, count: statusCounts.value[BookingStatus.PENDING] || 0 },
  { label: 'Confirmed', value: BookingStatus.CONFIRMED, count: statusCounts.value[BookingStatus.CONFIRMED] || 0 },
  { label: 'Completed', value: BookingStatus.COMPLETED, count: statusCounts.value[BookingStatus.COMPLETED] || 0 },
  { label: 'Cancelled', value: BookingStatus.CANCELLED, count: statusCounts.value[BookingStatus.CANCELLED] || 0 }
])

const filteredBookings = computed(() => {
  return bookings.value.filter(b => {
    // Status Tab filter
    if (activeTab.value !== 'ALL' && b.status !== activeTab.value) {
      return false
    }

    // Search query filter
    if (searchQuery.value.trim()) {
      const q = searchQuery.value.toLowerCase()
      const matchNo = b.bookingNo.toLowerCase().includes(q)
      const matchCustomer = (b.userFullName || b.username || '').toLowerCase().includes(q)
      const matchItems = (b.items || []).some(i => i.productName.toLowerCase().includes(q))
      return matchNo || matchCustomer || matchItems
    }

    return true
  })
})

onMounted(() => {
  fetchBookings()
})

const fetchBookings = async () => {
  loading.value = true
  try {
    bookings.value = await AdminApi.getAllBookings()
  } catch (err: any) {
    toastRef.value?.show(err.message || 'Failed to fetch bookings', 'error')
  } finally {
    loading.value = false
  }
}

const openInspectModal = (b: BookingResponse) => {
  selectedBooking.value = b
  isInspectModalOpen.value = true
}

const openManageModal = (b: BookingResponse) => {
  selectedBooking.value = b
  statusForm.value = {
    status: b.status as BookingStatus,
    paymentStatus: b.paymentStatus as PaymentStatus
  }
  isManageModalOpen.value = true
}

const submitStatusUpdate = async () => {
  if (!selectedBooking.value) return
  submitting.value = true
  try {
    const updated = await AdminApi.updateBookingStatus(selectedBooking.value.id, statusForm.value)
    const index = bookings.value.findIndex(b => b.id === selectedBooking.value?.id)
    if (index !== -1) bookings.value[index] = updated
    toastRef.value?.show('Booking order status updated successfully!')
    isManageModalOpen.value = false
  } catch (err: any) {
    toastRef.value?.show(err.message || 'Failed to update status', 'error')
  } finally {
    submitting.value = false
  }
}

const formatDate = (dateStr?: string) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString()
}

const formatDateOnly = (dateStr?: string) => {
  if (!dateStr) return 'N/A'
  return new Date(dateStr).toLocaleDateString()
}
</script>

<style scoped>
.admin-bookings-page {
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
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
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
}

.metric-icon.total { background: rgba(99, 102, 241, 0.15); }
.metric-icon.pending { background: rgba(245, 158, 11, 0.15); }
.metric-icon.confirmed { background: rgba(59, 130, 246, 0.15); }
.metric-icon.completed { background: rgba(16, 185, 129, 0.15); }

.metric-label {
  font-size: 0.78rem;
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

/* Controls & Tabs */
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

/* Booked Items Badges */
.items-badge-container {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
  max-width: 320px;
}

.item-pill {
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 6px;
  padding: 0.3rem 0.6rem;
  display: flex;
  flex-direction: column;
  gap: 0.15rem;
}

.item-name {
  font-weight: 600;
  font-size: 0.85rem;
  color: var(--text-main);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.item-meta {
  font-size: 0.75rem;
  color: var(--text-muted);
}

.item-more-pill {
  font-size: 0.75rem;
  color: var(--accent-color, #818cf8);
  font-weight: 600;
  padding-left: 0.2rem;
}

.booking-link {
  color: var(--primary-color, #6366f1);
  text-decoration: none;
}

.booking-link:hover {
  text-decoration: underline;
}

.customer-name {
  font-weight: 600;
  color: var(--text-main);
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

/* Inspect Modal */
.order-inspect-container {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.inspect-header-info {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 0.75rem;
  background: rgba(255, 255, 255, 0.03);
  padding: 1rem;
  border-radius: var(--radius-md);
  border: 1px solid var(--border-glass);
}

.info-group {
  display: flex;
  flex-direction: column;
  gap: 0.2rem;
}

.info-label {
  font-size: 0.75rem;
  color: var(--text-muted);
  text-transform: uppercase;
}

.info-value {
  font-size: 0.9rem;
  color: var(--text-main);
}

.section-subtitle {
  font-size: 1rem;
  font-weight: 700;
  margin-top: 0.5rem;
}

.items-table-wrapper {
  overflow-x: auto;
  border: 1px solid var(--border-glass);
  border-radius: var(--radius-md);
}

.items-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.85rem;
}

.items-table th, .items-table td {
  padding: 0.65rem 0.85rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

.items-table th {
  background: rgba(255, 255, 255, 0.03);
  color: var(--text-muted);
  text-align: left;
}

.order-totals-summary {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
  align-items: flex-end;
  margin-top: 0.5rem;
}

.total-row {
  display: flex;
  gap: 1.5rem;
  font-size: 0.9rem;
}

.grand-total {
  font-size: 1.1rem;
  font-weight: 800;
  color: var(--text-main);
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  padding-top: 0.35rem;
}

.notes-box {
  background: rgba(245, 158, 11, 0.1);
  border: 1px solid rgba(245, 158, 11, 0.2);
  padding: 0.75rem;
  border-radius: var(--radius-md);
  font-size: 0.85rem;
  color: #fbbf24;
}

.modal-info-box {
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid var(--border-glass);
  padding: 1rem;
  border-radius: var(--radius-md);
  margin-bottom: 1rem;
  font-size: 0.9rem;
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
