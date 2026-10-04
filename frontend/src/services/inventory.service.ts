import axiosInstance from "@/lib/axios";
import type {
  Product,
  CreateProductRequest,
  Inventory,
  InventoryThresholdRequest,
  PaginatedResponse,
} from "@/types";

function unwrap<T>(response: { data: { data: T } | T }): T {
  const payload = response.data;
  return payload && typeof payload === "object" && "data" in payload ? (payload as { data: T }).data : payload as T;
}

export const inventoryService = {
  // Products
  getProducts: async (page?: number, size?: number): Promise<PaginatedResponse<Product>> => {
    const response = await axiosInstance.get("/products", {
      params: { page, size },
    });
    return unwrap(response);
  },

  getProduct: async (id: number): Promise<Product> => {
    const response = await axiosInstance.get(`/products/${id}`);
    return unwrap(response);
  },

  createProduct: async (data: CreateProductRequest): Promise<Product> => {
    const response = await axiosInstance.post("/products", data);
    return unwrap(response);
  },

  updateProduct: async (id: number, data: Partial<CreateProductRequest>): Promise<Product> => {
    const response = await axiosInstance.put(`/products/${id}`, data);
    return unwrap(response);
  },

  // Inventory
  getInventory: async (params?: Record<string, unknown>): Promise<PaginatedResponse<Inventory>> => {
    const response = await axiosInstance.get("/inventory", { params });
    return unwrap(response);
  },

  getInventoryByWarehouse: async (warehouseId: number): Promise<Inventory[]> => {
    const response = await axiosInstance.get(`/inventory/warehouse/${warehouseId}`);
    return unwrap(response);
  },

  updateInventoryThreshold: async (id: number, data: InventoryThresholdRequest): Promise<Inventory> => {
    const response = await axiosInstance.put(`/inventory/${id}/threshold`, data);
    return unwrap(response);
  },
};
