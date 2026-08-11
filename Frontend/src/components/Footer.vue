<template>
  <footer class="footer-container">
    <div class="container footer-content">
      <div class="footer-brand">
        <h3>N2N Booking Platform</h3>
        <p>Enterprise grade booking management system powered by Vue 3 & Spring Boot.</p>
      </div>
      <div class="footer-copy">
        &copy; 2026 N2N Platform. All rights reserved.
      </div>
    </div>
  </footer>
</template>

<style scoped>
.footer-container {
  margin-top: 5rem;
  border-top: 1px solid var(--border-glass);
  padding: 3rem 0;
  color: var(--text-muted);
  font-size: 0.875rem;
}

.footer-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 1.5rem;
}

.footer-brand h3 {
  color: var(--text-main);
  margin-bottom: 0.25rem;
}
</style>
