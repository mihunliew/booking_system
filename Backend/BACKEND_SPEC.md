# N2N Booking System — Backend Architecture Spec

> **Purpose**: This document is the single source of truth for AI agents (or human developers) who need to add a new module to the backend. Follow every convention listed below to keep the codebase consistent.

---

## 1. Tech Stack & Dependencies

| Layer | Technology |
|---|---|
| Framework | Spring Boot 3.2.x (Java 21) |
| ORM | Spring Data JPA + Hibernate |
| Database | MySQL 8.x |
| Migrations | Flyway (Community Edition) |
| Auth | JWT (io.jsonwebtoken / jjwt) |
| Security | Spring Security 6 + Method-Level `@PreAuthorize` |
| Build | Maven (`pom.xml`) |
| Boilerplate | Lombok (`@Data`, `@Builder`, `@RequiredArgsConstructor`, etc.) |
| Validation | Jakarta Validation (`@NotBlank`, `@NotNull`, `@Email`, `@Min`, `@DecimalMin`) |

---

## 2. Package Structure

All source code lives under the base package: `com.n2n.booking`

```
com.n2n.booking/
├── BookingApplication.java          # Main entry point (@SpringBootApplication)
├── config/                          # Spring configs & filters
│   ├── CorsConfig.java              # CORS global config
│   ├── JwtAuthenticationFilter.java # OncePerRequestFilter — extracts JWT, sets SecurityContext
│   └── SecurityConfig.java          # SecurityFilterChain, PasswordEncoder, AuthManager
├── controller/                      # REST controllers (grouped by domain)
│   ├── AuthController.java          # /api/auth/**  (public)
│   ├── ProductController.java       # /api/products/** (public GET, auth for write)
│   ├── CartController.java          # /api/cart/** (authenticated users)
│   ├── BookingController.java       # /api/bookings/** (authenticated users)
│   ├── AdminDashboardController.java# /api/admin/dashboard (ADMIN+SUPERADMIN)
│   ├── AdminProductController.java  # /api/admin/products (permission-gated)
│   ├── AdminBookingController.java  # /api/admin/bookings (permission-gated)
│   ├── AdminUserController.java     # /api/admin/users (permission-gated)
│   └── AdminRoleController.java     # /api/admin/roles (SUPERADMIN only)
├── dto/                             # Data Transfer Objects (request/response)
├── entity/                          # JPA @Entity classes
├── enums/                           # Java enums
├── exception/                       # Custom exceptions & GlobalExceptionHandler
├── repository/                      # Spring Data JPA repositories
├── security/                        # JWT provider, UserPrincipal, UserDetailsService
└── service/                         # Business logic
```

---

## 3. Naming Conventions

| Item | Convention | Example |
|---|---|---|
| Entity | Singular noun, PascalCase | `Product`, `Booking`, `AdminRole` |
| Repository | `{Entity}Repository` | `ProductRepository` |
| Service | `{Entity}Service` or `{Domain}Service` | `ProductService`, `AdminService` |
| Controller (public) | `{Entity}Controller` | `ProductController` |
| Controller (admin) | `Admin{Entity}Controller` | `AdminProductController` |
| DTO (single) | `{Entity}DTO` | `ProductDTO` |
| DTO (grouped) | `{Entity}DTOs` (static inner classes) | `AuthDTOs.LoginRequest`, `BookingDTOs.BookingResponse` |
| Enum | PascalCase | `BookingStatus`, `PaymentStatus`, `Role` |
| DB Table | Plural snake_case | `products`, `admin_roles`, `booking_items` |
| DB Column | snake_case | `created_at`, `full_name`, `admin_role_id` |
| API Path (public) | `/api/{entity-plural}` | `/api/products`, `/api/bookings` |
| API Path (admin) | `/api/admin/{entity-plural}` | `/api/admin/products`, `/api/admin/roles` |

---

## 4. Entity Pattern

Every entity follows this exact template:

```java
package com.n2n.booking.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "table_name_plural")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EntityName {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- domain fields ---

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
```

### Key Rules
- PK is always `Long id` with `GenerationType.IDENTITY`.
- Use `@Column(nullable, unique, length)` annotations for constraints.
- Use `@Builder.Default` for fields with default values.
- `@CreationTimestamp` / `@UpdateTimestamp` for audit columns.
- Relationships: `@ManyToOne(fetch = FetchType.EAGER)` for parent refs; `@OneToMany(mappedBy, cascade = CascadeType.ALL, orphanRemoval = true)` for child collections.

---

## 5. Repository Pattern

```java
@Repository
public interface EntityNameRepository extends JpaRepository<EntityName, Long> {
    // Custom query methods using Spring Data method naming
    List<EntityName> findByFieldIgnoreCase(String field);
    boolean existsByField(String field);
    Optional<EntityName> findByField(String field);
}
```

### Key Rules
- Always extend `JpaRepository<Entity, Long>`.
- Use Spring Data derived queries where possible.
- Use `@Query` for complex queries only.

---

## 6. DTO Pattern

### Single DTO (for simple entities)
```java
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class EntityDTO {
    private Long id;

    @NotBlank(message = "Field is required")
    private String field;

    // response-only fields (no validation annotations)
    private String status;
}
```

### Grouped DTOs (for entities with multiple request/response shapes)
```java
public class EntityDTOs {

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class CreateRequest {
        @NotBlank private String field;
    }

    @Data @AllArgsConstructor @NoArgsConstructor @Builder
    public static class Response {
        private Long id;
        private String field;
        private String createdAt;
    }
}
```

### Key Rules
- Use Jakarta Validation: `@NotBlank`, `@NotNull`, `@Email`, `@Min`, `@DecimalMin`.
- Response DTOs must use `@Builder` for clean construction in service `mapToDTO()`.
- **Never expose Entity objects directly in controller responses** — always map to DTO.

---

## 7. Service Pattern

```java
@Service
@RequiredArgsConstructor
public class EntityService {

    private final EntityRepository entityRepository;

    public List<EntityDTO> getAll() {
        return entityRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public EntityDTO getById(Long id) {
        Entity entity = entityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Entity not found with id: " + id));
        return mapToDTO(entity);
    }

    @Transactional
    public EntityDTO create(EntityDTO dto) {
        Entity entity = Entity.builder()
                .field(dto.getField())
                .build();
        Entity saved = entityRepository.save(entity);
        return mapToDTO(saved);
    }

    @Transactional
    public EntityDTO update(Long id, EntityDTO dto) {
        Entity entity = entityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Entity not found with id: " + id));
        entity.setField(dto.getField());
        // ... set other fields
        Entity updated = entityRepository.save(entity);
        return mapToDTO(updated);
    }

    @Transactional
    public void delete(Long id) {
        if (!entityRepository.existsById(id)) {
            throw new ResourceNotFoundException("Entity not found with id: " + id);
        }
        entityRepository.deleteById(id);
    }

    public EntityDTO mapToDTO(Entity entity) {
        return EntityDTO.builder()
                .id(entity.getId())
                .field(entity.getField())
                .build();
    }
}
```

### Key Rules
- Inject repos via `@RequiredArgsConstructor` (constructor injection via Lombok).
- All write operations (`create`, `update`, `delete`) must be `@Transactional`.
- Always validate existence with `findById().orElseThrow()` or `existsById()`.
- Throw `ResourceNotFoundException` for 404, `BadRequestException` for 400.
- Every service has a private `mapToDTO()` method.

---

## 8. Controller Pattern

### Public Controller (User-facing)
```java
@RestController
@RequestMapping("/api/entities")
@RequiredArgsConstructor
public class EntityController {

    private final EntityService entityService;

    @GetMapping
    public ResponseEntity<List<EntityDTO>> getAll() {
        return ResponseEntity.ok(entityService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(entityService.getById(id));
    }
}
```

### Admin CRUD Controller (Permission-gated)
```java
@RestController
@RequestMapping("/api/admin/entities")
@PreAuthorize("hasAnyAuthority('ROLE_SUPERADMIN', 'ENTITIES_READ')")
@RequiredArgsConstructor
public class AdminEntityController {

    private final EntityService entityService;

    @GetMapping
    public ResponseEntity<List<EntityDTO>> getAll() {
        return ResponseEntity.ok(entityService.getAll());
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_SUPERADMIN', 'ENTITIES_CREATE')")
    public ResponseEntity<EntityDTO> create(@Valid @RequestBody EntityDTO dto) {
        return ResponseEntity.ok(entityService.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPERADMIN', 'ENTITIES_UPDATE')")
    public ResponseEntity<EntityDTO> update(@PathVariable Long id, @Valid @RequestBody EntityDTO dto) {
        return ResponseEntity.ok(entityService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPERADMIN', 'ENTITIES_DELETE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        entityService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
```

### Key Rules
- Controller methods return `ResponseEntity<T>`.
- Use `@Valid @RequestBody` for write endpoints.
- Use `@PathVariable` for ID-based lookups.
- Use `@RequestParam` for optional filters.
- For authenticated user access: `@AuthenticationPrincipal UserPrincipal currentUser`.
- **No business logic in controllers** — delegate everything to services.

---

## 9. RBAC & Security Architecture

### Roles Hierarchy
| Role | Description |
|---|---|
| `ROLE_USER` | Regular customer. Can browse, cart, book. |
| `ROLE_ADMIN` | Admin with **dynamic** permissions defined by their assigned `AdminRole`. |
| `ROLE_SUPERADMIN` | God-mode. **All checkboxes are always ticked.** Cannot be modified by anyone except another SUPERADMIN. The SUPERADMIN role itself is immutable — it is not a database `AdminRole` row but a hardcoded `Role` enum. |

### Permission Model
Permissions are stored per `AdminRole` in the `admin_permissions` table:

| Column | Type | Example |
|---|---|---|
| `module_name` | VARCHAR(50) | `PRODUCTS`, `BOOKINGS`, `USERS`, `ROLES` |
| `can_create` | BOOLEAN | `true` |
| `can_read` | BOOLEAN | `true` |
| `can_update` | BOOLEAN | `false` |
| `can_delete` | BOOLEAN | `false` |

Permissions are translated to Spring Security `GrantedAuthority` strings:
- Format: `{MODULE_NAME}_{ACTION}` → e.g. `PRODUCTS_CREATE`, `BOOKINGS_READ`
- This translation happens in `UserPrincipal.create(User user)`.

### `@PreAuthorize` Convention
- **Class-level**: `@PreAuthorize("hasAnyAuthority('ROLE_SUPERADMIN', '{MODULE}_READ')")` — gates all GET endpoints.
- **Method-level**: Override with specific action: `'ROLE_SUPERADMIN', '{MODULE}_CREATE'`, `'{MODULE}_UPDATE'`, `'{MODULE}_DELETE'`.
- **SUPERADMIN bypass**: Always include `'ROLE_SUPERADMIN'` in `hasAnyAuthority()` so superadmin never gets blocked.
- **SUPERADMIN-only controller limits**: Previously `AdminRoleController` was SUPERADMIN-only. It has been updated to support `ROLES_READ`, `ROLES_CREATE`, `ROLES_UPDATE`, `ROLES_DELETE` just like other controllers to allow admins to manage roles if they have the specific permissions.
### Security Constraints (hardcoded in `AdminService`)
1. **Cannot modify own role**: `if (userId.equals(currentUserId)) throw BadRequestException`.
2. **Only SUPERADMIN can touch SUPERADMIN**: Both assigning and removing.
3. **SUPERADMIN role is NOT an `AdminRole` record** — it's a hardcoded enum value. It has no database permission rows because it inherently has full access.

### Admin Portal Layout & UI Permissions (Frontend-Backend Contract)
The backend permissions (`canRead`, `canCreate`, `canUpdate`, `canDelete`) directly control the rendering of the Frontend's `AdminLayout` (defined in `FRONTEND_SPEC.md`) and its nested view components. The UI behaves strictly according to the following rules based on the user's permissions for a specific `moduleName`:

1. **Tab Visibility (Sidebar Navigation)**:
   - Requires `canRead` (or `ROLE_SUPERADMIN`).
   - If a user has `canRead = true` for a module (e.g., `PRODUCTS`), they will see the corresponding tab (e.g., "Products & Services") on the left-hand sidebar (`<AdminSidebar>`).
   - If `canRead = false`, the tab is completely hidden.

2. **Create Button Visibility**:
   - Requires `canCreate` (or `ROLE_SUPERADMIN`).
   - Example: The "+ Create User" or "+ Add Product" button inside the module's main view is only rendered if `hasPermission('MODULE', 'CREATE')` is true.

3. **Edit/Update Button Visibility**:
   - Requires `canUpdate` (or `ROLE_SUPERADMIN`).
   - Example: The "Edit" or "Manage Role" button on individual rows in a data table is only rendered if `hasPermission('MODULE', 'UPDATE')` is true.

4. **Delete Button Visibility**:
   - Requires `canDelete` (or `ROLE_SUPERADMIN`).
   - Example: The "Delete" button on individual rows in a data table is only rendered if `hasPermission('MODULE', 'DELETE')` is true.

---

## 10. Authentication Flow

### Login Flow
1. `POST /api/auth/login` → `AuthController` → `AuthService.login()`
2. `AuthenticationManager.authenticate()` triggers `UserDetailsServiceImpl.loadUserByUsername()`.
3. `UserPrincipal.create(user)` builds authorities from `user.getRole()` + `user.getAdminRole().getPermissions()`.
4. `JwtTokenProvider.generateToken()` creates a JWT with `sub = userId`.
5. Response: `JwtResponse { token, id, username, email, role, customRole, permissions[] }`.

### Request Authentication
1. `JwtAuthenticationFilter.doFilterInternal()` extracts Bearer token from `Authorization` header.
2. `tokenProvider.validateToken()` + `tokenProvider.getUserIdFromJWT()`.
3. `userDetailsService.loadUserById(userId)` → fresh `UserPrincipal` with current permissions.
4. Sets `SecurityContextHolder.getContext().setAuthentication(...)`.

### Route Protection (SecurityConfig)
```
/api/auth/**           → permitAll()
/api/products/** (GET) → permitAll()
/api/admin/**          → hasAnyAuthority("ROLE_ADMIN", "ROLE_SUPERADMIN")
everything else        → authenticated()
```

---

## 11. Exception Handling

All exceptions are caught by `GlobalExceptionHandler` (`@RestControllerAdvice`).

| Exception | HTTP Status | When to use |
|---|---|---|
| `ResourceNotFoundException` | 404 | Entity not found by ID |
| `BadRequestException` | 400 | Validation logic failures, duplicate names, permission violations |
| `MethodArgumentNotValidException` | 400 | Automatic — `@Valid` annotation failures |
| `Exception` (catch-all) | 500 | Unexpected errors |

Response body format:
```json
{
  "timestamp": "2026-08-06T12:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Username is already taken!"
}
```

---

## 12. Flyway Migration Rules

- Migration files live in: `src/main/resources/db/migration/`
- Naming: `V{N}__{description_snake_case}.sql` (double underscore).
- **NEVER modify an existing migration** that has already been applied.
- **Always create a new `V{N+1}__` file** for schema changes.
- Use `ALTER TABLE` for modifying existing tables.
- Use `INSERT INTO` for seed data, but **never hardcode IDs** — use auto-increment.
- Current versions:
  - `V1__init_schema.sql` — Core tables (users, products, cart_items, bookings, booking_items)
  - `V2__seed_data.sql` — Seed users + products + sample booking
  - `V3__add_admin_roles.sql` — admin_roles + admin_permissions tables, users.admin_role_id FK
  - `V4__seed_superadmin.sql` — Superadmin user seed

### Adding a New Module's Table
```sql
-- V5__add_my_module.sql
CREATE TABLE my_modules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    -- ... other columns ...
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

---

## 13. Configuration

### application.yml
```yaml
server:
  port: 8080

spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL}
    username: ${SPRING_DATASOURCE_USERNAME}
    password: ${SPRING_DATASOURCE_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: none          # Flyway manages all schema!
    show-sql: false
  flyway:
    enabled: true
    baseline-on-migrate: true

app:
  jwt:
    secret: ${JWT_SECRET}
    expiration-ms: 86400000   # 24 hours
```

### Environment Variables (`.env`)
```
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/n2n_booking_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
SPRING_DATASOURCE_USERNAME=n2n_user
SPRING_DATASOURCE_PASSWORD=n2n_password
JWT_SECRET=9a6e8b1c4d7f2e5a8c1b4d7f2e5a8c1b4d7f2e5a8c1b4d7f2e5a8c1b4d7f2e5a
```

---

## 14. Checklist: Adding a New Module

When adding a new module (e.g., "Promotions"), follow this exact order:

### Step 1: Database Migration
- [ ] Create `V{N}__add_promotions.sql` in `src/main/resources/db/migration/`
- [ ] Define table with `id BIGINT AUTO_INCREMENT PRIMARY KEY`, domain columns, `created_at`, `updated_at`

### Step 2: Entity
- [ ] Create `Promotion.java` in `entity/`
- [ ] Follow the entity template (Section 4)

### Step 3: Repository
- [ ] Create `PromotionRepository.java` in `repository/`
- [ ] Extend `JpaRepository<Promotion, Long>`

### Step 4: DTO
- [ ] Create `PromotionDTO.java` (or `PromotionDTOs.java` for grouped) in `dto/`
- [ ] Add validation annotations on request fields

### Step 5: Service
- [ ] Create `PromotionService.java` in `service/`
- [ ] Implement: `getAll()`, `getById()`, `create()`, `update()`, `delete()`, `mapToDTO()`

### Step 6: Public Controller (if user-facing)
- [ ] Create `PromotionController.java` in `controller/`
- [ ] `@RequestMapping("/api/promotions")`

### Step 7: Admin Controller
- [ ] Create `AdminPromotionController.java` in `controller/`
- [ ] `@RequestMapping("/api/admin/promotions")`
- [ ] Add `@PreAuthorize` with correct module name: `PROMOTIONS_READ`, `PROMOTIONS_CREATE`, etc.
- [ ] **ALWAYS include `'ROLE_SUPERADMIN'`** in every `hasAnyAuthority()` call

### Step 8: Register Module in RBAC
- [ ] Add `"PROMOTIONS"` as a valid `moduleName` in the `AdminPermission` entity comment and in the frontend `availableModules` array
- [ ] The SUPERADMIN automatically has full access — no database changes needed
- [ ] Existing admin roles can be updated via the Role Management UI to grant PROMOTIONS permissions

### Step 9: Update SecurityConfig (if needed)
- [ ] If public GET is needed: add `.requestMatchers(HttpMethod.GET, "/api/promotions/**").permitAll()`
- [ ] Admin endpoints are already covered by `.requestMatchers("/api/admin/**")`

### Step 10: Update AdminSidebar (Frontend)
- [ ] Add navigation link with `v-if="hasPermission('PROMOTIONS', 'READ')"`

---

## 15. Existing Modules Reference

| Module Name (RBAC) | Entity | Public API | Admin API | Admin Controller |
|---|---|---|---|---|
| — | `User` | — | `/api/admin/users` | `AdminUserController` |
| `PRODUCTS` | `Product` | `/api/products` | `/api/admin/products`, `/api/admin/products/{id}/schedule` | `AdminProductController` |
| `BOOKINGS` | `Booking` + `BookingItem` | `/api/bookings` | `/api/admin/bookings` | `AdminBookingController` |
| `USERS` | `User` | — | `/api/admin/users` | `AdminUserController` |
| `ROLES` | `AdminRole` + `AdminPermission` | — | `/api/admin/roles` | `AdminRoleController` (SUPERADMIN only) |
| — | `CartItem` | `/api/cart` | — | — |
| — | — | — | `/api/admin/dashboard` | `AdminDashboardController` |

---

## 16. Enums Reference

```java
// com.n2n.booking.enums.Role
ROLE_USER, ROLE_ADMIN, ROLE_SUPERADMIN

// com.n2n.booking.enums.BookingStatus
PENDING, CONFIRMED, CANCELLED, COMPLETED

// com.n2n.booking.enums.PaymentStatus
UNPAID, PAID, REFUNDED, FAILED
```

---

## 17. Error Response Contract

All error responses follow this JSON structure:

```json
{
  "timestamp": "ISO 8601 string",
  "status": 400,
  "error": "Bad Request | Not Found | Validation Failed | Internal Server Error",
  "message": "Human-readable error message"
}
```

For validation errors, an additional `errors` map is included:
```json
{
  "timestamp": "...",
  "status": 400,
  "error": "Validation Failed",
  "errors": {
    "name": "Product name is required",
    "price": "Price must be greater than zero"
  }
}
```
