"use client";

import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { inventoryService } from "@/services/inventory.service";
import type {
  Product,
  Inventory,
  CreateProductRequest,
  InventoryThresholdRequest,
} from "@/types";

export function useProducts(page: number = 0, size: number = 20) {
  return useQuery({
    queryKey: ["products", page, size],
    queryFn: () => inventoryService.getProducts(page, size),
  });
}

export function useProduct(id: number) {
  return useQuery({
    queryKey: ["product", id],
    queryFn: () => inventoryService.getProduct(id),
    enabled: !!id,
  });
}

export function useCreateProduct() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: CreateProductRequest) => inventoryService.createProduct(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["products"] });
    },
  });
}

export function useUpdateProduct() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: Partial<CreateProductRequest> }) =>
      inventoryService.updateProduct(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["products"] });
    },
  });
}

export function useInventory(params?: Record<string, unknown>) {
  return useQuery({
    queryKey: ["inventory", params],
    queryFn: () => inventoryService.getInventory(params),
  });
}

export function useUpdateInventoryThreshold() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: InventoryThresholdRequest }) =>
      inventoryService.updateInventoryThreshold(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["inventory"] });
    },
  });
}
