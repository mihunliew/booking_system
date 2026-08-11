<template>
  <div class="container page-container">
    <div class="cart-header">
      <h1 class="page-title">Shopping & Booking Cart</h1>
      <p class="page-subtitle">Review selected services before proceeding to checkout</p>
    </div>

    <div v-if="loading" class="loading">Loading your cart...</div>

    <div v-else-if="cartItems.length === 0" class="glass-panel empty-cart">
      <div class="empty-icon">🛒</div>
      <h2>Your Cart is Empty</h2>
      <p>Explore our catalog to add products and services to your booking cart.</p>
      <router-link to="/" class="btn btn-primary btn-lg" style="margin-top: 1.5rem;">Browse Products</router-link>
    </div>

    <div v-else class="cart-layout">
      <div class="cart-items-list">
        <div v-for="item in cartItems" :key="item.id" class="glass-card cart-item-card">
          <img :src="item.imageUrl || defaultImage" :alt="item.productName" class="item-thumb" />

          <div class="item-details">
            <h3 class="item-title">{{ item.productName }}</h3>
            <span class="item-category">{{ item.productCategory }}</span>

            <div class="item-inputs">
              <div class="input-group">
                <label>Booking Date:</label>
                <input
                  type="date"
                  v-model="item.bookingDate"
                  @change="updateItem(item)"
                  class="form-input input-sm"
                />
              </div>
              <div class="input-group">
                <label>Qty:</label>
                <input
                  type="number"
                  min="1"
                  v-model.number="item.quantity"
                  @change="updateItem(item)"
                  class="form-input input-sm qty-input"
                />
              </div>
            </div>
          </div>

          <div class="item-pricing">
            <span class="unit-price">${{ item.unitPrice }} / unit</span>
            <span class="subtotal-price">${{ item.subtotal.toFixed(2) }}</span>
            <button @click="removeItem(item.id)" class="btn btn-danger btn-sm">Remove</button>
          </div>
        </div>

        <div class="cart-actions">
          <button @click="clearCart" class="btn btn-secondary btn-sm">Clear Cart</button>
        </div>
      </div>

      <div class="order-summary glass-panel">
        <h2>Order Summary</h2>
        <div class="summary-row">
          <span>Items Total</span>
          <span>${{ totalSubtotal.toFixed(2) }}</span>
        </div>
        <div class="summary-row">
          <span>Service Fee & Tax</span>
          <span>$0.00</span>
        </div>
        <div class="summary-row total-row">
          <span>Grand Total</span>
          <span class="total-amount">${{ totalSubtotal.toFixed(2) }}</span>
        </div>

        <router-link to="/checkout" class="btn btn-primary btn-full btn-lg" style="margin-top: 1.5rem;">
          Proceed to Checkout &rarr;
        </router-link>
      </div>
    </div>

    <Toast ref="toastRef" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { CartApi } from '../../services'
import type { CartItemResponse } from '../../services'
import Toast from '../../components/Toast.vue'

const cartItems = ref<CartItemResponse[]>([])
const loading = ref(true)
const toastRef = ref<InstanceType<typeof Toast> | null>(null)

const defaultImage = 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?auto=format&fit=crop&w=800&q=80'

const totalSubtotal = computed(() => {
  return cartItems.value.reduce((acc, item) => acc + (item.subtotal || 0), 0)
})

const fetchCart = async () => {
  loading.value = true
  try {
    cartItems.value = await CartApi.getCart()
  } catch (err) {
    console.error('Failed to load cart:', err)
  } finally {
    loading.value = false
  }
}

const updateItem = async (item: CartItemResponse) => {
  try {
    const updated = await CartApi.updateCartItem(item.id, {
      quantity: item.quantity,
      bookingDate: item.bookingDate
    })
    item.subtotal = updated.subtotal
    toastRef.value?.show('Cart item updated')
  } catch (err) {
    toastRef.value?.show('Failed to update cart item', 'error')
  }
}

const removeItem = async (itemId: number) => {
  try {
    await CartApi.removeCartItem(itemId)
    cartItems.value = cartItems.value.filter(i => i.id !== itemId)
    toastRef.value?.show('Item removed')
  } catch (err) {
    toastRef.value?.show('Failed to remove item', 'error')
  }
}

const clearCart = async () => {
  try {
    await CartApi.clearCart()
    cartItems.value = []
    toastRef.value?.show('Cart cleared')
  } catch (err) {
    toastRef.value?.show('Failed to clear cart', 'error')
  }
}

onMounted(() => {
  fetchCart()
})
</script>

<style scoped>
.page-container {
  padding-top: 2rem;
}

.cart-header {
  margin-bottom: 2rem;
}

.page-title {
  font-size: 2rem;
  font-weight: 800;
}

.page-subtitle {
  color: var(--text-muted);
}

.empty-cart {
  padding: 4rem;
  text-align: center;
}

.empty-icon {
  font-size: 3.5rem;
  margin-bottom: 1rem;
}

.cart-layout {
  display: grid;
  grid-template-columns: 1fr 340px;
  gap: 2rem;
}

.cart-items-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.cart-item-card {
  display: flex;
  align-items: center;
  gap: 1.25rem;
  padding: 1.25rem;
}

.item-thumb {
  width: 90px;
  height: 90px;
  object-fit: cover;
  border-radius: var(--radius-md);
}

.item-details {
  flex: 1;
}

.item-title {
  font-size: 1.05rem;
  font-weight: 700;
  margin-bottom: 0.2rem;
}

.item-category {
  font-size: 0.75rem;
  color: var(--accent-secondary);
  font-weight: 700;
  display: block;
  margin-bottom: 0.75rem;
}

.item-inputs {
  display: flex;
  gap: 1rem;
}

.input-group {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  font-size: 0.8rem;
  color: var(--text-muted);
}

.input-sm {
  padding: 0.35rem 0.6rem;
  font-size: 0.8rem;
}

.qty-input {
  width: 70px;
}

.item-pricing {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 0.4rem;
}

.unit-price {
  font-size: 0.75rem;
  color: var(--text-muted);
}

.subtotal-price {
  font-size: 1.2rem;
  font-weight: 800;
  color: #38bdf8;
}

.cart-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 0.5rem;
}

.order-summary {
  padding: 1.75rem;
  height: fit-content;
}

.order-summary h2 {
  font-size: 1.25rem;
  margin-bottom: 1.25rem;
  border-bottom: 1px solid var(--border-glass);
  padding-bottom: 0.75rem;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 0.85rem;
  color: var(--text-muted);
  font-size: 0.9rem;
}

.total-row {
  border-top: 1px solid var(--border-glass);
  padding-top: 1rem;
  margin-top: 1rem;
  font-weight: 800;
  color: var(--text-main);
  font-size: 1.1rem;
}

.total-amount {
  font-size: 1.4rem;
  color: #38bdf8;
}
</style>
