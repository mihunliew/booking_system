<template>
  <div class="promocodes-page">
    <div class="page-header">
      <div>
        <h1 class="page-title">Promo Codes Management</h1>
        <p class="page-subtitle">Create and manage discounts, usage limits, and promotional rules</p>
      </div>
      <button v-if="hasPermission('PROMOCODES', 'CREATE')" @click="openCreateModal" class="btn btn-primary">
        + Create Promo Code
      </button>
    </div>

    <div v-if="loading" class="loading">Loading promo codes...</div>

    <div v-else class="glass-panel main-panel">
      <div class="table-container">
        <table class="custom-table">
          <thead>
            <tr>
              <th>Code</th>
              <th>Type</th>
              <th>Discount Value</th>
              <th>Min Spend</th>
              <th>Max Discount</th>
              <th>Usage Limit</th>
              <th>Used Count</th>
              <th>Status</th>
              <th v-if="hasPermission('PROMOCODES', 'UPDATE') || hasPermission('PROMOCODES', 'DELETE')">Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="promo in promoCodes" :key="promo.id">
              <td class="font-bold promo-code-text">🏷️ {{ promo.code }}</td>
              <td>
                <span class="badge-type">{{ promo.discountType }}</span>
              </td>
              <td class="font-bold">
                {{ promo.discountType === 'PERCENTAGE' ? promo.discountValue + '%' : '$' + promo.discountValue.toFixed(2) }}
              </td>
              <td>${{ (promo.minSpend || 0).toFixed(2) }}</td>
              <td>{{ promo.maxDiscount ? '$' + promo.maxDiscount.toFixed(2) : 'No Cap' }}</td>
              <td>{{ promo.usageLimit !== null && promo.usageLimit !== undefined ? promo.usageLimit : 'Unlimited' }}</td>
              <td class="font-bold">{{ promo.usedCount }}</td>
              <td>
                <span class="status-pill" :class="{ active: promo.isActive, inactive: !promo.isActive }">
                  {{ promo.isActive ? 'ACTIVE' : 'INACTIVE' }}
                </span>
              </td>
              <td v-if="hasPermission('PROMOCODES', 'UPDATE') || hasPermission('PROMOCODES', 'DELETE')">
                <div class="action-btn-group">
                  <button v-if="hasPermission('PROMOCODES', 'UPDATE')" @click="openEditModal(promo)" class="btn btn-secondary btn-sm">
                    Edit
                  </button>
                  <button v-if="hasPermission('PROMOCODES', 'DELETE')" @click="deletePromo(promo.id)" class="btn btn-danger btn-sm">
                    Delete
                  </button>
                </div>
              </td>
            </tr>
            <tr v-if="promoCodes.length === 0">
              <td colspan="9" style="text-align: center; padding: 2rem; color: var(--text-muted);">
                No promo codes configured yet. Click "+ Create Promo Code" to add one.
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Create/Edit Modal -->
    <Modal :isOpen="isModalOpen" :title="editingId ? 'Edit Promo Code' : 'Create Promo Code'" @close="closeModal">
      <form @submit.prevent="savePromoCode" class="modal-form">
        <div class="form-group">
          <label class="form-label">Promo Code *</label>
          <input type="text" v-model="form.code" required class="form-input uppercase" placeholder="e.g. SUMMER2026" />
        </div>

        <div class="form-row">
          <div class="form-group">
            <label class="form-label">Discount Type *</label>
            <select v-model="form.discountType" class="form-select">
              <option value="PERCENTAGE">PERCENTAGE (%)</option>
              <option value="FIXED_AMOUNT">FIXED AMOUNT ($)</option>
            </select>
          </div>
          <div class="form-group">
            <label class="form-label">Discount Value *</label>
            <input type="number" step="0.01" min="0.01" v-model.number="form.discountValue" required class="form-input" placeholder="e.g. 10 or 25.50" />
          </div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label class="form-label">Min Spend ($)</label>
            <input type="number" step="0.01" min="0" v-model.number="form.minSpend" class="form-input" placeholder="0.00" />
          </div>
          <div class="form-group">
            <label class="form-label">Max Discount Cap ($)</label>
            <input type="number" step="0.01" min="0" v-model.number="form.maxDiscount" class="form-input" placeholder="Leave empty for no cap" />
          </div>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label class="form-label">Usage Limit (Max Uses)</label>
            <input type="number" min="1" v-model.number="form.usageLimit" class="form-input" placeholder="Leave empty for unlimited" />
          </div>
          <div class="form-group flex-center">
            <label class="checkbox-label">
              <input type="checkbox" v-model="form.isActive" />
              <span>Is Active</span>
            </label>
          </div>
        </div>

        <div class="modal-actions">
          <button type="button" @click="closeModal" class="btn btn-secondary">Cancel</button>
          <button type="submit" class="btn btn-primary" :disabled="saving">
            {{ saving ? 'Saving...' : 'Save Promo Code' }}
          </button>
        </div>
      </form>
    </Modal>

    <Toast ref="toastRef" />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { PromoCodeApi } from '../../services'
import type { PromoCodeResponse } from '../../services'
import { DiscountType } from '../../constants/enums'
import Modal from '../../components/Modal.vue'
import Toast from '../../components/Toast.vue'
import { getStoredUser } from '../../helpers/auth.helper'

const promoCodes = ref<PromoCodeResponse[]>([])
const loading = ref(true)
const saving = ref(false)
const isModalOpen = ref(false)
const editingId = ref<number | null>(null)
const toastRef = ref<InstanceType<typeof Toast> | null>(null)

const currentUser = getStoredUser()

const form = reactive({
  code: '',
  discountType: DiscountType.PERCENTAGE,
  discountValue: 10,
  minSpend: 0,
  maxDiscount: undefined as number | undefined,
  usageLimit: undefined as number | undefined,
  isActive: true
})

const hasPermission = (moduleName: string, action: string) => {
  if (!currentUser) return false
  if (currentUser.role === 'ROLE_SUPERADMIN') return true
  if (!currentUser.permissions) return false
  const target = `${moduleName}_${action}`
  return currentUser.permissions.includes(target)
}

const fetchPromoCodes = async () => {
  loading.value = true
  try {
    promoCodes.value = await PromoCodeApi.getAllPromoCodes()
  } catch (err) {
    console.error('Failed to load promo codes:', err)
  } finally {
    loading.value = false
  }
}

const openCreateModal = () => {
  editingId.value = null
  form.code = ''
  form.discountType = DiscountType.PERCENTAGE
  form.discountValue = 10
  form.minSpend = 0
  form.maxDiscount = undefined
  form.usageLimit = undefined
  form.isActive = true
  isModalOpen.value = true
}

const openEditModal = (promo: PromoCodeResponse) => {
  editingId.value = promo.id
  form.code = promo.code
  form.discountType = promo.discountType
  form.discountValue = promo.discountValue
  form.minSpend = promo.minSpend || 0
  form.maxDiscount = promo.maxDiscount
  form.usageLimit = promo.usageLimit
  form.isActive = promo.isActive
  isModalOpen.value = true
}

const closeModal = () => {
  isModalOpen.value = false
}

const savePromoCode = async () => {
  saving.value = true
  try {
    const payload = {
      code: form.code,
      discountType: form.discountType,
      discountValue: form.discountValue,
      minSpend: form.minSpend,
      maxDiscount: form.maxDiscount,
      usageLimit: form.usageLimit,
      isActive: form.isActive
    }

    if (editingId.value) {
      await PromoCodeApi.updatePromoCode(editingId.value, payload)
      toastRef.value?.show('Promo code updated')
    } else {
      await PromoCodeApi.createPromoCode(payload)
      toastRef.value?.show('Promo code created')
    }
    closeModal()
    fetchPromoCodes()
  } catch (err: any) {
    toastRef.value?.show(err.response?.data?.message || err.message || 'Failed to save promo code', 'error')
  } finally {
    saving.value = false
  }
}

const deletePromo = async (id: number) => {
  if (!confirm('Are you sure you want to delete this promo code?')) return
  try {
    await PromoCodeApi.deletePromoCode(id)
    toastRef.value?.show('Promo code deleted')
    fetchPromoCodes()
  } catch (err: any) {
    toastRef.value?.show('Failed to delete promo code', 'error')
  }
}

onMounted(() => {
  fetchPromoCodes()
})
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.page-title {
  font-size: 2rem;
  font-weight: 800;
}

.page-subtitle {
  color: var(--text-muted);
}

.main-panel {
  padding: 1.5rem;
}

.font-bold {
  font-weight: 700;
}

.promo-code-text {
  color: #38bdf8;
  letter-spacing: 0.05em;
}

.badge-type {
  font-size: 0.75rem;
  font-weight: 700;
  padding: 0.2rem 0.5rem;
  border-radius: 4px;
  background: var(--bg-surface-elevated);
  border: 1px solid var(--border-glass);
}

.status-pill {
  font-size: 0.75rem;
  font-weight: 700;
  padding: 0.25rem 0.6rem;
  border-radius: var(--radius-full);
}

.status-pill.active {
  background: rgba(56, 189, 248, 0.15);
  color: #38bdf8;
}

.status-pill.inactive {
  background: rgba(239, 68, 68, 0.15);
  color: #ef4444;
}

.action-btn-group {
  display: flex;
  gap: 0.5rem;
  align-items: center;
  white-space: nowrap;
}

.uppercase {
  text-transform: uppercase;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.flex-center {
  display: flex;
  align-items: center;
  padding-top: 1.5rem;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
  font-weight: 600;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  margin-top: 1.5rem;
}
</style>
