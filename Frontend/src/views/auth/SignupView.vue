<template>
  <div class="auth-page container">
    <div class="glass-panel auth-card">
      <div class="auth-header">
        <h2 class="auth-title">Create Account</h2>
        <p class="auth-subtitle">Join N2N to start booking spaces & gear</p>
      </div>

      <form @submit.prevent="handleSignup" class="auth-form">
        <div v-if="error" class="error-alert">{{ error }}</div>

        <div class="form-group">
          <label class="form-label">Full Name</label>
          <input v-model="form.fullName" type="text" class="form-input" placeholder="e.g. Alice Smith" required />
        </div>

        <div class="form-group">
          <label class="form-label">Username</label>
          <input v-model="form.username" type="text" class="form-input" placeholder="Choose a username" required />
        </div>

        <div class="form-group">
          <label class="form-label">Email Address</label>
          <input v-model="form.email" type="email" class="form-input" placeholder="name@example.com" required />
        </div>

        <div class="form-group">
          <label class="form-label">Phone Number</label>
          <input v-model="form.phone" type="tel" class="form-input" placeholder="+60123456789" />
        </div>

        <div class="form-group">
          <label class="form-label">Password</label>
          <input v-model="form.password" type="password" class="form-input" placeholder="••••••••" required />
        </div>

        <button type="submit" class="btn btn-primary btn-full btn-lg" :disabled="loading">
          {{ loading ? 'Registering...' : 'Sign Up' }}
        </button>

        <p class="auth-footer">
          Already have an account?
          <router-link to="/login" class="auth-link">Log In</router-link>
        </p>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { AuthApi } from '../../services'

const router = useRouter()

const form = ref({
  fullName: '',
  username: '',
  email: '',
  phone: '',
  password: ''
})

const loading = ref(false)
const error = ref('')

const handleSignup = async () => {
  loading.value = true
  error.value = ''
  try {
    await AuthApi.signup(form.value)
    router.push('/login')
  } catch (err: any) {
    error.value = err.response?.data?.message || 'Failed to register account'
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
  padding: 2rem 0;
}

.auth-card {
  width: 100%;
  max-width: 480px;
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
</style>
