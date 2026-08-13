<template>
  <div class="product-management-page">
    <div class="page-header">
      <div>
        <h1 class="page-title">Product & Service Catalog (CRUD)</h1>
        <p class="page-subtitle">Manage rentable spaces, rooms, studios, and equipment items</p>
      </div>
      <button v-if="hasPermission('PRODUCTS', 'CREATE')" @click="openCreateModal" class="btn btn-primary">
        + Add New Product
      </button>
    </div>

    <div v-if="loading" class="loading">Loading catalog items...</div>

    <div v-else class="glass-panel content-card">
      <div class="table-container">
        <table class="custom-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Product / Service</th>
              <th>Category</th>
              <th>Price</th>
              <th>Capacity</th>
              <th>Status</th>
              <th style="text-align: right;">Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="p in products" :key="p.id">
              <td>#{{ p.id }}</td>
              <td class="product-cell">
                <img :src="p.imageUrl || defaultImage" class="cell-thumb" />
                <span class="font-bold">{{ p.name }}</span>
              </td>
              <td>
                <span class="badge" :style="getCategoryStyle(p.category)">{{ p.category }}</span>
              </td>
              <td class="font-bold">${{ p.price.toFixed(2) }}</td>
              <td>👥 {{ p.capacity }}</td>
              <td><span class="status-indicator" :class="p.status?.toLowerCase()">{{ p.status }}</span></td>
              <td style="text-align: right;">
                <div class="action-btn-group">
                  <button v-if="hasPermission('PRODUCTS', 'UPDATE')" @click="openEditModal(p)" class="btn btn-secondary btn-sm">Edit</button>
                  <button v-if="hasPermission('PRODUCTS', 'DELETE')" @click="deleteProduct(p.id)" class="btn btn-danger btn-sm">Delete</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <DialogPlus 
      :isOpen="isModalOpen" 
      :title="isEditMode ? 'Edit Product' : 'Add New Product'" 
      cancelText="Cancel"
      :cancelAction="() => isModalOpen = false"
      :continueText="isEditMode ? 'Update Product' : 'Create Product'"
      :continueAction="saveProduct"
      :submitting="submitting"
      @close="isModalOpen = false"
    >
      <form @submit.prevent="saveProduct">
        <div class="form-group">
          <label class="form-label">Product/Service Name *</label>
          <input v-model="form.name" type="text" class="form-input" required placeholder="e.g. Grand Executive Suite" />
        </div>

        <div class="form-row">
          <div class="form-group">
            <label class="form-label">Category *</label>
            <select v-model="form.category" class="form-select" required>
              <option value="Rooms">Rooms</option>
              <option value="Venues">Venues</option>
              <option value="Studios">Studios</option>
              <option value="Equipment">Equipment</option>
              <option value="Workspace">Workspace</option>
            </select>
          </div>

          <div class="form-group">
            <label class="form-label">Price ($/day) *</label>
            <input v-model.number="form.price" type="number" step="0.01" min="0" class="form-input" required />
          </div>

          <div class="form-group">
            <label class="form-label">Capacity *</label>
            <input v-model.number="form.capacity" type="number" min="1" class="form-input" required />
          </div>
        </div>

        <div class="form-group">
          <label class="form-label">Image URL</label>
          <input v-model="form.imageUrl" type="url" class="form-input" placeholder="https://..." />
        </div>

        <div class="form-group">
          <label class="form-label">Description</label>
          <textarea v-model="form.description" class="form-textarea" rows="3"></textarea>
        </div>

        <div class="form-group">
          <label class="form-label">Availability Status</label>
          <select v-model="form.status" class="form-select">
            <option value="AVAILABLE">AVAILABLE</option>
            <option value="UNAVAILABLE">UNAVAILABLE</option>
            <option value="MAINTENANCE">MAINTENANCE</option>
          </select>
        </div>
      </form>
    </DialogPlus>

    <Toast ref="toastRef" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { AdminApi } from '../../services'
import type { ProductDTO } from '../../services'
import DialogPlus from '../../components/DialogPlus.vue'
import Toast from '../../components/Toast.vue'
import { getCategoryBadgeColor } from '../../helpers/color.helper'
import { hasPermission } from '../../helpers/auth.helper'

const products = ref<ProductDTO[]>([])
const loading = ref(true)
const submitting = ref(false)
const isModalOpen = ref(false)
const isEditMode = ref(false)
const editingId = ref<number | null>(null)
const toastRef = ref<InstanceType<typeof Toast> | null>(null)

const defaultImage = 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?auto=format&fit=crop&w=800&q=80'

const form = ref<Partial<ProductDTO>>({
  name: '',
  category: 'Rooms',
  price: 0,
  capacity: 1,
  imageUrl: '',
  description: '',
  status: 'AVAILABLE'
})

const fetchProducts = async () => {
  loading.value = true
  try {
    products.value = await AdminApi.getAllProducts()
  } catch (err) {
    console.error('Failed to fetch products:', err)
  } finally {
    loading.value = false
  }
}

const openCreateModal = () => {
  isEditMode.value = false
  editingId.value = null
  form.value = {
    name: '',
    category: 'Rooms',
    price: 99.00,
    capacity: 2,
    imageUrl: '',
    description: '',
    status: 'AVAILABLE'
  }
  isModalOpen.value = true
}

const openEditModal = (product: ProductDTO) => {
  isEditMode.value = true
  editingId.value = product.id
  form.value = { ...product }
  isModalOpen.value = true
}

const saveProduct = async () => {
  submitting.value = true
  try {
    if (isEditMode.value && editingId.value) {
      const updated = await AdminApi.updateProduct(editingId.value, form.value as ProductDTO)
      const index = products.value.findIndex(p => p.id === editingId.value)
      if (index !== -1) products.value[index] = updated
      toastRef.value?.show('Product updated successfully!')
    } else {
      const created = await AdminApi.createProduct(form.value as ProductDTO)
      products.value.unshift(created)
      toastRef.value?.show('Product created successfully!')
    }
    isModalOpen.value = false
  } catch (err: any) {
    toastRef.value?.show(err.response?.data?.message || 'Failed to save product', 'error')
  } finally {
    submitting.value = false
  }
}

const deleteProduct = async (id: number) => {
  if (!confirm('Are you sure you want to delete this product?')) return
  try {
    await AdminApi.deleteProduct(id)
    products.value = products.value.filter(p => p.id !== id)
    toastRef.value?.show('Product deleted')
  } catch (err) {
    toastRef.value?.show('Failed to delete product', 'error')
  }
}

const getCategoryStyle = (cat: string) => {
  const badge = getCategoryBadgeColor(cat)
  return { backgroundColor: badge.bg, color: badge.color }
}

onMounted(() => {
  fetchProducts()
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

.content-card {
  padding: 1.5rem;
}

.product-cell {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.cell-thumb {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-sm);
  object-fit: cover;
}

.font-bold {
  font-weight: 700;
}

.status-indicator {
  font-size: 0.75rem;
  font-weight: 700;
}

.status-indicator.available {
  color: #34d399;
}

.status-indicator.unavailable {
  color: #f87171;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.modal-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
  margin-top: 1.5rem;
}

.action-btn-group {
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
}
</style>
