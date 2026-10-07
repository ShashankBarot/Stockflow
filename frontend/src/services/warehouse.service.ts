import axiosInstance from "@/lib/axios";
import type { Warehouse, CreateWarehouseRequest, PaginatedResponse } from "@/types";

export const warehouseService = {
  getWarehouses: async (): Promise<PaginatedResponse<Warehouse>> => {
    const response = await axiosInstance.get("/warehouses");
    const payload = response.data;
    const inner = payload?.data;
    const items: Warehouse[] = Array.isArray(inner)
      ? inner
      : Array.isArray(inner?.content)
      ? inner.content
      : Array.isArray(payload?.content)
      ? payload.content
      : [];

    return {
      success: payload?.success ?? true,
      message: payload?.message ?? "",
      data: items,
      totalPages: inner?.totalPages ?? 1,
      totalElements: inner?.totalElements ?? items.length,
      currentPage: inner?.number ?? 0,
      pageSize: inner?.size ?? items.length,
    };
  },

  createWarehouse: async (data: CreateWarehouseRequest): Promise<Warehouse> => {
    const response = await axiosInstance.post("/warehouses", data);
    return response.data;
  },
};
