// ===================== User Types =====================
export interface User {
  id: number;
  username: string;
  email: string;
  role: Role;
  createdAt: string;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  user: User;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  roleId: number;
}

// ===================== Role Types =====================
export interface Role {
  id: number;
  name: string;
}

// ===================== Product Types =====================
export interface Product {
  id: number;
  sku: string;
  name: string;
  description: string;
  category: string;
  basePrice: number;
  createdAt: string;
}

export interface CreateProductRequest {
  sku: string;
  name: string;
  description: string;
  category: string;
  basePrice: number;
}

// ===================== Warehouse Types =====================
export interface Warehouse {
  id: number;
  name: string;
  location: string;
  createdAt: string;
}

export interface CreateWarehouseRequest {
  name: string;
  location: string;
}

// ===================== Inventory Types =====================
export interface Inventory {
  id: number;
  product: Product;
  warehouse: Warehouse;
  quantity: number;
  minThreshold: number;
  maxCapacity: number;
  updatedAt: string;
}

export interface InventoryThresholdRequest {
  minThreshold: number;
  maxCapacity: number;
}

// ===================== Stock Movement Types =====================
export type MovementType = "INBOUND" | "OUTBOUND" | "TRANSFER" | "ADJUSTMENT";

export interface StockMovement {
  id: number;
  product: Product;
  warehouse: Warehouse;
  type: MovementType;
  quantity: number;
  referenceNumber: string;
  performedBy: User;
  notes: string;
  createdAt: string;
}

export interface CreateMovementRequest {
  productId: number;
  warehouseId: number;
  type: MovementType;
  quantity: number;
  referenceNumber: string;
  destinationWarehouseId?: number; // Only for TRANSFER
  notes: string;
}

// ===================== API Response Types =====================
export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

export interface PaginatedResponse<T> {
  success: boolean;
  message: string;
  data: T[];
  totalPages: number;
  totalElements: number;
  currentPage: number;
  pageSize: number;
}
