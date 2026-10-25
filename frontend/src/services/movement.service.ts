import axiosInstance from "@/lib/axios";
import axios from "axios";
import type {
  StockMovement,
  CreateMovementRequest,
  PaginatedResponse,
} from "@/types";

function unwrap<T>(response: { data: unknown }): T {
  const payload = response.data;
  return payload && typeof payload === "object" && "data" in payload
    ? (payload as { data: T }).data
    : payload as T;
}

function unwrapPage<T>(response: { data: unknown }): PaginatedResponse<T> {
  const envelope = response.data as { success?: boolean; message?: string; data?: unknown } | null;
  const payload = envelope && typeof envelope === "object" && "data" in envelope ? envelope.data : response.data;
  const page = payload && typeof payload === "object" && !Array.isArray(payload)
    ? payload as { content?: unknown[]; totalPages?: number; totalElements?: number; number?: number; size?: number }
    : undefined;
  const items = Array.isArray(payload) ? payload as T[] : Array.isArray(page?.content) ? page.content as T[] : [];

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

export const movementService = {
  createMovement: async (data: CreateMovementRequest): Promise<StockMovement> => {
    try {
      const response = await axiosInstance.post("/movements", data);
      return unwrap(response);
    } catch (error: unknown) {
      if (axios.isAxiosError(error)) {
        const message = (error.response?.data as { message?: string } | undefined)?.message;
        if (message) throw new Error(message);
      }
      throw error;
    }
  },

  getMovements: async (params?: Record<string, unknown>): Promise<PaginatedResponse<StockMovement>> => {
    const response = await axiosInstance.get("/movements", { params });
    return unwrapPage(response);
  },

  getMovement: async (id: number): Promise<StockMovement> => {
    const response = await axiosInstance.get(`/movements/${id}`);
    return unwrap(response);
  },
};
