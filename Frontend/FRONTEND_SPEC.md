# N2N Booking System — Frontend Specification

> **Purpose**: This file is a complete reference for generating new frontend modules.
> Give this file + your new module requirements to any AI, and it will produce pages
> that are **visually consistent** with the rest of the application.

---

## Table of Contents

1. [Tech Stack](#1-tech-stack)
2. [Project Structure](#2-project-structure)
3. [Design Tokens & CSS Variables](#3-design-tokens--css-variables)
4. [Theme System (ColorPick)](#4-theme-system-colorpick)
5. [Typography & Spacing](#5-typography--spacing)
6. [Component Catalog](#6-component-catalog)
7. [Layout System](#7-layout-system)
8. [Router Pattern](#8-router-pattern)
9. [API Integration Pattern](#9-api-integration-pattern)
10. [DTO / Interface Conventions](#10-dto--interface-conventions)
11. [Enum Definitions](#11-enum-definitions)
12. [Helper Utilities](#12-helper-utilities)
13. [New Module Checklist](#13-new-module-checklist)
14. [Complete Example: Adding a "Reviews" Module](#14-complete-example-adding-a-reviews-module)

---

## 1. Tech Stack

| Layer        | Technology                           |
| ------------ | ------------------------------------ |
| Framework    | **Vue 3** (Composition API, `<script setup lang="ts">`) |
| Language     | **TypeScript**                       |
| Router       | **vue-router 4** (createWebHistory)  |
| HTTP Client  | **Axios** (wrapped in `ApiHelper`)   |
| Build Tool   | **Vite**                             |
| Styling      | **Scoped CSS** + global `main.css`   |
| State        | **LocalStorage** (no Vuex/Pinia)     |

---

## 2. Project Structure

```
src/
├── assets/
│   └── main.css              # Global CSS variables & utility classes
├── components/                # Reusable UI components
│   ├── AdminSidebar.vue
│   ├── Footer.vue
│   ├── Modal.vue              # Generic modal (Teleport to body)
│   ├── Navbar.vue             # Top navigation bar
│   ├── ProductCard.vue        # Product listing card
│   ├── StatusBadge.vue        # Dynamic status/role badge
│   └── Toast.vue              # Toast notification (success/error/warning)
├── config.ts                  # API URL registry (AppConfig)
├── constants/
│   └── enums.ts               # BookingStatus, PaymentStatus, Role
├── dto/                       # TypeScript interfaces for API payloads
│   ├── auth.dto.ts
│   ├── admin.dto.ts
│   ├── booking.dto.ts
│   ├── cart.dto.ts
│   └── product.dto.ts
├── helpers/
│   ├── api_helper.ts          # Axios wrapper (GET/POST/PUT/DELETE)
│   ├── auth.helper.ts         # Token & user storage utilities
│   ├── color.helper.ts        # Status/category badge color mappings
│   └── theme.helper.ts        # ColorPick class (master theme colors)
├── layouts/
│   ├── UserLayout.vue         # Navbar + <router-view> + Footer
│   └── AdminLayout.vue        # AdminSidebar + <router-view>
├── router/
│   └── index.ts               # Route definitions & navigation guards
├── services/                  # API service classes (one per domain)
│   ├── index.ts               # Barrel export for all APIs + DTOs
│   ├── auth.api.ts
│   ├── admin.api.ts
│   ├── booking.api.ts
│   ├── cart.api.ts
│   └── product.api.ts
├── views/
│   ├── auth/                  # LoginView, SignupView
│   ├── user/                  # ProductListView, ProductDetailView, CartView, etc.
│   └── admin/                 # AdminDashboardView, AdminUserView, etc.
├── App.vue                    # Root (injects theme CSS vars + <router-view>)
└── main.ts                    # App bootstrap
```

---

## 3. Design Tokens & CSS Variables

All colors in the app are consumed via CSS custom properties defined in `:root`.
**NEVER hardcode colors.** Always use `var(--token-name)`.

The CSS variables are dynamically overridden at runtime in `App.vue` by reading
values from `theme.helper.ts` (ColorPick class).

### Variable Reference

```css
:root {
  /* ── Backgrounds ──────────────────────────────── */
  --bg-primary         /* Page body background          → ColorPick.backgroundColor (#ffffff) */
  --bg-surface         /* Card / panel backgrounds       → ColorPick.surfaceColor (#ffffff)   */
  --bg-surface-elevated /* Slightly darker surface       → ColorPick.backgroundColorLightGrey (#FCFDFF) */
  --bg-glass           /* Semi-transparent overlay       → ColorPick.surfaceColor + 'e6'       */
  --bg-glass-hover     /* Hover state overlay            */

  /* ── Borders ──────────────────────────────────── */
  --border-glass       /* Default light border           → ColorPick.backgroundColorGrey (#D9D9D9) */
  --border-glass-hover /* Hover state border             */

  /* ── Accent / Brand Colors ────────────────────── */
  --accent-primary       /* Primary brand color          → ColorPick.primaryColor (#A30000)     */
  --accent-primary-hover /* Darker on hover              → ColorPick.primaryDarkenColor (#472100) */
  --accent-secondary     /* Secondary color              → ColorPick.secondaryColor (#00563A)   */
  --accent-gradient      /* Primary gradient for buttons  → linear-gradient(primaryColor → thirdaryColor) */
  --accent-gradient-blue /* Alt gradient                  → linear-gradient(buttonBgColorBlue → secondaryColor) */

  /* ── Text ─────────────────────────────────────── */
  --text-main          /* Primary text                   → ColorPick.fontColorBlack (#000000)   */
  --text-muted         /* Secondary/muted text           → ColorPick.fontColorGrey (#757575)    */
  --text-subtle        /* Faint helper text              → ColorPick.fontColorGrey2 (#9A9A9A)   */
  --text-inverse       /* Text on colored backgrounds    → #ffffff                               */

  /* ── Status Colors ────────────────────────────── */
  --status-pending     /* Yellow-amber                   → ColorPick.ratingYellow               */
  --status-confirmed   /* Blue                           → ColorPick.buttonBgColorBlue          */
  --status-completed   /* Green                          → ColorPick.successColor               */
  --status-cancelled   /* Red                            → ColorPick.errorColor                 */

  /* ── Radius ───────────────────────────────────── */
  --radius-sm: 8px;
  --radius-md: 12px;
  --radius-lg: 18px;
  --radius-full: 9999px;

  /* ── Shadows ──────────────────────────────────── */
  --shadow-color: rgba(0, 0, 0, 0.4);
  --shadow-light: rgba(0, 0, 0, 0.05);
  --shadow-glow: 0 0 25px rgba(99, 102, 241, 0.25);

  /* ── Font ──────────────────────────────────────── */
  --font-family: 'Plus Jakarta Sans', -apple-system, BlinkMacSystemFont, sans-serif;
}
```

---

## 4. Theme System (ColorPick)

**File**: `src/helpers/theme.helper.ts`

```ts
class ColorPick {
  static backgroundColor: string = "#ffffff";
  static backgroundColorGrey: string = "#D9D9D9";
  static backgroundColorGrey2: string = "#e6e6e6";
  static backgroundColorLightGrey: string = "#FCFDFF";
  static backgroundColorLightGrey2: string = "#F5F5F5";
  static primaryColor: string = "#A30000";
  static primaryDarkenColor: string = "#472100";
  static secondaryColor: string = "#00563A";
  static secondaryDarkenColor: string = "#ffeec9";
  static thirdaryColor: string = "#EF1923";
  static surfaceColor: string = "#ffffff";
  static surfaceDarkColor: string = "#000000";
  static onBackgroundColor: string = "#0A0A0C";
  static onBackgroundDarkColor: string = "#f4f5fa";
  static backgroundColorYellow: string = "#FEA813";
  static onSurfaceColor: string = "#757474";
  static fontColor: string = "#000000";
  static fontColorWhite: string = "#ffffff";
  static fontColorYellow: string = '#FDF6D2';
  static fontColorGrey: string = '#757575';
  static fontColorGrey01: string = '#423939';
  static fontColorGrey2: string = '#9A9A9A';
  static fontColorGrey3: string = '#484848';
  static fontColorBlack: string = '#000000';
  static fontColorBrown: string = '#785657';
  static errorColor: string = "#FF0000";
  static successColor: string = '#52b963';
  static footerBgColor: string = '#A30000';
  static buttonBgColorBlue: string = '#184177';
  static navigatorDisabledColor: string = '#C5C5C5';
  static ratingYellow: string = '#F09B0A';
  static textfieldBg: string = '#EEF2F7';
}
export default ColorPick;
```

> **Rule**: If you need a new color, add it to `ColorPick` first, then map it to a
> CSS variable in `App.vue`'s `onMounted()`. Never use inline hex in templates.

---

## 5. Typography & Spacing

| Property           | Value                                |
| ------------------ | ------------------------------------ |
| Font Family        | `Plus Jakarta Sans`, system fallback |
| Page Title `<h1>`  | `font-size: 2rem; font-weight: 800`  |
| Section Title `<h2>` | `font-size: 1.25rem; font-weight: 700` |
| Body Text          | `font-size: 0.95rem; line-height: 1.6` |
| Muted / Helper     | `font-size: 0.85rem; color: var(--text-muted)` |
| Small Label        | `font-size: 0.75rem; font-weight: 700; text-transform: uppercase; letter-spacing: 0.05em` |
| Container max-width | `1280px` with `padding: 0 1.5rem`    |
| Section gap        | `2rem` – `2.5rem`                     |
| Card internal padding | `1.25rem` – `2.5rem`               |

---

## 6. Component Catalog

### 6.1 Panels & Cards

| Class           | Use For                                  | Key Styles                                              |
| --------------- | ---------------------------------------- | ------------------------------------------------------- |
| `.glass-panel`  | Sections, form containers, summary boxes | `bg: var(--bg-surface); border: 1px solid var(--border-glass); border-radius: var(--radius-lg); box-shadow: 0 4px 15px var(--shadow-light)` |
| `.glass-card`   | Smaller items (spec cards, payment cards) | `bg: var(--bg-surface); border: 1px solid var(--border-glass); border-radius: var(--radius-md)` |

**Hover effect on `.glass-card`**:
```css
.glass-card:hover {
  transform: translateY(-3px);
  border-color: var(--accent-primary);
  box-shadow: 0 8px 20px var(--shadow-light);
}
```

### 6.2 Buttons

| Class             | Background                        | Text Color            | Border                                  |
| ----------------- | --------------------------------- | --------------------- | --------------------------------------- |
| `.btn`            | –                                 | –                     | `border: none` (base reset)             |
| `.btn-primary`    | `var(--accent-gradient)`          | `var(--text-inverse)` | none                                    |
| `.btn-secondary`  | `var(--bg-surface)`               | `var(--text-main)`    | `1px solid var(--border-glass)`         |
| `.btn-danger`     | `var(--bg-surface-elevated)`      | `var(--status-cancelled)` | `1px solid var(--status-cancelled)` |
| `.btn-success`    | `var(--bg-surface-elevated)`      | `var(--status-completed)` | `1px solid var(--status-completed)` |

**Size modifiers**: `.btn-sm`, `.btn-lg`, `.btn-full` (width: 100%)

**Hover behaviors**:
- `.btn-primary:hover` → `opacity: 0.92; translateY(-1px)`
- `.btn-secondary:hover` → `bg: var(--bg-surface-elevated); border: 1px solid var(--border-glass-hover)`
- `.btn-danger:hover` → `bg: var(--status-cancelled); color: var(--text-inverse)`
- `.btn-success:hover` → `bg: var(--status-completed); color: var(--text-inverse)`

### 6.3 Forms

```css
.form-group   → margin-bottom: 1.25rem
.form-label   → font-size: 0.875rem; font-weight: 600; color: var(--text-muted)
.form-input   → bg: var(--bg-surface-elevated); border: 1px solid var(--border-glass); border-radius: var(--radius-md)
.form-input:focus → border-color: var(--accent-primary); box-shadow: 0 0 0 3px var(--shadow-light)
```

Also: `.form-select`, `.form-textarea` share the same styles.

### 6.4 Tables (Admin)

```css
.table-container  → overflow-x: auto
.custom-table     → border-collapse: collapse; width: 100%
.custom-table th  → font-size: 0.8rem; text-transform: uppercase; color: var(--text-muted); border-bottom: 1px solid var(--border-glass)
.custom-table td  → font-size: 0.9rem; border-bottom: 1px solid var(--border-glass)
.custom-table tr:hover td → background: var(--bg-surface-elevated)
```

### 6.5 Badges

```css
.badge → padding: 0.25rem 0.75rem; border-radius: var(--radius-full); font-size: 0.75rem; font-weight: 700; text-transform: uppercase
```

For status badges, use the `<StatusBadge>` component:
```vue
<StatusBadge type="booking" :value="booking.status" />
<StatusBadge type="payment" :value="booking.paymentStatus" />
<StatusBadge type="role" :value="user.role" />
```

### 6.6 Reusable Components

| Component           | Import Path                              | Props                                     | Usage                                      |
| ------------------- | ---------------------------------------- | ----------------------------------------- | ------------------------------------------ |
| `<Toast>`           | `@/components/Toast.vue`                 | –                                         | `ref="toastRef"` → `toastRef.value?.show(msg, type)` |
| `<DialogPlus>`      | `@/components/DialogPlus.vue`            | `:isOpen`, `:title`, `:cancelText`, `:cancelAction`, `:continueText`, `:continueAction`, `:submitting`, `:continueDisabled`, `:continueBtnClass`, `@close` | Standardized modal dialog wrapper with side-by-side action buttons |
| `<StatusBadge>`     | `@/components/StatusBadge.vue`           | `:type`, `:value`                         | Dynamic color badge based on status/role   |
| `<ProductCard>`     | `@/components/ProductCard.vue`           | `:product`, `@add-to-cart`                | Standard card for product listings         |

> **CRITICAL RULE FOR DIALOGS / MODALS**:
> **ALWAYS use `<DialogPlus>` for all modals, popups, and dialogs across the application.**
> Do **NOT** use basic `<Modal>` or custom modal markup.
>
> **`<DialogPlus>` Prop Rules**:
> - Pass `:isOpen="isModalOpen"` and `:title="Dialog Title"`.
> - Pass `:cancelText="Cancel"` and `:cancelAction="handleCancel"`. **Note**: If `:cancelAction` is omitted (e.g. for view-only/inspect popups), the Cancel button will **NOT** be rendered!
> - Pass `:continueText="Save / Confirm"` and `:continueAction="handleSubmit"`.
> - Pass `:submitting="submitting"` to show loading state on the primary button.
>
> **Example Usage**:
> ```vue
> <DialogPlus 
>   :isOpen="isModalOpen" 
>   title="Edit Product" 
>   cancelText="Cancel"
>   :cancelAction="() => isModalOpen = false"
>   continueText="Save Product"
>   :continueAction="submitProduct"
>   :submitting="submitting"
>   @close="isModalOpen = false"
> >
>   <!-- Form / Body Slot -->
>   <div class="form-group">
>     <label class="form-label">Product Name</label>
>     <input v-model="form.name" class="form-input" />
>   </div>
> </DialogPlus>
> ```

**Toast usage pattern**:
```vue
<script setup lang="ts">
import Toast from '@/components/Toast.vue'
const toastRef = ref<InstanceType<typeof Toast> | null>(null)

// In methods:
toastRef.value?.show('Success message!')              // green
toastRef.value?.show('Error message', 'error')        // red
toastRef.value?.show('Warning message', 'warning')    // amber
</script>

<template>
  <!-- at the bottom of template -->
  <Toast ref="toastRef" />
</template>
```

---

## 7. Layout System

### User-facing pages → `UserLayout`
- **File**: `src/layouts/UserLayout.vue`
- **Structure**: `<Navbar>` + `<router-view>` + `<Footer>`
- All routes under `path: '/'` use this layout

### Admin pages → `AdminLayout`
- **File**: `src/layouts/AdminLayout.vue`
- **Structure**: `<AdminSidebar>` + `<router-view>` (horizontal flex, gap: 2rem)
- All routes under `path: '/admin'` use this layout
- **UI Permissions & Layout Contract**: The visibility of tabs within the `<AdminSidebar>`, as well as the Create/Edit/Delete action buttons inside the `<router-view>`, are strictly controlled by the backend RBAC permissions (`canRead`, `canCreate`, `canUpdate`, `canDelete`). For the complete logic and Frontend-Backend Contract mapping, please refer to section **9. RBAC & Security Architecture > Admin Portal Layout & UI Permissions (Frontend-Backend Contract)** in [BACKEND_SPEC.md](../Backend/BACKEND_SPEC.md).

### View template pattern
Every page view follows this structure:
```vue
<template>
  <div class="container page-container">
    <h1 class="page-title">Page Title</h1>
    <p class="page-subtitle">Description text</p>

    <div v-if="loading" class="loading-spinner">Loading...</div>

    <div v-else>
      <!-- Main content -->
    </div>

    <Toast ref="toastRef" />
  </div>
</template>
```

**Page container padding**: `padding-top: 1.5rem` – `2rem`

---

## 8. Router Pattern

**File**: `src/router/index.ts`

### Adding a new user route:
```ts
// 1. Import the view at the top
import NewModuleView from '../views/user/NewModuleView.vue'

// 2. Add to the UserLayout children array
{
  path: '/',
  component: UserLayout,
  children: [
    // ... existing routes
    { path: 'new-module', name: 'NewModule', component: NewModuleView, meta: { requiresAuth: true } },
  ]
}
```

### Adding a new admin route:
```ts
// Add to the AdminLayout children array
{
  path: '/admin',
  component: AdminLayout,
  meta: { requiresAdmin: true },
  children: [
    // ... existing routes
    { path: 'new-module', name: 'AdminNewModule', component: AdminNewModuleView },
  ]
}
```

### Route meta:
- `{ requiresAuth: true }` → Redirects to Login if not authenticated
- `{ requiresAdmin: true }` → Redirects to Home if not admin

---

## 9. API Integration Pattern

### Step 1: Register URL in `config.ts`

```ts
// In src/config.ts, add:
static apiNewModuleUrl: string = `${this.backendUrl}/new-module`;
```

### Step 2: Create DTO interfaces in `src/dto/newmodule.dto.ts`

```ts
export interface NewModuleRequest {
  name: string;
  description?: string;
}

export interface NewModuleResponse {
  id: number;
  name: string;
  description?: string;
  createdAt: string;
}
```

### Step 3: Create API service in `src/services/newmodule.api.ts`

```ts
import AppConfig from '@/config';
import type { NewModuleRequest, NewModuleResponse } from '@/dto/newmodule.dto';
import ApiHelper from '@/helpers/api_helper';

export default class NewModuleApi {
  public static async getAll(): Promise<NewModuleResponse[]> {
    const response = await ApiHelper.get(AppConfig.apiNewModuleUrl);
    return response.data as NewModuleResponse[];
  }

  public static async getById(id: number | string): Promise<NewModuleResponse> {
    const response = await ApiHelper.get(`${AppConfig.apiNewModuleUrl}/${id}`);
    return response.data as NewModuleResponse;
  }

  public static async create(data: NewModuleRequest): Promise<NewModuleResponse> {
    const response = await ApiHelper.post(AppConfig.apiNewModuleUrl, undefined, data);
    return response.data as NewModuleResponse;
  }

  public static async update(id: number | string, data: NewModuleRequest): Promise<NewModuleResponse> {
    const response = await ApiHelper.put(`${AppConfig.apiNewModuleUrl}/${id}`, undefined, data);
    return response.data as NewModuleResponse;
  }

  public static async delete(id: number | string): Promise<void> {
    await ApiHelper.delete(`${AppConfig.apiNewModuleUrl}/${id}`);
  }
}
```

### Step 4: Register in barrel export `src/services/index.ts`

```ts
import NewModuleApi from './newmodule.api'

export { /* existing */, NewModuleApi }
export * from '@/dto/newmodule.dto'
```

### ApiHelper Method Signatures (Quick Reference)

```ts
ApiHelper.get(url)                          // GET request
ApiHelper.post(url, headers?, body?)        // POST with JSON body
ApiHelper.put(url, headers?, body?)         // PUT with JSON body
ApiHelper.delete(url)                       // DELETE request
```

All methods automatically:
- Attach JWT from localStorage (`Authorization: Bearer <token>`)
- Redirect to `/login` on 401/403
- Throw formatted error messages

---

## 10. DTO / Interface Conventions

| Rule                          | Example                                           |
| ----------------------------- | ------------------------------------------------- |
| File naming                   | `modulename.dto.ts`                               |
| Request interfaces            | `CreateXxxRequest`, `UpdateXxxRequest`             |
| Response interfaces           | `XxxResponse` or `XxxDTO`                         |
| Dates                         | `string` (ISO format `YYYY-MM-DD` or ISO datetime)|
| Enums                         | Import from `@/constants/enums`                    |
| Optional fields               | `field?: type`                                     |
| IDs                           | `number`                                           |

### Existing DTOs

**Auth**: `LoginRequest`, `SignupRequest`, `JwtResponse`, `UserResponse`
**Product**: `ProductDTO`, `ProductAvailabilityResponse`, `DayScheduleDTO`, `ProductMonthlyScheduleResponse`
**Cart**: `AddToCartRequest`, `UpdateCartRequest`, `CartItemResponse`
**Booking**: `CheckoutRequest`, `PayBookingRequest`, `UpdateStatusRequest`, `BookingItemResponse`, `BookingResponse`
**Admin**: `AdminDashboardDTO`

---

## 11. Enum Definitions

**File**: `src/constants/enums.ts`

```ts
export enum BookingStatus {
  PENDING = 'PENDING',
  CONFIRMED = 'CONFIRMED',
  CANCELLED = 'CANCELLED',
  COMPLETED = 'COMPLETED'
}

export enum PaymentStatus {
  UNPAID = 'UNPAID',
  PAID = 'PAID',
  REFUNDED = 'REFUNDED',
  FAILED = 'FAILED'
}

export enum Role {
  USER = 'ROLE_USER',
  ADMIN = 'ROLE_ADMIN'
}
```

When adding a new enum, add it to this file and use it in DTOs:
```ts
import { NewStatus } from '@/constants/enums'
```

---

## 12. Helper Utilities

### `auth.helper.ts` — Authentication State

```ts
setStoredAuth(token, user)    // Save token + user to localStorage
getStoredToken()              // Returns JWT string | null
getStoredUser()               // Returns parsed user object | null
removeStoredAuth()            // Clear auth from localStorage
isAuthenticated()             // Boolean check
isAdmin()                     // Boolean check for ROLE_ADMIN
```

### `color.helper.ts` — Dynamic Badge Colors

```ts
getBookingStatusColor(status)   // Returns { bg, color, border, label }
getPaymentStatusColor(status)   // Returns { bg, color, border, label }
getRoleBadgeColor(role)         // Returns { bg, color, border, label }
getCategoryBadgeColor(category) // Returns { bg, color }
```

Use with inline `:style` or pass to `<StatusBadge>`.

### `api_helper.ts` — HTTP Client Wrapper

Wraps Axios with auto auth headers, error handling, and 401 redirect.
See [Section 9](#9-api-integration-pattern) for usage.

---

## 13. New Module Checklist

When adding a brand new module (e.g., "Reviews", "Promotions", "Notifications"):

```
□ 1. DTO         → Create src/dto/{module}.dto.ts with Request/Response interfaces
□ 2. Enum        → If new statuses needed, add to src/constants/enums.ts
□ 3. Config      → Add API URL(s) to src/config.ts
□ 4. API Service  → Create src/services/{module}.api.ts (extends ApiHelper)
□ 5. Barrel       → Register API + DTO exports in src/services/index.ts
□ 6. Views        → Create view components in src/views/user/ or src/views/admin/
□ 7. Router       → Add route(s) to src/router/index.ts (under correct layout)
□ 8. Components   → If module needs reusable cards/items, add to src/components/
□ 9. Color Helper → If module has statuses, add color mapping to color.helper.ts
□ 10. Navbar      → If module needs nav link, update src/components/Navbar.vue
```

---

## 14. Complete Example: Adding a "Reviews" Module

### Prompt to give to AI:

> "I need to add a Reviews module to my N2N Booking System frontend.
>
> **Backend API endpoints**:
> - `GET /api/reviews` — List all reviews for current user
> - `GET /api/reviews/{id}` — Get single review
> - `POST /api/reviews` — Create review (body: { bookingId, rating, comment })
> - `PUT /api/reviews/{id}` — Update review
> - `DELETE /api/reviews/{id}` — Delete review
> - `GET /api/admin/reviews` — Admin: list all reviews
>
> **User views needed**:
> - Review list page at `/reviews`
> - Review form (modal) accessible from booking detail
>
> **Admin views needed**:
> - Admin review management at `/admin/reviews`
>
> Please follow the spec in `FRONTEND_SPEC.md` for all styling, structure, and patterns."

### What the AI should generate:

```
src/dto/review.dto.ts            ← CreateReviewRequest, UpdateReviewRequest, ReviewResponse
src/constants/enums.ts           ← (add ReviewStatus if needed)
src/config.ts                    ← (add apiReviewUrl, apiAdminReviewUrl)
src/services/review.api.ts       ← ReviewApi class
src/services/index.ts            ← (add ReviewApi export + re-export DTOs)
src/views/user/ReviewListView.vue ← User review list
src/views/admin/AdminReviewView.vue ← Admin review management
src/router/index.ts              ← (add routes under correct layouts)
src/components/Navbar.vue        ← (add "My Reviews" nav link for authenticated users)
```

All generated pages must:
- Use `class="container page-container"` as root wrapper
- Use `glass-panel` for section containers
- Use `glass-card` for item cards
- Use `btn btn-primary` / `btn-secondary` / `btn-danger` for buttons
- Use `form-group` + `form-label` + `form-input` for forms
- Use `custom-table` for admin data tables
- Use `<Toast>` for success/error feedback
- Use `<StatusBadge>` for any status display
- Use `<Modal>` for dialogs
- Never hardcode any colors — only `var(--xxx)` CSS variables
- Follow the `<script setup lang="ts">` pattern with TypeScript

---

## Quick Visual Rules Summary

| Element                  | Border Radius       | Border                              | Background            | Shadow                                |
| ------------------------ | ------------------- | ----------------------------------- | --------------------- | ------------------------------------- |
| Page container           | –                   | –                                   | `var(--bg-primary)`   | –                                     |
| Panel / Section          | `var(--radius-lg)`  | `1px solid var(--border-glass)`     | `var(--bg-surface)`   | `0 4px 15px var(--shadow-light)`      |
| Card / Item              | `var(--radius-md)`  | `1px solid var(--border-glass)`     | `var(--bg-surface)`   | –                                     |
| Card hover               | –                   | `1px solid var(--accent-primary)`   | –                     | `0 8px 20px var(--shadow-light)`      |
| Primary button           | `var(--radius-md)`  | none                                | `var(--accent-gradient)` | `0 4px 15px var(--shadow-light)`   |
| Secondary button         | `var(--radius-md)`  | `1px solid var(--border-glass)`     | `var(--bg-surface)`   | –                                     |
| Form input               | `var(--radius-md)`  | `1px solid var(--border-glass)`     | `var(--bg-surface-elevated)` | focus: `0 0 0 3px var(--shadow-light)` |
| Badge                    | `var(--radius-full)` | dynamic per status                 | dynamic per status    | –                                     |
| Modal                    | `var(--radius-lg)`  | `1px solid var(--border-glass)`     | `var(--bg-surface-elevated)` | `0 20px 50px rgba(0,0,0,0.6)`  |
| Toast                    | `var(--radius-md)`  | `1px solid` (color varies)          | success/error colors  | `0 10px 30px rgba(0,0,0,0.4)`        |
