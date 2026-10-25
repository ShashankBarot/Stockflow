# StockFlow — System Architecture & Technical Specifications

> **Status:** Partially Implemented (Auth verified; core domain stabilization underway)  
> **Target Environment:** Spring Boot 3 (Java 21/24), Next.js 16 (App Router), MySQL 8.0, Docker  
> **Architecture Level:** Software Architecture & Domain Specification  

---

## 1. System Overview & Core Architecture

StockFlow is an enterprise-grade Inventory & Stock Operations Management Platform designed to maintain high-integrity, real-time stock telemetry across distributed warehouses, supplier purchase pipelines, customer fulfillment streams, and inter-facility transfers.

```
                              ┌───────────────────────────────────────────┐
                              │             Next.js Frontend              │
                              │   (Next.js 16 App Router, React 19,      │
                              │    TanStack Query v5, Tailwind CSS v4)    │
                              └─────────────────────┬─────────────────────┘
                                                    │ REST (Bearer JWT)
                                                    ▼
┌─────────────────────────────────────────────────────────────────────────────────────────────────┐
│                                   Spring Boot Backend Gateway                                    │
│                              (Context Path: /api/v1, Port: 8080)                                │
│                                                                                                 │
│  ┌──────────────────────┐   ┌───────────────────────────┐   ┌────────────────────────────────┐  │
│  │   Security Filter    │──▶│   Controller Gateway      │──▶│      Service Domain            │  │
│  │  - JwtAuthFilter     │   │  - AuthController         │   │  - AuthService                 │  │
│  │  - RBAC (Role-based) │   │  - ProductController      │   │  - ProductService (Planned)    │  │
│  │  - CustomEntryPoint  │   │  - WarehouseController    │   │  - WarehouseService (Planned)  │  │
│  │  - AccessDeniedHndlr │   │  - InventoryController    │   │  - PurchaseService (Planned)   │  │
│  └──────────────────────┘   │  - MovementController     │   │  - SaleService (Planned)       │  │
│                             └─────────────┬─────────────┘   └───────────────┬────────────────┘  │
│                                           │                                 │                   │
│                                           │                 ┌───────────────▼────────────────┐  │
│                                           │                 │   CORE ENGINE (Tech Lead Only) │  │
│                                           └────────────────▶│   - StockService               │  │
│                                                             │   - StockMovementService       │  │
│                                                             │   - Concurrency Locking Engine │  │
│                                                             └───────────────┬──────────────── ┘  │
│                                                                             │                   │
│                                                                             ▼                   │
│                                                                     Data Repositories           │
└─────────────────────────────────────────────────────────────────────────────┬───────────────────┘
                                                                              │ Spring Data JPA
                                                                              ▼
                                                                ┌───────────────────────────┐
                                                                │      MySQL Database       │
                                                                │ (InnoDB, ACID compliant)  │
                                                                └───────────────────────────┘
```

---

## 2. Implementation Status Audit (Source of Truth)

An exhaustive code audit of the current repository establishes the following categorization:

### 2.1. CURRENTLY IMPLEMENTED
- **Authentication & Authorization Subsystem (Backend):**
  - Spring Security with Stateless JWT token filter (`JwtAuthenticationFilter`, `JwtTokenProvider`).
  - Access token generation (15m expiry) and Refresh token handling (7d expiry with database persistence).
  - Refresh token rotation and revocation logic in `AuthTokenService` and `RefreshTokenRepository`.
  - Roles (`ADMIN`, `MANAGER`, `STAFF`) stored in database and bootstrapped by `RoleDataInitializer`.
  - BCrypt password hashing and user registration (restricted to `ADMIN`).
  - Endpoints: `POST /api/v1/auth/login`, `POST /api/v1/auth/refresh`, `POST /api/v1/auth/logout`, `GET /api/v1/auth/me`, `POST /api/v1/auth/register`.
  - Automated integration test suite (`AuthSecurityIntegrationTest`, `JwtTokenProviderTest`).
- **Frontend Authentication & Session Handling:**
  - `useAuth` hook and `auth.service.ts` managing token persistence in `localStorage`.
  - Axios client with request bearer token injection and response interceptor for transparent 401 token refresh queue.
  - Role-protected routes via `<ProtectedRoute roles={['...']} />`.
  - Login page (`/login`), Registration page (`/register`), and Admin-only Settings page (`/settings`).
- **Frontend Design System & Visual Assets:**
  - Modern dark/light operational terminal interface using Tailwind CSS, Radix UI primitives, Lucide icons.
  - Reusable controls: `MetricsStrip`, `LiveFigure`, `DateRangePicker`, `SegmentedControl`, tables, dialogs, drawers.
- **Database Schema Models (Entities):**
  - `User`, `Role`, `RefreshToken`, `Product`, `Warehouse`, `Inventory`, `StockMovement`.

---

### 2.2. IN PROGRESS / PARTIALLY IMPLEMENTED (Defects Present)
- **Product & Warehouse Controllers:**
  - `ProductController` and `WarehouseController` exist, but annotate `@ConditionalOnBean(InventoryService.class)`. Since no bean implements `InventoryService`, these controllers are disabled by Spring Boot at runtime.
  - `WarehouseController` incorrectly delegates `getAllWarehouses` to `inventoryService.getInventory(pageable)` and `createWarehouse` to `inventoryService.createProduct(request)`.
  - `Product` entity uses an unnormalized `category` (plain `VARCHAR(100)` string). No `Category` or `Brand` foreign keys exist yet.
- **Stock Movement Controller:**
  - `MovementController` exists but also has `@ConditionalOnBean(InventoryService.class)`, preventing startup when enabled without an `InventoryService` bean.
- **Frontend Data Connectivity:**
  - `movement.service.ts` and `warehouse.service.ts` return raw `response.data` rather than unwrapping `response.data.data` (`ApiResponse` wrapper), breaking direct UI consumption.
  - Dashboard overview (`/`), Inventory (`/inventory`), Movements (`/movements`), and Warehouses (`/warehouses`) fall back to hardcoded in-memory arrays when API calls fail or return empty datasets.

---

### 2.3. PLANNED (Not Yet Implemented)
- **Core Stock Engine:**
  - `StockService` (orchestration of all stock ledger mutations).
  - Pessimistic write locking on `inventory` rows (`SELECT ... FOR UPDATE`).
  - True transactional immutability of `stock_movements`.
- **Master Data Subsystems:**
  - Dedicated `Category` entity, repository, service, controller, and UI.
  - `Brand` entity, repository, service, controller, and UI.
  - `Supplier` entity, repository, service, controller, and UI.
  - `Customer` entity, repository, service, controller, and UI.
- **Transactional Operational Workflows:**
  - **Purchase Orders:** `PurchaseOrder` and `PurchaseOrderItem` entities, receiving workflow with inbound stock movement.
  - **Sales Orders:** `SaleOrder` and `SaleOrderItem` entities, dispatch workflow with outbound stock movement and stock availability validation.
  - **Warehouse Transfers:** Transfer workflow with dual-leg ledger entries (origin outbound, destination inbound).
- **Analytics & Operational Telemetry:**
  - Real backend Reports API (valuation summaries, movement velocity, stockout forecasts).
  - Notifications subsystem (event-driven alerts for low stock and pending orders).
  - Audit Log subsystem (capturing user operations across the platform).

---

## 3. The Core Inventory Invariant & Architectural Rules

```
       PURCHASE ORDER                      SALES ORDER                     TRANSFER ORDER
    [Supplier Delivery]                [Customer Fulfillment]          [Inter-Facility Rebalance]
             │                                  │                                  │
             │ (PO Received)                    │ (Order Dispatched)               │ (Transfer Executed)
             ▼                                  ▼                                  ▼
   ┌────────────────────────────────────────────────────────────────────────────────────────┐
   │                                      StockService                                      │
   │               (Centralized, Transaction-Safe Inventory Mutation Engine)                 │
   │                                                                                        │
   │  1. Acquire Pessimistic Write Lock on Target Inventory Record(s)                       │
   │  2. Validate Constraints (Non-negative Stock, Warehouse Capacity Bounds)              │
   │  3. Calculate New Quantity:                                                            │
   │       - Inbound:     Q_new = Q_current + Q_delta                                       │
   │       - Outbound:    Q_new = Q_current - Q_delta  (Reject if Q_new < 0)                │
   │       - Adjustment:  Q_new = Q_absolute                                                │
   │  4. Update Inventory State                                                             │
   │  5. Append Immutable StockMovement Audit Record                                        │
   └───────────────────────────────────────────┬────────────────────────────────────────────┘
                                               │ Atomic JPA Transaction
                        ┌──────────────────────┴──────────────────────┐
                        ▼                                             ▼
             ┌─────────────────────┐                       ┌─────────────────────┐
             │   inventory Table   │                       │ stock_movements Tbl │
             │  (Mutable State)    │                       │  (Immutable Ledger) │
             └─────────────────────┘                       └─────────────────────┘
```

### Invariant Rules:
1. **No Direct Inventory Modification:** No service, controller, or batch job is permitted to directly mutate `inventory.quantity`. Every stock change MUST route through `StockService`.
2. **Immutable Audit Ledger:** Records in `stock_movements` are strictly write-once, read-many (append-only). Updates and deletes on this table are prohibited at both application and database permission levels.
3. **Pessimistic Concurrency Control:** When updating inventory, `StockService` MUST acquire a pessimistic write lock (`PESSIMISTIC_WRITE`) on the `(product_id, warehouse_id)` inventory tuple to eliminate race conditions under concurrent checkouts or intake batches.
4. **Non-Negative Stock Rule:** Outbound stock operations that would cause `quantity < 0` MUST fail with an `InsufficientStockException` and rollback the enclosing transaction.

---

## 4. API Standard & Contracts

### 4.1. Standard Response Envelope
All REST API endpoints communicate using the unified response format:
```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": { ... }
}
```
In case of errors:
```json
{
  "success": false,
  "message": "Detailed error message or validation summary",
  "data": null
}
```

### 4.2. Baseline API Catalog

| Method | Endpoint | Description | Auth Required | Roles Permitted | Request Body / Params |
|---|---|---|---|---|---|
| `POST` | `/auth/login` | Authenticate user & issue tokens | No | Public | `LoginRequest` (`username`, `password`) |
| `POST` | `/auth/refresh` | Rotate access & refresh token | No | Public | `RefreshTokenRequest` (`refreshToken`) |
| `POST` | `/auth/logout` | Revoke active refresh token | Yes | Any authenticated | `RefreshTokenRequest` (optional) |
| `GET`  | `/auth/me` | Current authenticated user profile | Yes | Any authenticated | None |
| `POST` | `/auth/register` | Create new staff/manager account | Yes | `ADMIN` | `RegisterRequest` (`username`, `email`, `password`) |
| `GET`  | `/products` | Paginated product list | Yes | `ADMIN`, `MANAGER`, `STAFF` | Query: `page`, `size`, `category`, `search` |
| `GET`  | `/products/{id}` | Retrieve product details | Yes | `ADMIN`, `MANAGER`, `STAFF` | Path: `id` |
| `POST` | `/products` | Create new product | Yes | `ADMIN`, `MANAGER` | `CreateProductRequest` |
| `PUT`  | `/products/{id}` | Update product information | Yes | `ADMIN`, `MANAGER` | `UpdateProductRequest` |
| `GET`  | `/warehouses` | List all warehouses | Yes | `ADMIN`, `MANAGER`, `STAFF` | Query: `page`, `size` |
| `POST` | `/warehouses` | Create warehouse facility | Yes | `ADMIN`, `MANAGER` | `CreateWarehouseRequest` |
| `GET`  | `/inventory` | Paginated stock levels | Yes | `ADMIN`, `MANAGER`, `STAFF` | Query: `page`, `size`, `warehouseId`, `lowStock` |
| `GET`  | `/inventory/warehouse/{warehouseId}` | Warehouse-specific inventory | Yes | `ADMIN`, `MANAGER`, `STAFF` | Path: `warehouseId` |
| `PUT`  | `/inventory/{id}/threshold` | Set minimum & capacity limits | Yes | `ADMIN`, `MANAGER` | `InventoryThresholdRequest` |
| `GET`  | `/movements` | Chronological movement ledger | Yes | `ADMIN`, `MANAGER`, `STAFF` | Query: `page`, `size`, `type`, `productId` |
| `POST` | `/movements` | Execute manual adjustment/movement | Yes | `ADMIN`, `MANAGER`, `STAFF` | `StockMovementRequest` |

---

## 5. Database Schema & Concurrency Strategy

### 5.1. Entity Relationships
- `users`: `(id, username, email, password_hash, role_id, created_at)`
- `roles`: `(id, name)` — seeded with `ADMIN`, `MANAGER`, `STAFF`
- `refresh_tokens`: `(id, user_id, token_hash, expires_at, revoked_at, created_at)`
- `products`: `(id, sku, name, description, category, base_price, created_at)`
- `warehouses`: `(id, name, location, created_at)`
- `inventory`: `(id, product_id, warehouse_id, quantity, min_threshold, max_capacity, updated_at)`
  - *Unique constraint:* `(product_id, warehouse_id)`
- `stock_movements`: `(id, product_id, warehouse_id, type, quantity, reference_number, performed_by, notes, created_at)`
  - *Foreign keys:* `product_id -> products.id`, `warehouse_id -> warehouses.id`, `performed_by -> users.id`

### 5.2. Critical Locking Strategy
```java
// InventoryRepository.java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT i FROM Inventory i WHERE i.product.id = :productId AND i.warehouse.id = :warehouseId")
Optional<Inventory> findByProductIdAndWarehouseIdForUpdate(
    @Param("productId") Long productId, 
    @Param("warehouseId") Long warehouseId
);
```
Every invocation in `StockService` that calculates `currentQuantity ± delta` MUST acquire this lock inside a `@Transactional(isolation = Isolation.READ_COMMITTED)` context.

---

## 6. Architecture Defect Register

1. **`@ConditionalOnBean(InventoryService.class)` Blockade:**  
   Currently prevents Spring from loading `ProductController`, `WarehouseController`, `InventoryController`, and `MovementController`. Must be addressed by decoupling domain services and implementing `InventoryServiceImpl` / `StockServiceImpl`.
2. **`WarehouseController` Service Routing:**  
   `WarehouseController` must be detached from `InventoryService` and given its own `WarehouseService`.
3. **Missing Maven Wrapper Executables:**  
   `mvnw` and `mvnw.cmd` must be restored in `backend/` for developer environment parity.
4. **Frontend Google Font Build Failure:**  
   `next/font/google` in `layout.tsx` fails when the build environment has restricted external network connectivity. Fallback standard sans-serif system fonts should be configured.
5. **Frontend ESLint Rule Breaches:**  
   7 component-level violations calling `setState` inside `useEffect` during mounting/modal open cycles.
