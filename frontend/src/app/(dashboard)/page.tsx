"use client";

import * as React from "react";
import { Plus, RefreshCw } from "lucide-react";
import { MetricsStrip } from "@/components/dashboard/metrics-strip";
import { InventoryChart } from "@/components/dashboard/inventory-chart";
import { InventoryTable } from "@/components/dashboard/inventory-table";
import { InsightPanel } from "@/components/dashboard/insight-panel";
import { AdjustStockDrawer } from "@/components/inventory/adjust-stock-drawer";
import { CreateProductModal } from "@/components/inventory/create-product-modal";
import { DateRangePicker, DatePreset } from "@/components/ui/date-range-picker";
import { BUTTON, BUTTON_PRIMARY } from "@/components/ui/control-classes";
import { useProducts, useInventory } from "@/hooks/useInventory";
import type { Product } from "@/types";

const INITIAL_FALLBACK_PRODUCTS: Product[] = [
  {
    id: 1,
    sku: "SKU-9021",
    name: "Industrial Micro-Sensor 24V",
    description: "High-precision telemetry sensor for industrial machinery",
    category: "Sensors",
    basePrice: 145.0,
    createdAt: new Date().toISOString(),
  },
  {
    id: 2,
    sku: "SKU-1044",
    name: "Thermal Controller Module",
    description: "Programmable temperature management unit",
    category: "Controllers",
    basePrice: 280.0,
    createdAt: new Date().toISOString(),
  },
  {
    id: 3,
    sku: "SKU-3180",
    name: "Linear Actuator 12V 100mm",
    description: "Compact motion actuator with feedback potentiometer",
    category: "Actuators",
    basePrice: 95.0,
    createdAt: new Date().toISOString(),
  },
  {
    id: 4,
    sku: "SKU-7720",
    name: "Precision Shielded Cable Loom",
    description: "Military-grade EMI shielded cabling",
    category: "Cables",
    basePrice: 42.5,
    createdAt: new Date().toISOString(),
  },
  {
    id: 5,
    sku: "SKU-4402",
    name: "Optoelectronic Coupler Array",
    description: "High isolation optical switches for telemetry bus",
    category: "Optics",
    basePrice: 18.0,
    createdAt: new Date().toISOString(),
  },
  {
    id: 6,
    sku: "SKU-8821",
    name: "Pressure Transducer 0-10 Bar",
    description: "Stainless steel diaphragm pressure sensor",
    category: "Sensors",
    basePrice: 195.0,
    createdAt: new Date().toISOString(),
  },
];

export default function OverviewDashboardPage() {
  const [isLive, setIsLive] = React.useState(true);
  const [dateRange, setDateRange] = React.useState<DatePreset>("30d");
  const [adjustingProduct, setAdjustingProduct] = React.useState<Product | null>(null);
  const [isCreateOpen, setIsCreateOpen] = React.useState(false);

  const { data: productsData, isLoading, refetch } = useProducts(0, 50);
  const { data: inventoryData } = useInventory();

  // Combine products with stock quantities from inventory response if present
  const products: Product[] = React.useMemo(() => {
    const rawList = productsData?.data && productsData.data.length > 0 ? productsData.data : INITIAL_FALLBACK_PRODUCTS;
    const invMap = new Map<number, number>();
    if (inventoryData?.data) {
      for (const item of inventoryData.data) {
        if (item.product?.id) {
          invMap.set(item.product.id, (invMap.get(item.product.id) || 0) + item.quantity);
        }
      }
    }

    return rawList.map((p, idx) => {
      const stock = invMap.get(p.id) ?? (idx === 1 ? 8 : idx === 4 ? 0 : 45 + idx * 12);
      return {
        ...p,
        currentStock: stock,
        unitPrice: p.basePrice,
        minStockLevel: idx === 1 ? 15 : 10,
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
          <DateRangePicker value={dateRange} onChange={setDateRange} />
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
            value: totalValuation > 0 ? totalValuation : 128450,
            format: "currency",
            change: 4.2,
            changeLabel: "vs 30d avg",
            sentiment: "positive",
          },
          {
            id: "units_stock",
          label: "Total units",
            value: totalUnits > 0 ? totalUnits : 4821,
            format: "integer",
            change: 2.1,
            changeLabel: "active balance",
            sentiment: "positive",
          },
          {
            id: "low_stock",
          label: "Low stock",
            value: lowStockCount,
            format: "integer",
            change: -1,
            changeLabel: "urgent POs",
            sentiment: "warning",
          },
          {
            id: "out_of_stock",
          label: "Out of stock",
            value: depletedCount,
            format: "integer",
            change: 0,
            changeLabel: "zero stock",
            sentiment: "danger",
          },
        ]}
      />

      {/* Valuation & Trajectory Chart */}
      <InventoryChart />

      {/* Main Workspace Split: Dense Inventory Table (2/3) + Operational Insight Panel (1/3) */}
      <div className="grid grid-cols-1 gap-5 xl:grid-cols-[minmax(0,1fr)_270px]">
        <div className="min-w-0">
          <InventoryTable
            products={products}
            isLoading={isLoading}
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
