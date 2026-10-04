import axiosInstance from "@/lib/axios";
import type {
  StockMovement,
  CreateMovementRequest,
  PaginatedResponse,
} from "@/types";

export const movementService = {
  createMovement: async (data: CreateMovementRequest): Promise<StockMovement> => {
    const response = await axiosInstance.post("/movements", data);
    return response.data;
  },

  getMovements: async (params?: Record<string, unknown>): Promise<PaginatedResponse<StockMovement>> => {
    const response = await axiosInstance.get("/movements", { params });
    return response.data;
  },

  getMovement: async (id: number): Promise<StockMovement> => {
    const response = await axiosInstance.get(`/movements/${id}`);
    return response.data;
  },
};
