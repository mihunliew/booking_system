<template>
  <Teleport to="body">
    <div v-if="isOpen" class="modal-backdrop" @click.self="handleBackdropClick">
      <div class="modal-content glass-panel" :style="{ maxWidth: maxWidth }">
        <div class="modal-header">
          <h3 class="modal-title">{{ title }}</h3>
          <button v-if="showCloseBtn && closeOnBackdrop" @click="close" class="close-btn" type="button">&times;</button>
        </div>

        <div class="modal-body">
          <slot></slot>
        </div>

        <div v-if="cancelAction || continueAction || $slots.footer" class="dialog-actions">
          <slot name="footer">
            <button 
              v-if="cancelAction" 
              type="button" 
              @click="cancelAction" 
              class="btn btn-secondary btn-full"
            >
              {{ cancelText }}
            </button>
            
            <button 
              v-if="continueAction" 
              type="button" 
              @click="continueAction" 
              class="btn btn-full" 
              :class="continueBtnClass || 'btn-primary'" 
              :disabled="continueDisabled || submitting"
            >
              {{ submitting ? 'Processing...' : continueText }}
            </button>
          </slot>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
const props = withDefaults(defineProps<{
  isOpen?: boolean;
  title?: string;
  maxWidth?: string;
  cancelText?: string;
  cancelAction?: (() => void) | Function;
  continueText?: string;
  continueAction?: (() => void) | Function;
  continueDisabled?: boolean;
  submitting?: boolean;
  continueBtnClass?: string;
  closeOnBackdrop?: boolean;
  showCloseBtn?: boolean;
}>(), {
  isOpen: false,
  title: '',
  maxWidth: '550px',
  cancelText: 'Cancel',
  continueText: 'Confirm',
  continueDisabled: false,
  submitting: false,
  continueBtnClass: 'btn-primary',
  closeOnBackdrop: true,
  showCloseBtn: true
})

const emit = defineEmits<{
  (e: 'close'): void
}>()

const close = () => {
  emit('close')
}

const handleBackdropClick = () => {
  if (props.closeOnBackdrop) {
    close()
  }
}
</script>

<style scoped>
.modal-backdrop {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.7);
  backdrop-filter: blur(8px);
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1.5rem;
}

.modal-content {
  width: 100%;
  background: var(--bg-surface-elevated, #1a1a2e);
  border: 1px solid var(--border-glass, rgba(255, 255, 255, 0.1));
  border-radius: var(--radius-lg, 16px);
  overflow: hidden;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.6);
  animation: modalFadeIn 0.25s ease-out;
  display: flex;
  flex-direction: column;
}

@keyframes modalFadeIn {
  from {
    opacity: 0;
    transform: scale(0.95) translateY(10px);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

.modal-header {
  padding: 1.25rem 1.5rem;
  border-bottom: 1px solid var(--border-glass, rgba(255, 255, 255, 0.1));
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-title {
  font-size: 1.15rem;
  font-weight: 700;
  color: var(--text-main, #ffffff);
  margin: 0;
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.5rem;
  color: var(--text-muted, #94a3b8);
  cursor: pointer;
  line-height: 1;
  transition: color 0.2s ease;
}

.close-btn:hover {
  color: var(--text-main, #ffffff);
}

.modal-body {
  padding: 1.5rem;
  max-height: calc(85vh - 130px);
  overflow-y: auto;
}

.dialog-actions {
  display: flex;
  gap: 1rem;
  padding: 1rem 1.5rem 1.5rem 1.5rem;
  border-top: 1px solid var(--border-glass, rgba(255, 255, 255, 0.05));
}

.dialog-actions .btn {
  flex: 1;
}

.btn-full {
  width: 100%;
}
</style>
