<template>
  <div class="container navbar-wrapper">
    <header class="navbar-header glass-panel">
      <div class="navbar-container">
        <router-link to="/" class="brand-logo">
          <div class="logo-icon">N2N</div>
          <span class="logo-text">Booking<span class="highlight">Hub</span></span>
        </router-link>

        <nav class="nav-links">
          <router-link to="/" class="nav-item">Explore</router-link>
          <router-link v-if="authenticated" to="/cart" class="nav-item cart-link">
            Cart
            <span v-if="cartCount > 0" class="cart-badge">{{ cartCount }}</span>
          </router-link>
          <router-link v-if="authenticated" to="/bookings" class="nav-item">My Bookings</router-link>
          <router-link v-if="admin" to="/admin" class="nav-item admin-badge-link">
            Admin Portal
          </router-link>
        </nav>

        <div class="user-actions">
          <template v-if="authenticated">
            <div class="user-profile">
              <div class="avatar">{{ userInitial }}</div>
              <span class="username">{{ user?.fullName || user?.username }}</span>
            </div>
            <button @click="handleLogout" class="btn btn-secondary btn-sm">Logout</button>
          </template>
          <template v-else>
            <router-link to="/login" class="btn btn-secondary btn-sm">Login</router-link>
            <router-link to="/signup" class="btn btn-primary btn-sm">Sign Up</router-link>
          </template>
        </div>
      </div>
    </header>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { isAuthenticated, isAdmin, getStoredUser, removeStoredAuth } from '../helpers/auth.helper'
import { CartApi } from '../services'
import type { UserResponse, JwtResponse } from '../services'

const router = useRouter()
const route = useRoute()
const authenticated = ref(false)
const admin = ref(false)
const user = ref<UserResponse | JwtResponse | null>(null)
const cartCount = ref(0)

const userInitial = computed(() => {
  if (!user.value) return 'U'
  const name = user.value.fullName || user.value.username
  return name ? name.charAt(0).toUpperCase() : 'U'
})

const fetchCartCount = async () => {
  if (!authenticated.value) return
  try {
    const items = await CartApi.getCart()
    cartCount.value = items.length
  } catch (err) {
    console.error('Failed to load cart count:', err)
  }
}

const handleLogout = () => {
  removeStoredAuth()
  authenticated.value = false
  admin.value = false
  user.value = null
  cartCount.value = 0
  router.push('/login')
}

const updateAuthState = () => {
  authenticated.value = isAuthenticated()
  admin.value = isAdmin()
  user.value = getStoredUser()
  if (authenticated.value) {
    fetchCartCount()
  }
}

watch(() => route.path, () => {
  updateAuthState()
})

onMounted(() => {
  updateAuthState()
})
</script>

<style scoped>
.navbar-wrapper {
  width: 100%;
}

.navbar-header {
  position: sticky;
  top: 1rem;
  z-index: 100;
  margin: 1rem 0;
  border-radius: var(--radius-lg);
  padding: 0.75rem 0;
}

.navbar-container {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 1.5rem;
}

.brand-logo {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  text-decoration: none;
  color: var(--text-main);
  font-weight: 800;
  font-size: 1.25rem;
}

.logo-icon {
  background: var(--accent-gradient);
  color: #fff;
  padding: 0.35rem 0.65rem;
  border-radius: var(--radius-sm);
  font-size: 0.9rem;
  letter-spacing: 0.05em;
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.4);
}

.highlight {
  color: var(--accent-secondary);
}

.nav-links {
  display: flex;
  align-items: center;
  gap: 1.75rem;
}

.nav-item {
  color: var(--text-muted);
  text-decoration: none;
  font-size: 0.925rem;
  font-weight: 600;
  transition: color 0.2s ease;
}

.nav-item:hover, .router-link-active {
  color: var(--text-main);
}

.cart-link {
  position: relative;
}

.cart-badge {
  position: absolute;
  top: -8px;
  right: -14px;
  background: #ec4899;
  color: #fff;
  font-size: 0.7rem;
  font-weight: 800;
  padding: 0.1rem 0.4rem;
  border-radius: var(--radius-full);
}

.admin-badge-link {
  background: rgba(168, 85, 247, 0.2);
  color: #c084fc;
  padding: 0.25rem 0.75rem;
  border-radius: var(--radius-full);
  border: 1px solid rgba(168, 85, 247, 0.4);
}

.user-actions {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--accent-gradient-blue);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  color: #fff;
  font-size: 0.85rem;
}

.username {
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--text-muted);
}
</style>
