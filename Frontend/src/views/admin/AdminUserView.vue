<template>
  <div class="user-management-page">
    <div class="page-header" style="display: flex; justify-content: space-between; align-items: center;">
      <div>
        <h1 class="page-title">User Account Management</h1>
        <p class="page-subtitle">View system users, modify permissions, and handle account operations</p>
      </div>
      <button v-if="hasPermission('USERS', 'CREATE') || isSuperAdmin()" @click="openCreateModal" class="btn btn-primary">
        + Create User
      </button>
    </div>

    <div v-if="loading" class="loading">Loading user records...</div>

    <div v-else class="glass-panel content-card">
      <div class="table-container">
        <table class="custom-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Username</th>
              <th>Full Name</th>
              <th>Email</th>
              <th>Role</th>
              <th>Joined Date</th>
              <th style="text-align: right;">Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="u in users" :key="u.id">
              <td>#{{ u.id }}</td>
              <td class="font-bold">{{ u.username }}</td>
              <td>{{ u.fullName }}</td>
              <td>{{ u.email }}</td>
              <td><StatusBadge type="role" :value="u.role" /></td>
              <td>{{ formatDate(u.createdAt) }}</td>
              <td style="text-align: right;">
                <div class="action-btn-group">
                  <button v-if="hasPermission('USERS', 'UPDATE')" @click="openRoleModal(u)" class="btn btn-secondary btn-sm">
                    Manage Role
                  </button>
                  <button v-if="hasPermission('USERS', 'DELETE')" @click="deleteUser(u.id)" class="btn btn-danger btn-sm">
                    Delete
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <Modal :isOpen="isRoleModalOpen" title="Manage User Role" @close="isRoleModalOpen = false">
      <div v-if="selectedUser">
        <p><strong>Username:</strong> {{ selectedUser.username }}</p>
        <p><strong>Current Base Role:</strong> {{ selectedUser.role }}</p>
        
        <div class="form-group" style="margin-top: 1rem;">
          <label class="form-label">Base Role</label>
          <select v-model="roleForm.baseRole" class="form-select">
            <option value="ROLE_USER">USER</option>
            <option value="ROLE_ADMIN">ADMIN</option>
            <option v-if="isSuperAdmin()" value="ROLE_SUPERADMIN">SUPERADMIN</option>
          </select>
        </div>

        <div v-if="roleForm.baseRole === 'ROLE_ADMIN'" class="form-group" style="margin-top: 1rem;">
          <label class="form-label">Custom Admin Role</label>
          <select v-model="roleForm.adminRoleId" class="form-select">
            <option :value="null">-- None (Basic Admin) --</option>
            <option v-for="r in adminRoles" :key="r.id" :value="r.id">{{ r.name }}</option>
          </select>
        </div>

        <div class="modal-actions" style="margin-top: 1.5rem; display: grid; grid-template-columns: 1fr 1fr; gap: 1rem;">
          <button @click="isRoleModalOpen = false" class="btn btn-secondary btn-full">Cancel</button>
          <button @click="submitRoleUpdate" class="btn btn-primary btn-full" :disabled="submitting">
            {{ submitting ? 'Updating...' : 'Save Role' }}
          </button>
        </div>
      </div>
    </Modal>

    <Modal :isOpen="isCreateModalOpen" title="Create New User" @close="isCreateModalOpen = false">
      <div>
        <div class="form-group">
          <label class="form-label">Username</label>
          <input v-model="createForm.username" class="form-input" placeholder="Enter username" />
        </div>
        <div class="form-group" style="margin-top: 1rem;">
          <label class="form-label">Email</label>
          <input v-model="createForm.email" type="email" class="form-input" placeholder="Enter email" />
        </div>
        <div class="form-group" style="margin-top: 1rem;">
          <label class="form-label">Full Name</label>
          <input v-model="createForm.fullName" class="form-input" placeholder="Enter full name" />
        </div>
        <div class="form-group" style="margin-top: 1rem;">
          <label class="form-label">Password</label>
          <input v-model="createForm.password" type="password" class="form-input" placeholder="Enter password" />
        </div>
        <div class="form-group" style="margin-top: 1rem;">
          <label class="form-label">Phone</label>
          <input v-model="createForm.phone" class="form-input" placeholder="Enter phone" />
        </div>
        <div class="form-group" style="margin-top: 1rem;">
          <label class="form-label">Role</label>
          <select v-model="createForm.role" class="form-select">
            <option value="ROLE_USER">USER</option>
            <option value="ROLE_ADMIN">ADMIN</option>
            <option v-if="isSuperAdmin()" value="ROLE_SUPERADMIN">SUPERADMIN</option>
          </select>
        </div>
        <div v-if="createForm.role === 'ROLE_ADMIN'" class="form-group" style="margin-top: 1rem;">
          <label class="form-label">Custom Admin Role</label>
          <select v-model="createForm.adminRoleId" class="form-select">
            <option :value="null">-- None (Basic Admin) --</option>
            <option v-for="r in adminRoles" :key="r.id" :value="r.id">{{ r.name }}</option>
          </select>
        </div>

        <div class="modal-actions" style="margin-top: 1.5rem; display: grid; grid-template-columns: 1fr 1fr; gap: 1rem;">
          <button @click="isCreateModalOpen = false" class="btn btn-secondary btn-full">Cancel</button>
          <button @click="submitCreateUser" class="btn btn-primary btn-full" :disabled="submitting">
            {{ submitting ? 'Creating...' : 'Create User' }}
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
import type { UserResponse } from '../../services'
import StatusBadge from '../../components/StatusBadge.vue'
import Modal from '../../components/Modal.vue'
import Toast from '../../components/Toast.vue'
import { Role } from '../../constants/enums'
import { hasPermission, isSuperAdmin } from '../../helpers/auth.helper'
import type { AdminRoleResponse } from '../../services/admin.api'

const users = ref<UserResponse[]>([])
const adminRoles = ref<AdminRoleResponse[]>([])
const loading = ref(true)
const submitting = ref(false)
const isRoleModalOpen = ref(false)
const isCreateModalOpen = ref(false)
const selectedUser = ref<UserResponse | null>(null)

const roleForm = ref({
  baseRole: 'ROLE_USER',
  adminRoleId: null as number | null
})

const createForm = ref({
  username: '',
  email: '',
  password: '',
  fullName: '',
  phone: '',
  role: 'ROLE_USER',
  adminRoleId: null as number | null
})

const toastRef = ref<InstanceType<typeof Toast> | null>(null)

const fetchUsers = async () => {
  loading.value = true
  try {
    users.value = await AdminApi.getAllUsers()
  } catch (err) {
    console.error('Failed to fetch users:', err)
  } finally {
    loading.value = false
  }
}

const fetchRoles = async () => {
  try {
    adminRoles.value = await AdminApi.getAllRoles()
  } catch (err) {
    console.error('Failed to fetch admin roles', err)
  }
}

const openRoleModal = (user: UserResponse) => {
  selectedUser.value = user
  const r = user.role as unknown as string
  roleForm.value.baseRole = r
  // If user has a custom role, we need to find it in adminRoles
  // We only get 'customRole' (string name) in the UserResponse, not the ID.
  // We can find the ID by matching the name:
  if (user.customRole) {
    const role = adminRoles.value.find(ar => ar.name === user.customRole)
    roleForm.value.adminRoleId = role ? role.id : null
  } else {
    roleForm.value.adminRoleId = null
  }
  isRoleModalOpen.value = true
}

const submitRoleUpdate = async () => {
  if (!selectedUser.value) return
  submitting.value = true
  try {
    const updated = await AdminApi.updateUserRole(
      selectedUser.value.id, 
      roleForm.value.baseRole, 
      roleForm.value.baseRole === 'ROLE_ADMIN' && roleForm.value.adminRoleId ? roleForm.value.adminRoleId : undefined
    )
    
    // Update local state
    const index = users.value.findIndex(u => u.id === selectedUser.value!.id)
    if (index !== -1) users.value[index] = updated
    
    toastRef.value?.show(`Updated ${updated.username}'s role successfully!`)
    isRoleModalOpen.value = false
  } catch (err: any) {
    toastRef.value?.show(err.response?.data?.message || 'Failed to update user role', 'error')
  } finally {
    submitting.value = false
  }
}

const openCreateModal = () => {
  createForm.value = {
    username: '',
    email: '',
    password: '',
    fullName: '',
    phone: '',
    role: 'ROLE_USER',
    adminRoleId: null
  }
  isCreateModalOpen.value = false // force reset if needed, then open
  isCreateModalOpen.value = true
}

const submitCreateUser = async () => {
  if (!createForm.value.username || !createForm.value.email || !createForm.value.password || !createForm.value.fullName) {
    toastRef.value?.show('Please fill in all required fields (username, email, password, full name)', 'error')
    return
  }
  submitting.value = true
  try {
    const payload: any = {
      username: createForm.value.username,
      email: createForm.value.email,
      password: createForm.value.password,
      fullName: createForm.value.fullName,
      phone: createForm.value.phone,
      role: createForm.value.role,
    }
    if (createForm.value.role === 'ROLE_ADMIN' && createForm.value.adminRoleId) {
      payload.adminRoleId = createForm.value.adminRoleId
    }
    const newUser = await AdminApi.createUser(payload)
    users.value.push(newUser)
    toastRef.value?.show(`User ${newUser.username} created successfully!`)
    isCreateModalOpen.value = false
  } catch (err: any) {
    toastRef.value?.show(err.response?.data?.message || 'Failed to create user', 'error')
  } finally {
    submitting.value = false
  }
}

const deleteUser = async (userId: number) => {
  if (!confirm('Are you sure you want to delete this user?')) return
  try {
    await AdminApi.deleteUser(userId)
    users.value = users.value.filter(u => u.id !== userId)
    toastRef.value?.show('User deleted')
  } catch (err) {
    toastRef.value?.show('Failed to delete user', 'error')
  }
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleDateString()
}

onMounted(async () => {
  await fetchRoles()
  await fetchUsers()
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
</style>
