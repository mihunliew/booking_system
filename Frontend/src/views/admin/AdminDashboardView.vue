<template>
  <div class="dashboard-page">
    <div class="page-header">
      <h1 class="page-title">Admin Dashboard</h1>
      <p class="page-subtitle">Real-time system overview & performance analytics</p>
    </div>

    <div v-if="loading" class="loading">Loading dashboard metrics...</div>

    <template v-else-if="stats">
      <div class="kpi-grid">
        <div class="glass-panel kpi-card">
          <span class="kpi-icon">💰</span>
          <div class="kpi-info">
            <span class="kpi-label">Total Revenue</span>
            <span class="kpi-value">${{ stats.totalRevenue ? stats.totalRevenue.toFixed(2) : '0.00' }}</span>
          </div>
        </div>

        <div class="glass-panel kpi-card">
          <span class="kpi-icon">📑</span>
          <div class="kpi-info">
            <span class="kpi-label">Total Bookings</span>
            <span class="kpi-value">{{ stats.totalBookings }}</span>
          </div>
        </div>

        <div class="glass-panel kpi-card">
          <span class="kpi-icon">⏳</span>
          <div class="kpi-info">
            <span class="kpi-label">Pending Approval</span>
            <span class="kpi-value warning">{{ stats.pendingBookings }}</span>
          </div>
        </div>

        <div class="glass-panel kpi-card">
          <span class="kpi-icon">👥</span>
          <div class="kpi-info">
            <span class="kpi-label">Registered Users</span>
            <span class="kpi-value">{{ stats.totalUsers }}</span>
          </div>
        </div>
      </div>

      <div class="recent-section glass-panel">
        <div class="section-header">
          <h2>Recent Booking Requests</h2>
          <router-link to="/admin/bookings" class="btn btn-secondary btn-sm">View All</router-link>
        </div>

        <div class="table-container">
          <table class="custom-table">
            <thead>
              <tr>
                <th>Booking #</th>
                <th>Customer</th>
                <th>Total</th>
                <th>Booking Status</th>
                <th>Payment Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="b in stats.recentBookings" :key="b.id">
                <td class="font-bold">{{ b.bookingNo }}</td>
                <td>{{ b.userFullName || b.username }}</td>
                <td class="font-bold">${{ b.totalAmount.toFixed(2) }}</td>
                <td><StatusBadge type="booking" :value="b.status" /></td>
                <td><StatusBadge type="payment" :value="b.paymentStatus" /></td>
                <td>
                  <router-link :to="`/admin/bookings/${b.id}`" class="btn btn-secondary btn-sm">
                    Inspect
                  </router-link>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { AdminApi } from '../../services'
import type { AdminDashboardDTO } from '../../services'
import StatusBadge from '../../components/StatusBadge.vue'

const stats = ref<AdminDashboardDTO | null>(null)
const loading = ref(true)

const fetchDashboard = async () => {
  loading.value = true
  try {
    stats.value = await AdminApi.getDashboard()
  } catch (err) {
    console.error('Failed to load dashboard:', err)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchDashboard()
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

.kpi-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 1.5rem;
  margin-bottom: 2.5rem;
}

.kpi-card {
  padding: 1.5rem;
  display: flex;
  align-items: center;
  gap: 1.25rem;
}

.kpi-icon {
  font-size: 2.25rem;
}

.kpi-info {
  display: flex;
  flex-direction: column;
}

.kpi-label {
  font-size: 0.8rem;
  color: var(--text-muted);
  font-weight: 600;
}

.kpi-value {
  font-size: 1.6rem;
  font-weight: 800;
  color: var(--text-main);
}

.kpi-value.warning {
  color: #fbbf24;
}

.recent-section {
  padding: 1.75rem;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.25rem;
}

.font-bold {
  font-weight: 700;
}
</style>
