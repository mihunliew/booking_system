<template>
  <div class="schedule-view">
    <div class="page-header">
      <div>
        <h1 class="page-title">Availability & Schedule</h1>
        <p class="page-subtitle">Read-only monthly inventory schedule & real-time slot tracking</p>
      </div>
      <div class="read-only-badge glass-card">
        👁️ Read-Only View
      </div>
    </div>

    <!-- Filter & Month Navigation Bar -->
    <div class="controls-card glass-panel">
      <div class="control-group">
        <label class="control-label">Select Product / Service:</label>
        <select v-model="selectedProductId" class="form-select product-select" :disabled="loadingProducts" @change="loadSchedule">
          <option v-if="loadingProducts" :value="null">Loading products...</option>
          <option v-else-if="products.length === 0" :value="null">No products found</option>
          <option v-for="prod in products" :key="prod.id" :value="prod.id">
            {{ prod.name }} (Daily Stock: {{ prod.stockQuantity || 10 }})
          </option>
        </select>
      </div>

      <div class="month-navigator">
        <button @click="prevMonth" class="btn btn-secondary btn-sm" :disabled="loadingSchedule">&larr; Prev</button>
        <div class="current-month-display">
          <span class="month-name">{{ currentMonthName }}</span>
          <span class="year-name">{{ currentYear }}</span>
        </div>
        <button @click="nextMonth" class="btn btn-secondary btn-sm" :disabled="loadingSchedule">Next &rarr;</button>
        <button @click="resetToCurrentMonth" class="btn btn-primary btn-sm ml-2" :disabled="loadingSchedule">Today</button>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loadingSchedule" class="loading-state glass-panel">
      <span>Loading availability schedule...</span>
    </div>

    <!-- Empty/No Product State -->
    <div v-else-if="!selectedProduct" class="empty-state glass-panel">
      <p>Please select a product above to view its monthly availability schedule.</p>
    </div>

    <!-- Calendar View -->
    <div v-else class="calendar-container glass-panel">
      <div class="calendar-header-info">
        <h3>📅 {{ selectedProduct.name }} — Inventory Schedule</h3>
        <div class="legend">
          <span class="legend-item"><span class="dot dot-available"></span> Available</span>
          <span class="legend-item"><span class="dot dot-partial"></span> Partial</span>
          <span class="legend-item"><span class="dot dot-soldout"></span> Sold Out</span>
        </div>
      </div>

      <!-- Weekday Headers -->
      <div class="calendar-grid headers">
        <div v-for="day in weekDays" :key="day" class="weekday-cell">{{ day }}</div>
      </div>

      <!-- Days Grid -->
      <div class="calendar-grid days">
        <!-- Blank Leading Days -->
        <div v-for="blank in blankLeadingDays" :key="'blank-' + blank" class="day-cell blank"></div>

        <!-- Month Days -->
        <div
          v-for="dayInfo in calendarDays"
          :key="dayInfo.date"
          class="day-cell"
          :class="{
            'today': dayInfo.isToday,
            'sold-out': dayInfo.soldOut,
            'partially-booked': dayInfo.bookedCount > 0 || dayInfo.heldCount > 0
          }"
        >
          <div class="day-header">
            <span class="day-number">{{ dayInfo.dayNumber }}</span>
            <span v-if="dayInfo.isToday" class="today-badge">Today</span>
          </div>

          <div class="day-body">
            <div
              class="stock-badge"
              :class="{
                'badge-soldout': dayInfo.soldOut,
                'badge-partial': dayInfo.availableSlots < dayInfo.stockQuantity && !dayInfo.soldOut,
                'badge-full': dayInfo.availableSlots === dayInfo.stockQuantity
              }"
            >
              <template v-if="dayInfo.soldOut">
                🔴 Sold Out
              </template>
              <template v-else-if="dayInfo.availableSlots < dayInfo.stockQuantity">
                🟡 {{ dayInfo.availableSlots }}/{{ dayInfo.stockQuantity }} Left
              </template>
              <template v-else>
                🟢 {{ dayInfo.availableSlots }}/{{ dayInfo.stockQuantity }} Left
              </template>
            </div>

            <!-- Progress Bar -->
            <div class="occupancy-bar-track">
              <div
                class="occupancy-bar-fill"
                :style="{ width: getOccupancyPercent(dayInfo) + '%' }"
                :class="dayInfo.soldOut ? 'fill-danger' : 'fill-primary'"
              ></div>
            </div>

            <div class="breakdown-info">
              <span title="Confirmed Bookings">Booked: {{ dayInfo.bookedCount }}</span>
              <span v-if="dayInfo.heldCount > 0" class="text-warning" title="5m Pending Holds">Held: {{ dayInfo.heldCount }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <Toast ref="toastRef" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { AdminApi, ProductApi } from '../../services'
import type { ProductDTO, ProductMonthlyScheduleResponse, DayScheduleDTO } from '../../services'
import Toast from '../../components/Toast.vue'

const products = ref<ProductDTO[]>([])
const selectedProductId = ref<number | null>(null)
const loadingProducts = ref(true)

const todayObj = new Date()
const currentYear = ref(todayObj.getFullYear())
const currentMonth = ref(todayObj.getMonth() + 1) // 1-12

const scheduleResponse = ref<ProductMonthlyScheduleResponse | null>(null)
const loadingSchedule = ref(false)
const toastRef = ref<InstanceType<typeof Toast> | null>(null)

const weekDays = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat']
const monthNames = [
  'January', 'February', 'March', 'April', 'May', 'June',
  'July', 'August', 'September', 'October', 'November', 'December'
]

const currentMonthName = computed(() => monthNames[currentMonth.value - 1])

const selectedProduct = computed(() => {
  return products.value.find(p => p.id === selectedProductId.value) || null
})

// Calculate leading blank days for calendar alignment
const blankLeadingDays = computed(() => {
  const firstDay = new Date(currentYear.value, currentMonth.value - 1, 1)
  return firstDay.getDay() // 0 for Sun, 1 for Mon, etc.
})

interface CalendarDayItem extends DayScheduleDTO {
  dayNumber: number;
  isToday: boolean;
}

const calendarDays = computed<CalendarDayItem[]>(() => {
  if (!scheduleResponse.value || !scheduleResponse.value.days) return []
  const todayStr = new Date().toISOString().split('T')[0]
  return scheduleResponse.value.days.map(d => {
    const dayNum = parseInt(d.date.split('-')[2], 10)
    return {
      ...d,
      dayNumber: dayNum,
      isToday: d.date === todayStr
    }
  })
})

const getOccupancyPercent = (day: DayScheduleDTO) => {
  if (!day.stockQuantity || day.stockQuantity <= 0) return 0
  const occupied = day.bookedCount + day.heldCount
  const pct = (occupied / day.stockQuantity) * 100
  return Math.min(100, Math.max(0, pct))
}

const loadProducts = async () => {
  loadingProducts.value = true
  try {
    const list = await AdminApi.getAllProducts()
    products.value = list
    if (list.length > 0) {
      selectedProductId.value = list[0].id
      await loadSchedule()
    }
  } catch (err: any) {
    toastRef.value?.show('Failed to load product list', 'error')
  } finally {
    loadingProducts.value = false
  }
}

const loadSchedule = async () => {
  if (!selectedProductId.value) return
  loadingSchedule.value = true
  try {
    const data = await ProductApi.getMonthlySchedule(
      selectedProductId.value,
      currentYear.value,
      currentMonth.value
    )
    scheduleResponse.value = data
  } catch (err: any) {
    const msg = typeof err === 'string' ? err : (err.message || 'Failed to load availability schedule')
    toastRef.value?.show(msg, 'error')
  } finally {
    loadingSchedule.value = false
  }
}

const prevMonth = () => {
  if (currentMonth.value === 1) {
    currentMonth.value = 12
    currentYear.value--
  } else {
    currentMonth.value--
  }
  loadSchedule()
}

const nextMonth = () => {
  if (currentMonth.value === 12) {
    currentMonth.value = 1
    currentYear.value++
  } else {
    currentMonth.value++
  }
  loadSchedule()
}

const resetToCurrentMonth = () => {
  const now = new Date()
  currentYear.value = now.getFullYear()
  currentMonth.value = now.getMonth() + 1
  loadSchedule()
}

onMounted(() => {
  loadProducts()
})
</script>

<style scoped>
.schedule-view {
  padding: 1.5rem;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 1.5rem;
}

.page-title {
  font-size: 1.75rem;
  font-weight: 800;
  color: var(--text-main, #ffffff);
}

.page-subtitle {
  font-size: 0.875rem;
  color: var(--text-muted, #94a3b8);
  margin-top: 0.25rem;
}

.read-only-badge {
  padding: 0.5rem 1rem;
  font-size: 0.85rem;
  font-weight: 700;
  color: #38bdf8;
  border-radius: 20px;
}

.controls-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.25rem 1.5rem;
  margin-bottom: 1.5rem;
  flex-wrap: wrap;
  gap: 1rem;
}

.control-group {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.control-label {
  font-size: 0.9rem;
  font-weight: 700;
  color: var(--text-main, #ffffff);
}

.product-select {
  padding: 0.6rem 1rem;
  border-radius: 8px;
  background: var(--bg-surface-elevated, #1e1e38);
  color: var(--text-main, #ffffff);
  border: 1px solid var(--border-glass, rgba(255, 255, 255, 0.15));
  font-weight: 600;
  min-width: 250px;
}

.month-navigator {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.current-month-display {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 140px;
}

.month-name {
  font-size: 1.1rem;
  font-weight: 800;
  color: var(--text-main, #ffffff);
}

.year-name {
  font-size: 0.75rem;
  color: var(--text-muted, #94a3b8);
}

.loading-state, .empty-state {
  padding: 3rem;
  text-align: center;
  color: var(--text-muted, #94a3b8);
}

.calendar-container {
  padding: 1.5rem;
}

.calendar-header-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.25rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid var(--border-glass, rgba(255, 255, 255, 0.1));
}

.calendar-header-info h3 {
  font-size: 1.15rem;
  font-weight: 700;
  margin: 0;
}

.legend {
  display: flex;
  gap: 1.25rem;
  font-size: 0.8rem;
  color: var(--text-muted, #94a3b8);
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 0.35rem;
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.dot-available { background: #34d399; }
.dot-partial { background: #fbbf24; }
.dot-soldout { background: #f87171; }

.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 0.75rem;
}

.calendar-grid.headers {
  margin-bottom: 0.5rem;
}

.weekday-cell {
  text-align: center;
  font-weight: 700;
  font-size: 0.8rem;
  text-transform: uppercase;
  color: var(--text-muted, #94a3b8);
  padding: 0.5rem;
}

.day-cell {
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid var(--border-glass, rgba(255, 255, 255, 0.08));
  border-radius: 12px;
  padding: 0.75rem;
  min-height: 105px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  transition: transform 0.2s ease, border-color 0.2s ease;
}

.day-cell:hover {
  border-color: rgba(255, 255, 255, 0.25);
  transform: translateY(-2px);
}

.day-cell.blank {
  background: transparent;
  border: none;
  pointer-events: none;
}

.day-cell.today {
  border-color: #38bdf8;
  box-shadow: 0 0 10px rgba(56, 189, 248, 0.2);
}

.day-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
}

.day-number {
  font-weight: 800;
  font-size: 1rem;
  color: var(--text-main, #ffffff);
}

.today-badge {
  font-size: 0.65rem;
  background: #38bdf8;
  color: #000;
  padding: 0.1rem 0.4rem;
  border-radius: 4px;
  font-weight: 800;
}

.stock-badge {
  font-size: 0.75rem;
  font-weight: 700;
  padding: 0.35rem 0.5rem;
  border-radius: 6px;
  text-align: center;
  margin-bottom: 0.4rem;
}

.badge-full {
  background: rgba(52, 211, 153, 0.15);
  color: #34d399;
}

.badge-partial {
  background: rgba(251, 191, 36, 0.15);
  color: #fbbf24;
}

.badge-soldout {
  background: rgba(248, 113, 113, 0.15);
  color: #f87171;
}

.occupancy-bar-track {
  width: 100%;
  height: 4px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 2px;
  overflow: hidden;
  margin-bottom: 0.4rem;
}

.occupancy-bar-fill {
  height: 100%;
  transition: width 0.3s ease;
}

.fill-primary {
  background: linear-gradient(90deg, #38bdf8, #34d399);
}

.fill-danger {
  background: #f87171;
}

.breakdown-info {
  display: flex;
  justify-content: space-between;
  font-size: 0.68rem;
  color: var(--text-muted, #94a3b8);
}
</style>
