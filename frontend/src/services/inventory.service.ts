import axiosInstance from "@/lib/axios";
import type {
  Product,
  CreateProductRequest,
  Inventory,
  InventoryThresholdRequest,
  PaginatedResponse,
} from "@/types";

function unwrap<T>(response: { data: unknown }): T {
  const payload = response.data;
  return payload && typeof payload === "object" && "data" in payload ? (payload as { data: T }).data : payload as T;
}

function toPage<T>(response: { data: unknown }, mapItem: (item: unknown) => T = (item) => item as T): PaginatedResponse<T> {
  const envelope = response.data as { success?: boolean; message?: string; data?: unknown } | null;
  const payload = envelope && typeof envelope === "object" && "data" in envelope
    ? envelope.data
    : response.data;
  const page = payload && typeof payload === "object" && !Array.isArray(payload)
    ? payload as { content?: unknown[]; totalPages?: number; totalElements?: number; number?: number; size?: number }
    : undefined;
  const rawItems = Array.isArray(payload) ? payload : Array.isArray(page?.content) ? page.content : [];
  const items = rawItems.map(mapItem);

  return {
    success: envelope?.success ?? true,
    message: envelope?.message ?? "",
    data: items,
    totalPages: page?.totalPages ?? 1,
    totalElements: page?.totalElements ?? items.length,
    currentPage: page?.number ?? 0,
    pageSize: page?.size ?? items.length,
  };
}

function normalizeProduct(item: unknown): Product {
  const product = item as Product & { categoryName?: string; brandName?: string };
  return {
    ...product,
    category: product.categoryName ?? product.category ?? "",
    brand: product.brandName ?? product.brand,
  };
}

export const inventoryService = {
  // Products
  getProducts: async (page?: number, size?: number): Promise<PaginatedResponse<Product>> => {
    const response = await axiosInstance.get("/products", {
      params: { page, size },
    });
    return toPage(response, normalizeProduct);
  },

  getProduct: async (id: number): Promise<Product> => {
    const response = await axiosInstance.get(`/products/${id}`);
    return normalizeProduct(unwrap(response));
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
    return toPage(response);
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
