<template>
  <Teleport to="body">
    <Transition name="toast-slide">
      <div v-if="visible" class="toast-container" :class="type">
        <div class="toast-icon">{{ icon }}</div>
        <div class="toast-message">{{ message }}</div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

const visible = ref(false)
const message = ref('')
const type = ref<'success' | 'error' | 'warning'>('success')

const icon = computed(() => {
  if (type.value === 'error') return '❌'
  if (type.value === 'warning') return '⚠️'
  return '✅'
})

let timer: number | null = null

const show = (msg: string, toastType: 'success' | 'error' | 'warning' = 'success', duration = 3000) => {
  message.value = msg
  type.value = toastType
  visible.value = true
  if (timer) clearTimeout(timer)
  timer = window.setTimeout(() => {
    visible.value = false
  }, duration)
}

defineExpose({ show })
</script>

<style scoped>
.toast-container {
  position: fixed;
  bottom: 2rem;
  right: 2rem;
  z-index: 2000;
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.85rem 1.35rem;
  border-radius: var(--radius-md);
  font-size: 0.9rem;
  font-weight: 600;
  color: #fff;
  backdrop-filter: blur(12px);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.4);
}

.toast-container.success {
  background: rgba(16, 185, 129, 0.9);
  border: 1px solid rgba(16, 185, 129, 0.5);
}

.toast-container.error {
  background: rgba(239, 68, 68, 0.9);
  border: 1px solid rgba(239, 68, 68, 0.5);
}

.toast-slide-enter-active, .toast-slide-leave-active {
  transition: all 0.3s ease;
}

.toast-slide-enter-from, .toast-slide-leave-to {
  opacity: 0;
  transform: translateY(20px);
}
</style>
