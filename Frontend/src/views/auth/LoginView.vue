<template>
  <div class="auth-page container">
    <div class="glass-panel auth-card">
      <div class="auth-header">
        <h2 class="auth-title">Welcome Back</h2>
        <p class="auth-subtitle">Sign in to manage your bookings</p>
      </div>

      <form @submit.prevent="handleLogin" class="auth-form">
        <div v-if="error" class="error-alert">{{ error }}</div>

        <div class="form-group">
          <label class="form-label">Username</label>
          <input v-model="form.username" type="text" class="form-input" placeholder="Enter your username" required />
        </div>

        <div class="form-group">
          <label class="form-label">Password</label>
          <input v-model="form.password" type="password" class="form-input" placeholder="••••••••" required />
        </div>

        <button type="submit" class="btn btn-primary btn-full btn-lg" :disabled="loading">
          {{ loading ? 'Signing In...' : 'Sign In' }}
        </button>

        <p class="auth-footer">
          Don't have an account?
          <router-link to="/signup" class="auth-link">Create Account</router-link>
        </p>

        <div class="demo-creds">
          <p><strong>Demo Admin:</strong> admin / admin123</p>
          <p><strong>Demo User:</strong> user / user123</p>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { AuthApi } from '../../services'
import { setStoredAuth } from '../../helpers/auth.helper'

const router = useRouter()

const form = ref({
  username: '',
  password: ''
})

const loading = ref(false)
const error = ref('')

const handleLogin = async () => {
  loading.value = true
  error.value = ''
  try {
    const res = await AuthApi.login(form.value)
    setStoredAuth(res.token, res)
    
    // Redirect based on role
    if (res.role === 'ROLE_ADMIN' as unknown || res.role === 'ADMIN' as unknown) {
      router.push('/admin')
    } else {
      router.push('/')
    }
  } catch (err: any) {
    error.value = err.response?.data?.message || 'Invalid username or password'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: calc(100vh - 140px);
}

.auth-card {
  width: 100%;
  max-width: 440px;
  padding: 2.5rem;
}

.auth-header {
  text-align: center;
  margin-bottom: 2rem;
}

.auth-title {
  font-size: 1.75rem;
  font-weight: 800;
  color: var(--text-main);
}

.auth-subtitle {
  font-size: 0.9rem;
  color: var(--text-muted);

}

.error-alert {
  background: rgba(239, 68, 68, 0.2);
  border: 1px solid rgba(239, 68, 68, 0.4);
  color: #fca5a5;
  padding: 0.75rem;
  border-radius: var(--radius-md);
  font-size: 0.85rem;
  margin-bottom: 1.25rem;
  text-align: center;
}

.auth-footer {
  margin-top: 1.5rem;
  font-size: 0.9rem;
  text-align: center;
  color: var(--text-muted);
}

.auth-link {
  color: var(--accent-secondary);
  font-weight: 700;
  text-decoration: none;
}

.demo-creds {
  margin-top: 1.75rem;
  padding: 0.85rem;
  background: rgba(255, 255, 255, 0.04);
  border-radius: var(--radius-md);
  font-size: 0.8rem;
  color: var(--text-muted);
  text-align: center;
}
</style>
