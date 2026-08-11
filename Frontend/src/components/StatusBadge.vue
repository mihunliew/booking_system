<template>
  <span class="badge" :style="{ backgroundColor: config.bg, color: config.color, border: config.border }">
    {{ config.label }}
  </span>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { getBookingStatusColor, getPaymentStatusColor, getRoleBadgeColor } from '../helpers/color.helper'

const props = withDefaults(defineProps<{
  type?: 'booking' | 'payment' | 'role';
  value: string;
}>(), {
  type: 'booking'
})

const config = computed(() => {
  if (props.type === 'payment') {
    return getPaymentStatusColor(props.value)
  }
  if (props.type === 'role') {
    return getRoleBadgeColor(props.value)
  }
  return getBookingStatusColor(props.value)
})
</script>
