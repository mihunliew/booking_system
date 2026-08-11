<template>
  <div class="container page-container">
    <button @click="router.back()" class="btn btn-secondary btn-sm back-btn">
      &larr; Back to Listings
    </button>

    <div v-if="loading" class="loading">Loading details...</div>

    <div v-else-if="product" class="detail-grid">
      <div class="image-column glass-panel">
        <img :src="product.imageUrl || defaultImage" :alt="product.name" class="detail-image" />
      </div>

      <div class="info-column glass-panel">
        <span class="category-badge">{{ product.category }}</span>
        <h1 class="product-title">{{ product.name }}</h1>
        <p class="price-display">${{ product.price }} <small>/ day</small></p>

        <p class="description">{{ product.description }}</p>

        <div class="specs-grid">
          <div class="spec-card glass-card">
            <span class="spec-label">Capacity Limit</span>
            <span class="spec-value">👥 {{ product.capacity }} Persons</span>
          </div>
          <div class="spec-card glass-card">
            <span class="spec-label">Status</span>
            <span class="spec-value">🟢 {{ product.status }}</span>
          </div>
        </div>

        <div class="booking-box">
          <h3>Reserve Slot</h3>
          <div class="form-group">
            <label class="form-label">Select Date</label>
            <input v-model="bookingDate" type="date" class="form-input" :min="minDate" />
          </div>

          <div class="form-group">
            <label class="form-label">Quantity</label>
            <input v-model.number="quantity" type="number" min="1" :max="product.capacity" class="form-input" />
          </div>

          <button @click="addToCart" class="btn btn-primary btn-full btn-lg" :disabled="submitting">
            {{ submitting ? 'Adding...' : 'Add to Cart' }}
          </button>
        </div>
      </div>
    </div>

    <Toast ref="toastRef" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ProductApi, CartApi } from '../../services'
import type { ProductDTO } from '../../services'
import { isAuthenticated } from '../../helpers/auth.helper'
import Toast from '../../components/Toast.vue'

const route = useRoute()
const router = useRouter()

const product = ref<ProductDTO | null>(null)
const loading = ref(true)
const submitting = ref(false)
const toastRef = ref<InstanceType<typeof Toast> | null>(null)

const today = new Date().toISOString().split('T')[0]
const minDate = ref(today)
const bookingDate = ref(today)
const quantity = ref(1)

const defaultImage = 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?auto=format&fit=crop&w=800&q=80'

const fetchProduct = async () => {
  loading.value = true
  try {
    product.value = await ProductApi.getProductById(route.params.id as string)
  } catch (err) {
    console.error('Failed to load product details:', err)
  } finally {
    loading.value = false
  }
}

const addToCart = async () => {
  if (!isAuthenticated()) {
    router.push('/login')
    return
  }
  
  if (!product.value) return

  submitting.value = true
  try {
    await CartApi.addToCart({
      productId: product.value.id,
      quantity: quantity.value,
      bookingDate: bookingDate.value
    })
    toastRef.value?.show('Added to cart successfully!')
  } catch (err: any) {
    toastRef.value?.show(err.response?.data?.message || 'Failed to add to cart', 'error')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchProduct()
})
</script>

<style scoped>
.page-container {
  padding-top: 1.5rem;
}

.back-btn {
  margin-bottom: 1.5rem;
}

.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 2.5rem;
}

.image-column {
  overflow: hidden;
  height: 450px;
}

.detail-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.info-column {
  padding: 2.5rem;
  display: flex;
  flex-direction: column;
}

.category-badge {
  font-size: 0.75rem;
  font-weight: 800;
  color: var(--accent-secondary);
  letter-spacing: 0.05em;
  text-transform: uppercase;
  margin-bottom: 0.5rem;
}

.product-title {
  font-size: 2rem;
  font-weight: 800;
  color: var(--text-main);
  margin-bottom: 0.5rem;
}

.price-display {
  font-size: 1.75rem;
  font-weight: 800;
  color: #38bdf8;
  margin-bottom: 1.5rem;
}

.price-display small {
  font-size: 0.9rem;
  color: var(--text-muted);
}

.description {
  font-size: 0.95rem;
  color: var(--text-muted);
  line-height: 1.7;
  margin-bottom: 2rem;
}

.specs-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
  margin-bottom: 2rem;
}

.spec-card {
  padding: 1rem;
  display: flex;
  flex-direction: column;
}

.spec-label {
  font-size: 0.75rem;
  color: var(--text-muted);
}

.spec-value {
  font-size: 0.95rem;
  font-weight: 700;
  color: var(--text-main);
}

.booking-box {
  margin-top: auto;
  padding-top: 1.5rem;
  border-top: 1px solid var(--border-glass);
}

.booking-box h3 {
  font-size: 1.1rem;
  margin-bottom: 1rem;
}
</style>
