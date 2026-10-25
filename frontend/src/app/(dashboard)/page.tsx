"use client";

import * as React from "react";
import { Plus, RefreshCw } from "lucide-react";
import { MetricsStrip } from "@/components/dashboard/metrics-strip";
import { InventoryChart } from "@/components/dashboard/inventory-chart";
import { InventoryTable } from "@/components/dashboard/inventory-table";
import { InsightPanel } from "@/components/dashboard/insight-panel";
import { AdjustStockDrawer } from "@/components/inventory/adjust-stock-drawer";
import { CreateProductModal } from "@/components/inventory/create-product-modal";
import { BUTTON, BUTTON_PRIMARY } from "@/components/ui/control-classes";
import { useProducts, useInventory } from "@/hooks/useInventory";
import type { Product } from "@/types";

export default function OverviewDashboardPage() {
  const [adjustingProduct, setAdjustingProduct] = React.useState<Product | null>(null);
  const [isCreateOpen, setIsCreateOpen] = React.useState(false);

  const { data: productsData, isLoading, refetch } = useProducts(0, 500);
  const { data: inventoryData, isLoading: isInventoryLoading } = useInventory({ page: 0, size: 500 });

  // Combine products with stock quantities from inventory response if present
  const products: Product[] = React.useMemo(() => {
    const rawList = productsData?.data ?? [];
    const invMap = new Map<number, { quantity: number; minThreshold: number }>();
    if (inventoryData?.data) {
      for (const item of inventoryData.data) {
        if (item.product?.id) {
          const current = invMap.get(item.product.id) ?? { quantity: 0, minThreshold: 0 };
          invMap.set(item.product.id, {
            quantity: current.quantity + item.quantity,
            minThreshold: current.minThreshold + item.minThreshold,
          });
        }
      }
    }

    return rawList.map((p) => {
      const inventory = invMap.get(p.id);
      return {
        ...p,
        currentStock: inventory?.quantity ?? 0,
        unitPrice: p.basePrice,
        minStockLevel: inventory?.minThreshold ?? 0,
      } as Product & { currentStock: number; unitPrice: number; minStockLevel: number };
    });
  }, [productsData, inventoryData]);

  // Aggregate metrics
  const totalValuation = React.useMemo(() => {
    return products.reduce((acc, p) => {
      const stock = (p as unknown as { currentStock?: number }).currentStock ?? 0;
      const price = p.basePrice ?? 0;
      return acc + stock * price;
    }, 0);
  }, [products]);

  const totalUnits = React.useMemo(() => {
    return products.reduce((acc, p) => {
      return acc + ((p as unknown as { currentStock?: number }).currentStock ?? 0);
    }, 0);
  }, [products]);

  const lowStockCount = React.useMemo(() => {
    return products.filter((p) => {
      const s = (p as unknown as { currentStock?: number }).currentStock ?? 0;
      const min = (p as unknown as { minStockLevel?: number }).minStockLevel ?? 10;
      return s > 0 && s <= min;
    }).length;
  }, [products]);

  const depletedCount = React.useMemo(() => {
    return products.filter((p) => ((p as unknown as { currentStock?: number }).currentStock ?? 0) <= 0).length;
  }, [products]);

  return (
    <div className="mx-auto max-w-[1600px] space-y-5">
      {/* Top Header Section */}
      <div className="flex flex-col gap-3 border-b border-white/[0.07] pb-4 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <div className="flex items-center gap-2.5">
            <h1 className="text-[27px] font-medium tracking-tight text-foreground md:text-[30px]">
              Overview
            </h1>
          </div>
          <p className="mt-1 text-xs text-muted-foreground">
            Inventory performance, valuation and warehouse health.
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-2">
          <button
            type="button"
            onClick={() => refetch()}
            className={BUTTON}
            title="Refresh inventory"
            aria-label="Refresh inventory"
          >
            <RefreshCw className="h-3.5 w-3.5" />
          </button>
          <button
            type="button"
            onClick={() => setIsCreateOpen(true)}
            className={BUTTON_PRIMARY}
          >
            <Plus className="h-3.5 w-3.5" />
            <span>Add product</span>
          </button>
        </div>
      </div>

      {/* Metrics Strip */}
      <MetricsStrip
        metrics={[
          {
            id: "total_val",
          label: "Total inventory value",
            value: totalValuation,
            format: "currency",
            sentiment: "positive",
          },
          {
            id: "units_stock",
          label: "Total units",
            value: totalUnits,
            format: "integer",
            sentiment: "positive",
          },
          {
            id: "low_stock",
          label: "Low stock",
            value: lowStockCount,
            format: "integer",
            sentiment: "warning",
          },
          {
            id: "out_of_stock",
          label: "Out of stock",
            value: depletedCount,
            format: "integer",
            sentiment: "danger",
          },
        ]}
      />

      {/* Valuation & Trajectory Chart */}
      <InventoryChart products={products} />

      {/* Main Workspace Split: Dense Inventory Table (2/3) + Operational Insight Panel (1/3) */}
      <div className="grid grid-cols-1 gap-5 xl:grid-cols-[minmax(0,1fr)_270px]">
        <div className="min-w-0">
          <InventoryTable
            products={products}
            isLoading={isLoading || isInventoryLoading}
            onAdjustStock={(prod) => setAdjustingProduct(prod)}
            onCreateProduct={() => setIsCreateOpen(true)}
          />
        </div>

        <aside className="min-w-0">
          <InsightPanel />
        </aside>
      </div>

      {/* Modals & Drawers */}
      <AdjustStockDrawer
        product={adjustingProduct}
        isOpen={!!adjustingProduct}
        onClose={() => setAdjustingProduct(null)}
        onSuccess={() => refetch()}
      />

      <CreateProductModal
        isOpen={isCreateOpen}
        onClose={() => setIsCreateOpen(false)}
        onSuccess={() => refetch()}
      />
    </div>
  );
}
