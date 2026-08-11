<template>
  <div class="admin-layout container">
    <AdminSidebar />
    <main class="admin-main">
      <router-view />
    </main>
  </div>
</template>

<script setup lang="ts">
import AdminSidebar from '../../components/AdminSidebar.vue'
</script>

<style scoped>
.admin-layout {
  display: flex;
  gap: 2rem;
  padding-top: 1.5rem;
  align-items: flex-start;
}

.admin-main {
  flex: 1;
}
</style>
