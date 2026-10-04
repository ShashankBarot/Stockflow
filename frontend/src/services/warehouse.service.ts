import axiosInstance from "@/lib/axios";
import type { Warehouse, CreateWarehouseRequest, PaginatedResponse } from "@/types";

export const warehouseService = {
  getWarehouses: async (): Promise<PaginatedResponse<Warehouse>> => {
    const response = await axiosInstance.get("/warehouses");
    return response.data;
  },

  createWarehouse: async (data: CreateWarehouseRequest): Promise<Warehouse> => {
    const response = await axiosInstance.post("/warehouses", data);
    return response.data;
  },
};
