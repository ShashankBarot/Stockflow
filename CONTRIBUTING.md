# StockFlow — Developer Workflow & Contributing Guidelines

Welcome to the StockFlow engineering team. To enable three developers to work rapidly in parallel without merge conflicts, regressions, or architectural drift, all team members must adhere to this document.

---

## 1. Team Roster & Ownership Matrix

| Developer | Role | Primary Domain & Code Ownership | Protected Boundaries |
|---|---|---|---|
| **Developer 1** | **Technical Lead & Software Architect** | • Overall System Architecture & Contracts<br>• Core `StockService` & `StockMovementService`<br>• Pessimistic Locking & Concurrency Control<br>• Database Integrity & Migrations<br>• Security Review & CI/CD Pipeline<br>• Final PR Approvals & Integration | **Exclusive Owner** of `StockService`, `StockMovement` creation logic, `SecurityConfig.java`, and DDL schema scripts. |
| **Developer 2** | **Backend Developer (Spring Boot)** | • Master Data APIs (`Products`, `Categories`, `Brands`, `Suppliers`, `Customers`, `Warehouses`)<br>• Commercial Flows (`PurchaseOrders`, `SaleOrders`, `Transfers`) consuming `StockService`<br>• Reports & Analytics Services<br>• Notifications & Audit Subsystems<br>• Unit & SpringBootTest suites for feature domains | **Must NOT** bypass `StockService` to mutate `inventory` directly. Must route all stock deltas through `StockService`. |
| **Developer 3** | **Frontend Developer (Next.js)** | • Next.js App Router Pages & Layouts<br>• UI Component Library & Form Validations<br>• TanStack Query Hooks & State Cache<br>• API Integration Services (`*.service.ts`)<br>• Responsive Design, Modals, Drawers & Visualizations<br>• Preserving existing approved Design System | **Must NOT** modify backend DTO contracts unilaterally. Must adhere to agreed API envelopes (`ApiResponse<T>`). |

---

## 2. Feature Status Ledger

To avoid making assumptions during development, consult the status table:

- **CURRENTLY IMPLEMENTED:**
  - JWT Authentication & RBAC (`/auth/login`, `/auth/refresh`, `/auth/logout`, `/auth/me`, `/auth/register`).
  - Refresh Token Rotation & DB Session Revocation.
  - Frontend Auth State (`useAuth`), Protected Routes, Token Storage, Auto-Refresh Interceptor.
  - Base UI System (Tailwind v4, Radix UI, Lucide icons, Dark/Light operational layout).
- **IN PROGRESS / NEEDS FIX:**
  - Product & Warehouse Controllers (unblock `@ConditionalOnBean`, decouple from `InventoryService`).
  - Stock Movement Controller (connect to real service once created).
  - Frontend Service Unwrapping (`movement.service.ts` and `warehouse.service.ts`).
  - Frontend ESLint errors (7 component lifecycle violations).
- **PLANNED:**
  - `StockService` transactional concurrency engine.
  - Categories, Brands, Suppliers, Customers CRUD.
  - Purchase Orders, Sales Orders, Transfers workflows.
  - Real backend Reports & Analytics.
  - Real-time Notifications & System Audit Trail.

---

## 3. Git Workflow & Branching Strategy

We follow a strict **Gitflow-lite** model based on two persistent branches:

```
[main] (Production-ready, tagged releases only)
  ▲
  │ (Release PR after milestone validation)
[develop] (Integration branch; all features merge here)
  ▲
  ├── feature/core-stock-engine       (Dev 1)
  ├── feature/master-data-products   (Dev 2)
  ├── feature/product-management-ui   (Dev 3)
  └── fix/frontend-eslint-cleanup    (Dev 3)
```

### 3.1. Branch Naming Conventions
- Features: `feature/<developer>-<domain-scope>` (e.g., `feature/dev2-products-api`, `feature/dev3-products-ui`)
- Bug fixes: `fix/<developer>-<issue-scope>` (e.g., `fix/dev1-inventory-bean-blocker`)
- Architectural spikes / contract specs: `spike/<developer>-<scope>`

### 3.2. Commit Message Standards (Conventional Commits)
All commit messages must follow the format:
`<type>(<scope>): <short description in present tense>`

Examples:
- `feat(products): implement category and brand entity relationships`
- `fix(inventory): apply pessimistic write lock to stock deductions`
- `chore(deps): configure local font fallbacks for offline next build`
- `docs(contracts): define purchase order receive request DTO`

### 3.3. Pull Request & Review Protocol
1. **Never commit directly to `develop` or `main`.**
2. **Rebase Before PR:** Prior to opening a PR, rebase your feature branch on `origin/develop`:
   ```bash
   git fetch origin
   git rebase origin/develop
   ```
3. **Required Approvals:**
   - Dev 2 PRs require review from Dev 1 (Tech Lead) and sanity check from Dev 3 (for API contract alignment).
   - Dev 3 PRs require review from Dev 1.
   - Dev 1 PRs touching core services are open for architectural review by Dev 2.
4. **Merge Method:** Use **Squash and Merge** into `develop` to preserve a clean, linear commit log.

---

## 4. Conflict Avoidance & Protected File Protocol

Certain files are high-risk touchpoints that multiple features will interact with:

1. **`SecurityConfig.java`:**  
   *Problem:* Dev 2 adds new endpoints and attempts to change security matchers.  
   *Rule:* Dev 2 must announce new endpoint patterns in the API Contract issue. Dev 1 will approve and merge route authorization policies.
2. **`types/index.ts` (Frontend):**  
   *Rule:* Group type definitions by domain namespaces (`ProductTypes`, `PurchaseTypes`, etc.). Never modify existing working types without notifying the team.
3. **`sidebar.tsx` & App Navigation:**  
   *Rule:* Dev 3 owns navigation routing and sidebar icons. Backend devs must request new menu slots by filing a UI issue.
4. **Database Entities & Schema Migrations:**  
   *Rule:* All alterations to database columns or JPA table mappings must be coordinated with Dev 1 to prevent schema desynchronization.

---

## 5. API-First Development Protocol

To enable **Backend (Dev 2)** and **Frontend (Dev 3)** to build simultaneously:

1. **Contract Freeze:** The HTTP method, URI, request body JSON schema, and response body JSON schema must be documented in `ARCHITECTURE.md` before coding begins.
2. **Frontend Mocking:** Dev 3 creates mock data or Next.js route handlers matching the exact contract in `types/index.ts`. Dev 3 builds and tests UI without waiting for backend deployment.
3. **Backend Implementation:** Dev 2 implements the Spring Boot Controller and validates using MockMvc integration tests against the contract.
4. **Integration Handshake:** Once Dev 2 merges into `develop`, Dev 3 flips the service from mock to the live `/api/v1` endpoint.

---

## 6. Pre-Commit Checklist & Quality Gates

Before opening any Pull Request, developers must run the following checks locally:

### For Backend Developers (Dev 1 & Dev 2):
```bash
# Verify backend compiles and passes all integration tests
mvn clean test
```
*Gate:* Zero test failures. Any PR that breaks `AuthSecurityIntegrationTest` will be rejected immediately.

### For Frontend Developers (Dev 3):
```bash
# Check TypeScript types
npx tsc --noEmit

# Run ESLint validation
npm run lint
```
*Gate:* Zero TypeScript errors and zero ESLint errors (`43 problems (7 errors)` must be cleaned up as Milestone 0 priority).
