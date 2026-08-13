<template>
  <div class="page-container container">
    <section class="hero-section glass-panel">
      <div class="hero-content">
        <span class="hero-tag">ENTERPRISE PREMIER BOOKINGS</span>
        <h1 class="hero-title">Reserve Premium Venues, Suites & Equipment</h1>
        <p class="hero-subtitle">Instant real-time availability, effortless checkouts, and seamless management.</p>
      </div>
    </section>

    <div class="filter-bar">
      <div class="categories">
        <button
          v-for="cat in categories"
          :key="cat"
          @click="selectCategory(cat)"
          class="btn btn-sm"
          :class="selectedCategory === cat ? 'btn-primary' : 'btn-secondary'"
        >
          {{ cat }}
        </button>
      </div>
    </div>

    <div v-if="loading" class="loading-spinner">Loading available products...</div>

    <div v-else-if="products.length === 0" class="empty-state glass-card">
      <p>No available booking options found for this category.</p>
    </div>

    <div v-else class="product-grid">
      <ProductCard
        v-for="product in products"
        :key="product.id"
        :product="product"
        @add-to-cart="openAddToCartModal"
      />
    </div>

    <DialogPlus 
      :isOpen="isModalOpen" 
      title="Book Service / Product" 
      cancelText="Cancel"
      :cancelAction="() => isModalOpen = false"
      continueText="Confirm & Add to Cart"
      :continueAction="submitAddToCart"
      :submitting="submitting"
      :continueDisabled="checkingAvailability || (availability !== null && availability.availableSlots <= 0)"
      @close="isModalOpen = false"
    >
      <div v-if="selectedProduct" class="booking-modal-body">
        <h4>{{ selectedProduct.name }}</h4>
        <p class="modal-price">${{ selectedProduct.price }} / day <small style="opacity: 0.7;">(Max {{ selectedProduct.capacity }} guests)</small></p>

        <div class="form-group" style="margin-top: 1rem;">
          <label class="form-label">Booking Date</label>
          <input v-model="bookingForm.bookingDate" @change="checkAvailability" type="date" class="form-input" :min="minDate" required />
        </div>

        <div v-if="checkingAvailability" class="availability-status" style="color: var(--text-muted); font-size: 0.85rem; margin-bottom: 0.75rem;">
          Checking daily availability...
        </div>
        <div v-else-if="availability" class="availability-status" style="margin-bottom: 0.75rem;">
          <span v-if="availability.availableSlots > 0" class="badge badge-success" style="background: rgba(52, 211, 153, 0.15); color: #34d399; padding: 0.35rem 0.65rem; border-radius: 6px; font-weight: 600;">
            ✓ {{ availability.availableSlots }} unit(s) available for {{ bookingForm.bookingDate }}
          </span>
          <span v-else class="badge badge-danger" style="background: rgba(248, 113, 113, 0.15); color: #f87171; padding: 0.35rem 0.65rem; border-radius: 6px; font-weight: 600;">
            ✕ Sold out for {{ bookingForm.bookingDate }}
          </span>
        </div>

        <div class="form-group">
          <label class="form-label">Quantity / Units Needed</label>
          <input 
            v-model.number="bookingForm.quantity" 
            type="number" 
            min="1" 
            :max="availability ? availability.availableSlots : (selectedProduct.stockQuantity || 10)" 
            class="form-input" 
            :disabled="availability !== null && availability.availableSlots <= 0"
            required 
          />
        </div>
      </div>
    </DialogPlus>

    <Toast ref="toastRef" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import ProductCard from '../../components/ProductCard.vue'
import DialogPlus from '../../components/DialogPlus.vue'
import Toast from '../../components/Toast.vue'
import { ProductApi, CartApi } from '../../services'
import type { ProductDTO } from '../../services'
import { isAuthenticated } from '../../helpers/auth.helper'

const router = useRouter()
const products = ref<ProductDTO[]>([])
const loading = ref(true)
const categories = ['All', 'Rooms', 'Venues', 'Studios', 'Equipment', 'Workspace']
const selectedCategory = ref('All')

const isModalOpen = ref(false)
const selectedProduct = ref<ProductDTO | null>(null)
const submitting = ref(false)
const toastRef = ref<InstanceType<typeof Toast> | null>(null)

const today = new Date().toISOString().split('T')[0]
const minDate = ref(today)

const bookingForm = ref({
  bookingDate: today,
  quantity: 1
})

const fetchProducts = async (cat = 'All') => {
  loading.value = true
  try {
    const categoryQuery = cat === 'All' ? undefined : cat
    products.value = await ProductApi.getAllProducts(categoryQuery)
  } catch (err) {
    console.error('Error fetching products:', err)
  } finally {
    loading.value = false
  }
}

const selectCategory = (cat: string) => {
  selectedCategory.value = cat
  fetchProducts(cat)
}

const availability = ref<any>(null)
const checkingAvailability = ref(false)

const checkAvailability = async () => {
  if (!selectedProduct.value || !bookingForm.value.bookingDate) return
  checkingAvailability.value = true
  try {
    const res = await ProductApi.getProductAvailability(selectedProduct.value.id, bookingForm.value.bookingDate)
    availability.value = res
    if (res.availableSlots < bookingForm.value.quantity) {
      bookingForm.value.quantity = Math.max(1, res.availableSlots)
    }
  } catch (err) {
    console.error('Failed to check availability:', err)
  } finally {
    checkingAvailability.value = false
  }
}

const openAddToCartModal = (product: ProductDTO) => {
  if (!isAuthenticated()) {
    router.push('/login')
    return
  }
  selectedProduct.value = product
  availability.value = null
  bookingForm.value = {
    bookingDate: today,
    quantity: 1
  }
  isModalOpen.value = true
  checkAvailability()
}

const submitAddToCart = async () => {
  if (!bookingForm.value.bookingDate || !selectedProduct.value) return
  submitting.value = true
  try {
    await CartApi.addToCart({
      productId: selectedProduct.value.id,
      quantity: bookingForm.value.quantity,
      bookingDate: bookingForm.value.bookingDate
    })
    isModalOpen.value = false
    toastRef.value?.show('Added to cart successfully!')
  } catch (err: any) {
    toastRef.value?.show(err.response?.data?.message || 'Failed to add to cart', 'error')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchProducts()
})
</script>

<style scoped>
.page-container {
  padding-top: 1.5rem;
}

.hero-section {
  padding: 3.5rem 2.5rem;
  margin-bottom: 2.5rem;
}

.hero-tag {
  font-size: 0.75rem;
  font-weight: 800;
  letter-spacing: 0.1em;
  color: var(--accent-secondary);
  margin-bottom: 0.5rem;
  display: block;
}

.hero-title {
  font-size: 2.25rem;
  font-weight: 800;
  color: var(--text-main);
  margin-bottom: 0.75rem;
}

.hero-subtitle {
  font-size: 1.05rem;
  color: var(--text-muted);
  max-width: 650px;
}

.filter-bar {
  margin-bottom: 2rem;
}

.categories {
  display: flex;
  gap: 0.65rem;
  flex-wrap: wrap;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 1.75rem;
}

.loading-spinner, .empty-state {
  padding: 4rem;
  text-align: center;
  color: var(--text-muted);
  font-size: 1.1rem;
}

.modal-price {
  font-size: 1.25rem;
  font-weight: 800;
  color: #38bdf8;
  margin-top: 0.25rem;
}

.modal-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
  margin-top: 1.75rem;
}
</style>
