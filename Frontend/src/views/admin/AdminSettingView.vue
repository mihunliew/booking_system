<template>
  <div class="admin-settings-page">
    <div class="page-header">
      <h1 class="page-title">General Settings</h1>
      <p class="page-subtitle">Manage payment methods and system configuration (SuperAdmin Only)</p>
    </div>

    <div v-if="loading" class="loading">Loading settings...</div>

    <div v-else class="settings-content glass-panel">
      <div class="table-header">
        <h2>Payment Methods</h2>
        <button @click="openModal()" class="btn btn-primary btn-sm">+ Add Method</button>
      </div>

      <div class="table-container">
        <table class="custom-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Icon</th>
              <th>Name</th>
              <th>Provider Key</th>
              <th>Status</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="setting in settings" :key="setting.id">
              <td>{{ setting.id }}</td>
              <td class="icon-cell">{{ setting.icon }}</td>
              <td class="font-bold">{{ setting.name }}</td>
              <td><code>{{ setting.providerKey }}</code></td>
              <td>
                <span class="status-badge" :class="setting.active ? 'active' : 'inactive'">
                  {{ setting.active ? 'Active' : 'Disabled' }}
                </span>
              </td>
              <td class="actions-cell">
                <button @click="openModal(setting)" class="btn btn-secondary btn-sm">Edit</button>
                <button @click="deleteSetting(setting.id)" class="btn btn-danger btn-sm">Delete</button>
              </td>
            </tr>
            <tr v-if="settings.length === 0">
              <td colspan="6" style="text-align: center; padding: 2rem;">No settings found</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Modal -->
    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-content glass-panel">
        <div class="modal-header">
          <h2>{{ editingSetting ? 'Edit Payment Method' : 'Add Payment Method' }}</h2>
          <button class="close-btn" @click="closeModal">&times;</button>
        </div>
        
        <form @submit.prevent="saveSetting" class="modal-form">
          <div class="form-group">
            <label class="form-label">Name</label>
            <input v-model="form.name" type="text" class="form-input" required placeholder="e.g. Credit Card" />
          </div>
          
          <div class="form-group">
            <label class="form-label">Provider Key (Stripe)</label>
            <input v-model="form.providerKey" type="text" class="form-input" required placeholder="e.g. card, fpx" />
          </div>

          <div class="form-group">
            <label class="form-label">Icon (Emoji)</label>
            <input v-model="form.icon" type="text" class="form-input" placeholder="e.g. 💳" />
          </div>

          <div class="form-group">
            <label class="form-label">Description</label>
            <input v-model="form.description" type="text" class="form-input" placeholder="e.g. Pay securely via card" />
          </div>

          <div class="form-group checkbox-group">
            <label class="form-label" style="display: flex; align-items: center; gap: 0.5rem; cursor: pointer;">
              <input v-model="form.active" type="checkbox" />
              <span>Is Active?</span>
            </label>
          </div>

          <div class="modal-actions">
            <button type="button" @click="closeModal" class="btn btn-secondary">Cancel</button>
            <button type="submit" class="btn btn-primary" :disabled="submitting">
              {{ submitting ? 'Saving...' : 'Save Method' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <Toast ref="toastRef" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { SettingApi } from '../../services'
import type { SettingResponse, SettingRequest } from '../../services'
import Toast from '../../components/Toast.vue'

const settings = ref<SettingResponse[]>([])
const loading = ref(true)
const submitting = ref(false)
const toastRef = ref<InstanceType<typeof Toast> | null>(null)

const showModal = ref(false)
const editingSetting = ref<SettingResponse | null>(null)

const form = ref<SettingRequest>({
  settingType: 'PAYMENT_METHOD',
  name: '',
  providerKey: '',
  icon: '',
  description: '',
  active: true
})

const fetchSettings = async () => {
  loading.value = true
  try {
    settings.value = await SettingApi.getAllSettings()
  } catch (err: any) {
    toastRef.value?.show(err.response?.data?.message || 'Failed to load settings', 'error')
  } finally {
    loading.value = false
  }
}

const openModal = (setting?: SettingResponse) => {
  if (setting) {
    editingSetting.value = setting
    form.value = { ...setting }
  } else {
    editingSetting.value = null
    form.value = {
      settingType: 'PAYMENT_METHOD',
      name: '',
      providerKey: '',
      icon: '',
      description: '',
      active: true
    }
  }
  showModal.value = true
}

const closeModal = () => {
  showModal.value = false
  editingSetting.value = null
}

const saveSetting = async () => {
  submitting.value = true
  try {
    if (editingSetting.value) {
      await SettingApi.updateSetting(editingSetting.value.id, form.value)
      toastRef.value?.show('Setting updated successfully')
    } else {
      await SettingApi.createSetting(form.value)
      toastRef.value?.show('Setting created successfully')
    }
    closeModal()
    fetchSettings()
  } catch (err: any) {
    toastRef.value?.show(err.response?.data?.message || 'Operation failed', 'error')
  } finally {
    submitting.value = false
  }
}

const deleteSetting = async (id: number) => {
  if (!confirm('Are you sure you want to delete this payment method?')) return
  try {
    await SettingApi.deleteSetting(id)
    toastRef.value?.show('Setting deleted')
    fetchSettings()
  } catch (err: any) {
    toastRef.value?.show(err.response?.data?.message || 'Delete failed', 'error')
  }
}

onMounted(() => {
  fetchSettings()
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
.settings-content {
  padding: 1.5rem;
}
.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}
.font-bold {
  font-weight: 700;
}
.icon-cell {
  font-size: 1.5rem;
}
.actions-cell {
  display: flex;
  gap: 0.5rem;
}
.status-badge {
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  font-size: 0.75rem;
  font-weight: 600;
}
.status-badge.active {
  background: rgba(16, 185, 129, 0.1);
  color: #10b981;
}
.status-badge.inactive {
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
}
/* Modal styles */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  backdrop-filter: blur(4px);
}
.modal-content {
  background: var(--bg-surface-elevated);
  padding: 2rem;
  border-radius: var(--radius-lg);
  width: 100%;
  max-width: 500px;
  border: 1px solid var(--border-glass);
}
.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}
.modal-header h2 {
  font-size: 1.25rem;
  font-weight: 700;
}
.close-btn {
  background: none;
  border: none;
  font-size: 1.5rem;
  color: var(--text-muted);
  cursor: pointer;
}
.close-btn:hover {
  color: var(--text-main);
}
.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  margin-top: 2rem;
}
.checkbox-group {
  margin-top: 1rem;
}
</style>
