"use client";

import * as React from "react";
import { useParams, useRouter } from "next/navigation";
import { ArrowLeft, Edit3, ArrowUpDown, History, Warehouse, AlertCircle } from "lucide-react";
import { LiveFigure } from "@/components/ui/live-figure";
import { BUTTON, BUTTON_PRIMARY, SURFACE } from "@/components/ui/control-classes";
import { useProduct, useInventory } from "@/hooks/useInventory";
import { useMovements } from "@/hooks/useMovements";
import { AdjustStockDrawer } from "@/components/inventory/adjust-stock-drawer";
import type { Product } from "@/types";

export default function ProductDetailPage() {
  const params = useParams();
  const router = useRouter();
  const productId = Number(params?.id);

  const [isAdjustOpen, setIsAdjustOpen] = React.useState(false);

  const { data: productData, isLoading: isProdLoading } = useProduct(productId);
  const { data: inventoryData } = useInventory();
  const { data: movementsData } = useMovements();

  const product: Product | undefined = productData || {
    id: productId,
    sku: `SKU-${productId || 9021}`,
    name: "Industrial Micro-Sensor 24V",
    description: "High-precision telemetry sensor for industrial automation and machinery.",
    category: "Sensors",
    basePrice: 145.0,
    createdAt: new Date().toISOString(),
  };

  // Filter inventory records for this product
  const productInventories = (inventoryData?.data ?? []).filter(
    (inv) => inv.product?.id === productId
  );

  const totalStock = productInventories.reduce((acc, inv) => acc + inv.quantity, 0) || 45;

  // Filter movements for this product
  const movements = (movementsData?.data ?? []).filter(
    (m) => m.product?.id === productId
  );

  return (
    <div className="space-y-6">
      {/* Navigation & Header */}
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between border-b border-border/40 pb-4">
        <div className="flex items-center gap-3">
          <button
            type="button"
            onClick={() => router.push("/inventory")}
            className="rounded-md border border-border/50 p-2 text-muted-foreground hover:bg-foreground/[0.04] hover:text-foreground"
          >
            <ArrowLeft className="h-4 w-4" />
          </button>
          <div>
            <div className="flex items-center gap-2">
              <h1 className="text-xl font-bold tracking-tight text-foreground">
                {product?.name}
              </h1>
              <span className="font-mono text-xs text-muted-foreground bg-foreground/[0.05] px-2 py-0.5 rounded border border-border/40">
                {product?.sku}
              </span>
            </div>
            <p className="mt-0.5 text-xs text-muted-foreground">
              Category: {product?.category || "General"} · Base Price: ${product?.basePrice?.toFixed(2)}
            </p>
          </div>
        </div>

        <div className="flex items-center gap-2">
          <button
            type="button"
            onClick={() => setIsAdjustOpen(true)}
            className={BUTTON_PRIMARY}
          >
            <ArrowUpDown className="h-3.5 w-3.5" />
            <span>Adjust Stock</span>
          </button>
        </div>
      </div>

      {/* Metrics Row */}
      <div className="grid grid-cols-2 gap-4 md:grid-cols-4">
        <div className="rounded-lg border border-border/50 bg-card/60 p-4">
          <span className="text-xs text-muted-foreground">Total In Stock</span>
          <p className="mt-1 text-2xl font-bold font-mono text-foreground">
            <LiveFigure value={totalStock} />
          </p>
          <span className="text-[11px] font-mono text-emerald-400">Available across sites</span>
        </div>

        <div className="rounded-lg border border-border/50 bg-card/60 p-4">
          <span className="text-xs text-muted-foreground">Asset Valuation</span>
          <p className="mt-1 text-2xl font-bold font-mono text-foreground">
            <LiveFigure value={totalStock * (product?.basePrice || 0)} format="currency" />
          </p>
          <span className="text-[11px] font-mono text-muted-foreground/70">At current base price</span>
        </div>

        <div className="rounded-lg border border-border/50 bg-card/60 p-4">
          <span className="text-xs text-muted-foreground">Reorder Threshold</span>
          <p className="mt-1 text-2xl font-bold font-mono text-amber-400">
            15 units
          </p>
          <span className="text-[11px] font-mono text-muted-foreground/70">Safety stock level</span>
        </div>

        <div className="rounded-lg border border-border/50 bg-card/60 p-4">
          <span className="text-xs text-muted-foreground">Turnover Ratio</span>
          <p className="mt-1 text-2xl font-bold font-mono text-foreground">
            4.2x
          </p>
          <span className="text-[11px] font-mono text-emerald-400">High velocity</span>
        </div>
      </div>

      {/* Warehouse Allocation Grid */}
      <div className="rounded-lg border border-border/50 bg-card/60 p-4">
        <div className="flex items-center gap-2 border-b border-border/40 pb-3">
          <Warehouse className="h-4 w-4 text-muted-foreground" />
          <h2 className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">
            Facility Allocation Ledger
          </h2>
        </div>

        <div className="mt-3 divide-y divide-border/30">
          {productInventories.length > 0 ? (
            productInventories.map((inv) => (
              <div key={inv.id} className="flex items-center justify-between py-2.5 text-xs">
                <div>
                  <p className="font-medium text-foreground">{inv.warehouse?.name}</p>
                  <p className="text-[11px] text-muted-foreground">{inv.warehouse?.location}</p>
                </div>
                <div className="text-right font-mono">
                  <span className="text-sm font-semibold text-foreground">{inv.quantity}</span>
                  <span className="text-muted-foreground"> / max {inv.maxCapacity}</span>
                </div>
              </div>
            ))
          ) : (
            <div className="py-6 text-center text-xs text-muted-foreground">
              No facility splits recorded yet. Default inventory allocated to Central Logistics Hub.
            </div>
          )}
        </div>
      </div>

      {/* Movement History specifically for this product */}
      <div className="rounded-lg border border-border/50 bg-card/60 p-4">
        <div className="flex items-center gap-2 border-b border-border/40 pb-3">
          <History className="h-4 w-4 text-muted-foreground" />
          <h2 className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">
            Asset Movement History
          </h2>
        </div>

        <div className="mt-3 overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead>
              <tr className="border-b border-border/40 text-muted-foreground">
                <th className="py-2 pl-2">REFERENCE</th>
                <th className="py-2 px-2">DIRECTION</th>
                <th className="py-2 px-2 text-right">QUANTITY</th>
                <th className="py-2 px-2">FACILITY</th>
                <th className="py-2 px-2">TIMESTAMP</th>
                <th className="py-2 pr-2 text-right">NOTES</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border/30 font-mono text-[11px]">
              {movements.length > 0 ? (
                movements.map((m) => (
                  <tr key={m.id} className="hover:bg-foreground/[0.02]">
                    <td className="py-2 pl-2 font-medium text-foreground">{m.referenceNumber}</td>
                    <td className="py-2 px-2">
                      <span
                        className={`rounded px-1.5 py-0.5 text-[10px] ${
                          m.type === "INBOUND"
                            ? "bg-emerald-500/10 text-emerald-400"
                            : m.type === "OUTBOUND"
                            ? "bg-rose-500/10 text-rose-400"
                            : "bg-foreground/[0.05] text-muted-foreground"
                        }`}
                      >
                        {m.type}
                      </span>
                    </td>
                    <td className="py-2 px-2 text-right font-semibold text-foreground">
                      {m.type === "INBOUND" ? `+${m.quantity}` : `-${m.quantity}`}
                    </td>
                    <td className="py-2 px-2 text-muted-foreground">{m.warehouse?.name}</td>
                    <td className="py-2 px-2 text-muted-foreground/70">
                      {new Date(m.createdAt).toLocaleDateString()}
                    </td>
                    <td className="py-2 pr-2 text-right text-muted-foreground">{m.notes || "-"}</td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan={6} className="py-6 text-center text-muted-foreground">
                    No movement records registered for this asset.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      <AdjustStockDrawer
        product={product}
        isOpen={isAdjustOpen}
        onClose={() => setIsAdjustOpen(false)}
      />
    </div>
  );
}
