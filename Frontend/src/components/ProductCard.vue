<template>
  <div class="product-card">
    <div class="image-wrapper">
      <img :src="product.imageUrl || defaultImage" :alt="product.name" class="product-image" />
      <span class="category-tag" :style="categoryStyle">{{ product.category }}</span>
    </div>

    <div class="product-body">
      <h3 class="product-title">{{ product.name }}</h3>
      <p class="product-description">{{ truncatedDescription }}</p>

      <div class="product-meta">
        <span class="capacity-info">👥 Capacity: {{ product.capacity }}</span>
        <span class="price-tag">${{ product.price }} <small>/ day</small></span>
      </div>

      <div class="card-actions">
        <router-link :to="`/products/${product.id}`" class="btn btn-secondary btn-sm btn-full">
          View Details
        </router-link>
        <button
          @click="$emit('add-to-cart', product)"
          class="btn btn-sm btn-full"
          :class="isNotAvailable ? 'btn-disabled' : 'btn-primary'"
          :disabled="isNotAvailable"
        >
          {{ isNotAvailable ? (product.status === 'MAINTENANCE' ? 'Maintenance' : 'Unavailable') : 'Book Now' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { getCategoryBadgeColor } from '../helpers/color.helper'
import type { ProductDTO } from '../services'

const props = defineProps<{
  product: ProductDTO
}>()

defineEmits<{
  (e: 'add-to-cart', product: ProductDTO): void
}>()

const defaultImage = 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?auto=format&fit=crop&w=800&q=80'

const categoryStyle = computed(() => {
  const badge = getCategoryBadgeColor(props.product.category)
  return {
    backgroundColor: badge.bg,
    color: badge.color
  }
})

const isNotAvailable = computed(() => {
  return !!props.product.status && props.product.status !== 'AVAILABLE'
})

const truncatedDescription = computed(() => {
  if (!props.product.description) return ''
  return props.product.description.length > 80
    ? props.product.description.substring(0, 80) + '...'
    : props.product.description
})
</script>

<style scoped>
.product-card {
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
  background: var(--bg-surface);
  border: 1px solid var(--border-glass);
  border-radius: var(--radius-md);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.product-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.08);
  border-color: rgba(99, 102, 241, 0.2);
}

.image-wrapper {
  position: relative;
  width: 100%;
  height: 190px;
  overflow: hidden;
}

.product-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s ease;
}

.product-card:hover .product-image {
  transform: scale(1.06);
}

.category-tag {
  position: absolute;
  top: 0.75rem;
  left: 0.75rem;
  font-size: 0.75rem;
  font-weight: 700;
  padding: 0.25rem 0.65rem;
  border-radius: var(--radius-sm);
  backdrop-filter: blur(8px);
}

.product-body {
  padding: 1.25rem;
  display: flex;
  flex-direction: column;
  flex: 1;
}

.product-title {
  font-size: 1.1rem;
  font-weight: 700;
  color: var(--text-main);
  margin-bottom: 0.5rem;
}

.product-description {
  font-size: 0.85rem;
  color: var(--text-muted);
  margin-bottom: 1rem;
  flex: 1;
}

.product-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.25rem;
  padding-top: 0.75rem;
  border-top: 1px solid rgba(255, 255, 255, 0.05);
}

.capacity-info {
  font-size: 0.8rem;
  color: var(--text-muted);
}

.price-tag {
  font-size: 1.25rem;
  font-weight: 800;
  color: var(--accent-primary);
}

.price-tag small {
  font-size: 0.75rem;
  font-weight: 400;
  color: var(--text-muted);
}

.card-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0.6rem;
}
</style>
