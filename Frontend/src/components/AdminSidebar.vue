<template>
  <aside class="admin-sidebar glass-panel">
    <div class="sidebar-header">
      <div class="admin-tag">ADMIN PORTAL</div>
      <h2 class="sidebar-title">Dashboard</h2>
    </div>

    <nav class="sidebar-nav">
      <router-link to="/admin" class="nav-link" exact-active-class="active">
        📊 Performance Overview
      </router-link>
      <router-link v-if="hasPermission('PRODUCTS', 'READ')" to="/admin/products" class="nav-link" active-class="active">
        📦 Products & Services
      </router-link>
      <router-link v-if="hasPermission('PRODUCTS', 'READ')" to="/admin/schedule" class="nav-link" active-class="active">
        📅 Availability & Schedule
      </router-link>
      <router-link v-if="hasPermission('BOOKINGS', 'READ')" to="/admin/bookings" class="nav-link" active-class="active">
        📑 Booking Orders
      </router-link>
      <router-link v-if="hasPermission('PAYMENTS', 'READ')" to="/admin/payments" class="nav-link" active-class="active">
        💳 Payments & Transactions
      </router-link>
      <router-link v-if="hasPermission('PROMOCODES', 'READ')" to="/admin/promocodes" class="nav-link" active-class="active">
        🏷️ Promo Codes
      </router-link>
      <router-link v-if="hasPermission('USERS', 'READ')" to="/admin/users" class="nav-link" active-class="active">
        👥 User Accounts
      </router-link>
      <router-link v-if="hasPermission('ROLES', 'READ')" to="/admin/roles" class="nav-link" active-class="active">
        🛡️ Roles & Permissions
      </router-link>
      <router-link v-if="isSuperAdmin()" to="/admin/settings" class="nav-link" active-class="active">
        ⚙️ Settings
      </router-link>
    </nav>

    <div class="sidebar-footer">
      <router-link to="/" class="btn btn-secondary btn-sm btn-full">
        &larr; Exit Admin Portal
      </router-link>
    </div>
  </aside>
</template>

<script setup lang="ts">
import { hasPermission, isSuperAdmin } from '@/helpers/auth.helper'
</script>

<style scoped>
.admin-sidebar {
  width: 260px;
  flex-shrink: 0;
  min-height: calc(100vh - 4rem);
  padding: 1.5rem;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  margin-bottom: 2rem;
}

.admin-tag {
  font-size: 0.65rem;
  font-weight: 800;
  letter-spacing: 0.1em;
  color: #ec4899;
  margin-bottom: 0.25rem;
}

.sidebar-title {
  font-size: 1.35rem;
  font-weight: 800;
  color: var(--text-main);
}

.sidebar-nav {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  flex: 1;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem 1rem;
  border-radius: var(--radius-md);
  color: var(--text-muted);
  text-decoration: none;
  font-size: 0.9rem;
  font-weight: 600;
  transition: all 0.2s ease;
}

.nav-link:hover {
  background: rgba(255, 255, 255, 0.05);
  color: var(--text-main);
}

.nav-link.active {
  background: var(--accent-gradient);
  color: #fff;
  box-shadow: 0 4px 15px rgba(99, 102, 241, 0.3);
}

.sidebar-footer {
  margin-top: 2rem;
  padding-top: 1rem;
  border-top: 1px solid var(--border-glass);
}
</style>
