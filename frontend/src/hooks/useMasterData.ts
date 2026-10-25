"use client";

import { useQuery } from "@tanstack/react-query";
import { masterDataService } from "@/services/master-data.service";

export function useCategories() {
  return useQuery({
    queryKey: ["categories"],
    queryFn: masterDataService.getCategories,
  });
}

export function useBrands() {
  return useQuery({
    queryKey: ["brands"],
    queryFn: masterDataService.getBrands,
  });
}
