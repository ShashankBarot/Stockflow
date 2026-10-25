import axiosInstance from "@/lib/axios";
import type { CatalogOption } from "@/types";

function unwrapPage(response: { data: unknown }): CatalogOption[] {
  const envelope = response.data as { data?: unknown } | null;
  const payload = envelope && typeof envelope === "object" && "data" in envelope
    ? envelope.data
    : response.data;
  if (Array.isArray(payload)) return payload as CatalogOption[];
  if (payload && typeof payload === "object" && "content" in payload && Array.isArray(payload.content)) {
    return payload.content as CatalogOption[];
  }
  return [];
}

export const masterDataService = {
  getCategories: async (): Promise<CatalogOption[]> => {
    const response = await axiosInstance.get("/categories", { params: { page: 0, size: 100 } });
    return unwrapPage(response);
  },
  getBrands: async (): Promise<CatalogOption[]> => {
    const response = await axiosInstance.get("/brands", { params: { page: 0, size: 100 } });
    return unwrapPage(response);
  },
};
