<template>
  <div class="admin-bookings-page">
    <div class="page-header">
      <h1 class="page-title">User Booking Listings & Status Management</h1>
      <p class="page-subtitle">Inspect customer orders, update statuses, and process completions</p>
    </div>

    <div v-if="loading" class="loading">Loading all system bookings...</div>

    <div v-else class="glass-panel content-card">
      <div class="table-container">
        <table class="custom-table">
          <thead>
            <tr>
              <th>Booking #</th>
              <th>Customer</th>
              <th>Total Amount</th>
              <th>Booking Status</th>
              <th>Payment Status</th>
              <th>Created Date</th>
              <th style="text-align: right;">Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="b in bookings" :key="b.id">
              <td class="font-bold">{{ b.bookingNo }}</td>
              <td>{{ b.userFullName || b.username }}</td>
              <td class="font-bold">${{ b.totalAmount.toFixed(2) }}</td>
              <td><StatusBadge type="booking" :value="b.status" /></td>
              <td><StatusBadge type="payment" :value="b.paymentStatus" /></td>
              <td>{{ formatDate(b.createdAt) }}</td>
              <td style="text-align: right;">
                <div class="action-btn-group">
                  <button v-if="hasPermission('BOOKINGS', 'UPDATE')" @click="openManageModal(b)" class="btn btn-secondary btn-sm">
                    Manage Status
                  </button>
                  <router-link :to="`/admin/bookings/${b.id}`" class="btn btn-primary btn-sm">
                    Inspect Invoice
                  </router-link>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <Modal :isOpen="isModalOpen" title="Update Booking Status" @close="isModalOpen = false">
      <div v-if="selectedBooking">
        <p><strong>Booking #:</strong> {{ selectedBooking.bookingNo }}</p>
        <p><strong>Customer:</strong> {{ selectedBooking.userFullName }}</p>

        <div class="form-group" style="margin-top: 1.25rem;">
          <label class="form-label">Booking Status</label>
          <select v-model="statusForm.status" class="form-select">
            <option :value="BookingStatus.PENDING">PENDING</option>
            <option :value="BookingStatus.CONFIRMED">CONFIRMED</option>
            <option :value="BookingStatus.COMPLETED">COMPLETED</option>
            <option :value="BookingStatus.CANCELLED">CANCELLED</option>
          </select>
        </div>

        <div class="form-group">
          <label class="form-label">Payment Status</label>
          <select v-model="statusForm.paymentStatus" class="form-select">
            <option :value="PaymentStatus.UNPAID">UNPAID</option>
            <option :value="PaymentStatus.PAID">PAID</option>
            <option :value="PaymentStatus.REFUNDED">REFUNDED</option>
            <option :value="PaymentStatus.FAILED">FAILED</option>
          </select>
        </div>

        <div class="modal-actions">
          <button @click="isModalOpen = false" class="btn btn-secondary btn-full">Cancel</button>
          <button @click="submitStatusUpdate" class="btn btn-primary btn-full" :disabled="submitting">
            {{ submitting ? 'Updating...' : 'Save Changes' }}
          </button>
        </div>
      </div>
    </Modal>

    <Toast ref="toastRef" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { AdminApi } from '../../services'
import type { BookingResponse } from '../../services'
import StatusBadge from '../../components/StatusBadge.vue'
import Modal from '../../components/Modal.vue'
import Toast from '../../components/Toast.vue'
import { BookingStatus, PaymentStatus } from '../../constants/enums'
import { hasPermission } from '../../helpers/auth.helper'

const bookings = ref<BookingResponse[]>([])
const loading = ref(true)
const submitting = ref(false)
const isModalOpen = ref(false)
const selectedBooking = ref<BookingResponse | null>(null)
const toastRef = ref<InstanceType<typeof Toast> | null>(null)

const statusForm = ref({
  status: BookingStatus.PENDING,
  paymentStatus: PaymentStatus.UNPAID
})

const fetchBookings = async () => {
  loading.value = true
  try {
    bookings.value = await AdminApi.getAllBookings()
  } catch (err) {
    console.error('Failed to fetch bookings:', err)
  } finally {
    loading.value = false
  }
}

const openManageModal = (b: BookingResponse) => {
  selectedBooking.value = b
  statusForm.value = {
    status: b.status as BookingStatus,
    paymentStatus: b.paymentStatus as PaymentStatus
  }
  isModalOpen.value = true
}

const submitStatusUpdate = async () => {
  if (!selectedBooking.value) return
  submitting.value = true
  try {
    const updated = await AdminApi.updateBookingStatus(selectedBooking.value.id, statusForm.value)
    const index = bookings.value.findIndex(b => b.id === selectedBooking.value?.id)
    if (index !== -1) bookings.value[index] = updated
    toastRef.value?.show('Booking status updated!')
    isModalOpen.value = false
  } catch (err) {
    toastRef.value?.show('Failed to update status', 'error')
  } finally {
    submitting.value = false
  }
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleDateString()
}

onMounted(() => {
  fetchBookings()
})
</script>

<style scoped>
.page-header {
  margin-bottom: 2rem;
}

.page-title {
  font-size: 2rem;
  font-weight: 800;
}

.page-subtitle {
  color: var(--text-muted);
}

.content-card {
  padding: 1.5rem;
}

.font-bold {
  font-weight: 700;
}

.action-btn-group {
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
}

.modal-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
  margin-top: 1.5rem;
}
</style>
