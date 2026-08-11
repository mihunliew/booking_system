<template>
  <router-view />
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import ColorPick from './helpers/theme.helper'

onMounted(() => {
  const root = document.documentElement
  
  // Dynamic CSS variables binding from ColorPick
  root.style.setProperty('--bg-primary', ColorPick.backgroundColor) // Changed from light grey to main background
  root.style.setProperty('--bg-surface', ColorPick.surfaceColor)
  root.style.setProperty('--bg-surface-elevated', ColorPick.backgroundColorLightGrey)
  // glass effect needs opacity, we can simulate or just use solid for now, or add hex alpha
  root.style.setProperty('--bg-glass', ColorPick.surfaceColor + 'e6') // 90% opacity
  root.style.setProperty('--border-glass', ColorPick.backgroundColorGrey)
  
  root.style.setProperty('--accent-primary', ColorPick.primaryColor)
  root.style.setProperty('--accent-primary-hover', ColorPick.primaryDarkenColor)
  root.style.setProperty('--accent-secondary', ColorPick.secondaryColor)
  
  root.style.setProperty('--accent-gradient', `linear-gradient(135deg, ${ColorPick.primaryColor} 0%, ${ColorPick.thirdaryColor} 100%)`)
  root.style.setProperty('--accent-gradient-blue', `linear-gradient(135deg, ${ColorPick.buttonBgColorBlue} 0%, ${ColorPick.secondaryColor} 100%)`)
  
  root.style.setProperty('--text-main', ColorPick.fontColorBlack)
  root.style.setProperty('--text-muted', ColorPick.fontColorGrey)
  root.style.setProperty('--text-subtle', ColorPick.fontColorGrey2)

  root.style.setProperty('--status-pending', ColorPick.ratingYellow)
  root.style.setProperty('--status-confirmed', ColorPick.buttonBgColorBlue)
  root.style.setProperty('--status-completed', ColorPick.successColor)
  root.style.setProperty('--status-cancelled', ColorPick.errorColor)
})
</script>

<style>
/* Global styles can stay here if needed */
</style>
