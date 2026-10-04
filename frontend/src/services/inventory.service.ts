import axiosInstance from "@/lib/axios";
import type {
  Product,
  CreateProductRequest,
  Inventory,
  InventoryThresholdRequest,
  PaginatedResponse,
} from "@/types";

export const inventoryService = {
  // Products
  getProducts: async (page?: number, size?: number): Promise<PaginatedResponse<Product>> => {
    const response = await axiosInstance.get("/products", {
      params: { page, size },
    });
    return response.data;
  },

  getProduct: async (id: number): Promise<Product> => {
    const response = await axiosInstance.get(`/products/${id}`);
    return response.data;
  },

  createProduct: async (data: CreateProductRequest): Promise<Product> => {
    const response = await axiosInstance.post("/products", data);
    return response.data;
  },

  updateProduct: async (id: number, data: Partial<CreateProductRequest>): Promise<Product> => {
    const response = await axiosInstance.put(`/products/${id}`, data);
    return response.data;
  },

  // Inventory
  getInventory: async (params?: Record<string, unknown>): Promise<PaginatedResponse<Inventory>> => {
    const response = await axiosInstance.get("/inventory", { params });
    return response.data;
  },

  getInventoryByWarehouse: async (warehouseId: number): Promise<Inventory[]> => {
    const response = await axiosInstance.get(`/inventory/warehouse/${warehouseId}`);
    return response.data;
  },

  updateInventoryThreshold: async (id: number, data: InventoryThresholdRequest): Promise<Inventory> => {
    const response = await axiosInstance.put(`/inventory/${id}/threshold`, data);
    return response.data;
  },
};
