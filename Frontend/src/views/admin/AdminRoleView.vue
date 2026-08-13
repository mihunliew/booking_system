<template>
  <div class="role-management-page">
    <div class="page-header">
      <h1 class="page-title">Role & Permission Management</h1>
      <p class="page-subtitle">Configure custom admin roles and access control</p>
    </div>

    <div v-if="loading" class="loading">Loading roles...</div>

    <div v-else class="glass-panel content-card">
      <div class="header-actions">
        <button v-if="isSuperAdmin()" @click="openCreateModal" class="btn btn-primary">
          + Add New Role
        </button>
      </div>

      <div class="table-container" style="margin-top: 1rem;">
        <table class="custom-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Role Name</th>
              <th>Description</th>
              <th style="text-align: right;">Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="r in roles" :key="r.id">
              <td>#{{ r.id }}</td>
              <td class="font-bold">{{ r.name }}</td>
              <td>{{ r.description }}</td>
              <td style="text-align: right;">
                <div class="action-btn-group">
                  <button v-if="isSuperAdmin()" @click="openEditModal(r)" class="btn btn-secondary btn-sm">Configure Permissions</button>
                  <button v-if="isSuperAdmin()" @click="deleteRole(r.id)" class="btn btn-danger btn-sm">Delete</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Create/Edit Role Dialog -->
    <DialogPlus 
      :isOpen="isModalOpen" 
      :title="isEditMode ? 'Edit Admin Role' : 'Create Admin Role'" 
      cancelText="Cancel"
      :cancelAction="() => isModalOpen = false"
      continueText="Save Role"
      :continueAction="saveRole"
      :submitting="submitting"
      @close="isModalOpen = false"
    >
      <form @submit.prevent="saveRole">
        <div class="form-group">
          <label class="form-label">Role Name *</label>
          <input v-model="form.name" type="text" class="form-input" required placeholder="e.g. Finance Manager" />
        </div>

        <div class="form-group">
          <label class="form-label">Description</label>
          <input v-model="form.description" type="text" class="form-input" placeholder="Role responsibilities..." />
        </div>

        <h4 class="section-title">Module Permissions</h4>
        <div class="permission-matrix">
          <table class="matrix-table">
            <thead>
              <tr>
                <th>Module</th>
                <th>Read</th>
                <th>Create</th>
                <th>Update</th>
                <th>Delete</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="mod in availableModules" :key="mod">
                <td class="font-bold">{{ mod }}</td>
                <td><input type="checkbox" v-model="getPermissionRef(mod).canRead" /></td>
                <td><input type="checkbox" v-model="getPermissionRef(mod).canCreate" /></td>
                <td><input type="checkbox" v-model="getPermissionRef(mod).canUpdate" /></td>
                <td><input type="checkbox" v-model="getPermissionRef(mod).canDelete" /></td>
              </tr>
            </tbody>
          </table>
        </div>
      </form>
    </DialogPlus>

    <Toast ref="toastRef" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { AdminApi } from '../../services'
import type { AdminRoleResponse, AdminRoleRequest } from '../../services/admin.api'
import type { AdminPermissionDTO } from '../../dto/auth.dto'
import DialogPlus from '../../components/DialogPlus.vue'
import Toast from '../../components/Toast.vue'
import { isSuperAdmin } from '../../helpers/auth.helper'

const roles = ref<AdminRoleResponse[]>([])
const loading = ref(true)
const submitting = ref(false)
const isModalOpen = ref(false)
const isEditMode = ref(false)
const editingId = ref<number | null>(null)
const toastRef = ref<InstanceType<typeof Toast> | null>(null)

const availableModules = ['PRODUCTS', 'BOOKINGS', 'PAYMENTS', 'PROMOCODES', 'USERS', 'ROLES']

const form = ref<AdminRoleRequest>({
  name: '',
  description: '',
  permissions: []
})

const getPermissionRef = (moduleName: string): AdminPermissionDTO => {
  let perm = form.value.permissions.find(p => p.moduleName === moduleName)
  if (!perm) {
    perm = { moduleName, canRead: false, canCreate: false, canUpdate: false, canDelete: false }
    form.value.permissions.push(perm)
  }
  return perm
}

const fetchRoles = async () => {
  loading.value = true
  try {
    roles.value = await AdminApi.getAllRoles()
  } catch (err) {
    console.error('Failed to fetch roles:', err)
  } finally {
    loading.value = false
  }
}

const openCreateModal = () => {
  isEditMode.value = false
  editingId.value = null
  form.value = {
    name: '',
    description: '',
    permissions: availableModules.map(m => ({ moduleName: m, canRead: false, canCreate: false, canUpdate: false, canDelete: false }))
  }
  isModalOpen.value = true
}

const openEditModal = (role: AdminRoleResponse) => {
  isEditMode.value = true
  editingId.value = role.id
  // Deep clone to avoid mutating table immediately
  form.value = JSON.parse(JSON.stringify(role))
  
  // Ensure all modules exist in form.permissions
  availableModules.forEach(m => getPermissionRef(m))
  
  isModalOpen.value = true
}

const saveRole = async () => {
  submitting.value = true
  try {
    if (isEditMode.value && editingId.value) {
      const updated = await AdminApi.updateRole(editingId.value, form.value)
      const index = roles.value.findIndex(r => r.id === editingId.value)
      if (index !== -1) roles.value[index] = updated
      toastRef.value?.show('Role updated successfully!')
    } else {
      const created = await AdminApi.createRole(form.value)
      roles.value.unshift(created)
      toastRef.value?.show('Role created successfully!')
    }
    isModalOpen.value = false
  } catch (err: any) {
    toastRef.value?.show(err.response?.data?.message || 'Failed to save role', 'error')
  } finally {
    submitting.value = false
  }
}

const deleteRole = async (id: number) => {
  if (!confirm('Are you sure you want to delete this role?')) return
  try {
    await AdminApi.deleteRole(id)
    roles.value = roles.value.filter(r => r.id !== id)
    toastRef.value?.show('Role deleted')
  } catch (err) {
    toastRef.value?.show('Failed to delete role', 'error')
  }
}

onMounted(() => {
  fetchRoles()
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

.header-actions {
  display: flex;
  justify-content: flex-end;
}

.font-bold {
  font-weight: 700;
}

.action-btn-group {
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
}

.permissions-container {
  max-height: 300px;
  overflow-y: auto;
  border: 1px solid var(--border-glass);
  border-radius: var(--radius-md);
  margin-bottom: 1.5rem;
}

.permission-table th, .permission-table td {
  text-align: center;
}
.permission-table td:first-child, .permission-table th:first-child {
  text-align: left;
}

.modal-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

input[type="checkbox"] {
  width: 1.25rem;
  height: 1.25rem;
  accent-color: #6366f1;
}
</style>
