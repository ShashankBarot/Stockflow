import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { warehouseService } from "@/services/warehouse.service";
import type { CreateWarehouseRequest } from "@/types";

export const WAREHOUSE_QUERY_KEYS = {
  all: ["warehouses"] as const,
  lists: () => [...WAREHOUSE_QUERY_KEYS.all, "list"] as const,
};

export function useWarehouses() {
  return useQuery({
    queryKey: WAREHOUSE_QUERY_KEYS.lists(),
    queryFn: () => warehouseService.getWarehouses(),
  });
}

export function useCreateWarehouse() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: CreateWarehouseRequest) => warehouseService.createWarehouse(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: WAREHOUSE_QUERY_KEYS.all });
    },
  });
}
