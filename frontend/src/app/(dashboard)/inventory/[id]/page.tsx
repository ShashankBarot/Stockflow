"use client";

import * as React from "react";
import { useParams, useRouter } from "next/navigation";
import { ArrowLeft, ArrowUpDown, History, Warehouse, AlertCircle } from "lucide-react";
import { LiveFigure } from "@/components/ui/live-figure";
import { BUTTON, BUTTON_PRIMARY } from "@/components/ui/control-classes";
import { useProduct, useInventory } from "@/hooks/useInventory";
import { useMovements } from "@/hooks/useMovements";
import { AdjustStockDrawer } from "@/components/inventory/adjust-stock-drawer";
import type { Product } from "@/types";

export default function ProductDetailPage() {
  const params = useParams();
  const router = useRouter();
  const productId = Number(params?.id);

  const [isAdjustOpen, setIsAdjustOpen] = React.useState(false);

  const { data: productData, isLoading: isProdLoading, isError: isProductError } = useProduct(productId);
  const { data: inventoryData, isLoading: isInventoryLoading, isError: isInventoryError } = useInventory({ productId, page: 0, size: 500 });
  const { data: movementsData, isLoading: isMovementsLoading, isError: isMovementsError } = useMovements({ productId, page: 0, size: 500 });

  const product: Product | undefined = productData;

  // Filter inventory records for this product
  const productInventories = inventoryData?.data ?? [];

  const totalStock = productInventories.reduce((acc, inv) => acc + inv.quantity, 0);
  const reorderThreshold = productInventories.reduce((acc, inv) => acc + (inv.minThreshold ?? 0), 0);

  // Filter movements for this product
  const movements = movementsData?.data ?? [];

  if (isProdLoading) return <div className="py-16 text-center text-sm text-muted-foreground">Loading product record…</div>;
  if (isProductError || !product) return <div className="mx-auto max-w-lg rounded-lg border border-border/50 bg-card/60 p-6 text-center"><AlertCircle className="mx-auto h-5 w-5 text-amber-400" /><h1 className="mt-3 text-sm font-semibold text-foreground">Product not found</h1><p className="mt-1 text-xs text-muted-foreground">This product could not be loaded from StockFlow.</p><button type="button" onClick={() => router.push("/inventory")} className={`${BUTTON} mt-4`}><ArrowLeft className="h-3.5 w-3.5" />Back to inventory</button></div>;

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
                  Category: {product?.category || "—"} · Base Price: ${product?.basePrice?.toFixed(2)}
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
            {isInventoryLoading || isInventoryError ? "—" : <LiveFigure value={totalStock} />}
          </p>
          <span className="text-[11px] font-mono text-muted-foreground">Across registered facilities</span>
        </div>

        <div className="rounded-lg border border-border/50 bg-card/60 p-4">
          <span className="text-xs text-muted-foreground">Asset Valuation</span>
          <p className="mt-1 text-2xl font-bold font-mono text-foreground">
            {isInventoryLoading || isInventoryError ? "—" : <LiveFigure value={totalStock * product.basePrice} format="currency" />}
          </p>
          <span className="text-[11px] font-mono text-muted-foreground/70">At current base price</span>
        </div>

        <div className="rounded-lg border border-border/50 bg-card/60 p-4">
          <span className="text-xs text-muted-foreground">Reorder Threshold</span>
          <p className="mt-1 text-2xl font-bold font-mono text-amber-400">
            {isInventoryLoading || isInventoryError ? "—" : `${reorderThreshold} units`}
          </p>
          <span className="text-[11px] font-mono text-muted-foreground/70">Combined minimum across facilities</span>
        </div>

        <div className="rounded-lg border border-border/50 bg-card/60 p-4">
          <span className="text-xs text-muted-foreground">Movement Records</span>
          <p className="mt-1 text-2xl font-bold font-mono text-foreground">
            {isMovementsLoading || isMovementsError ? "—" : movementsData?.totalElements ?? movements.length}
          </p>
          <span className="text-[11px] font-mono text-muted-foreground/70">Recorded for this product</span>
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
                  {inv.maxCapacity != null && <span className="text-muted-foreground"> / max {inv.maxCapacity}</span>}
                </div>
              </div>
            ))
          ) : (
            <div className="py-6 text-center text-xs text-muted-foreground">
              {isInventoryLoading ? "Loading facility records…" : isInventoryError ? "Facility allocations could not be loaded." : "No inventory is currently assigned to a facility."}
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
              {isMovementsLoading ? (
                <tr><td colSpan={6} className="py-6 text-center text-muted-foreground">Loading movement history…</td></tr>
              ) : isMovementsError ? (
                <tr><td colSpan={6} role="alert" className="py-6 text-center text-rose-300">Movement history could not be loaded.</td></tr>
              ) : movements.length > 0 ? (
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
