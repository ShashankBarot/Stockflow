"use client";

import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { movementService } from "@/services/movement.service";
import type { StockMovement, CreateMovementRequest } from "@/types";

export function useMovements(params?: Record<string, unknown>) {
  return useQuery({
    queryKey: ["movements", params],
    queryFn: () => movementService.getMovements(params),
  });
}

export function useMovement(id: number) {
  return useQuery({
    queryKey: ["movement", id],
    queryFn: () => movementService.getMovement(id),
    enabled: !!id,
  });
}

export function useCreateMovement() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: CreateMovementRequest) => movementService.createMovement(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["movements"] });
      queryClient.invalidateQueries({ queryKey: ["inventory"] });
    },
  });
}
