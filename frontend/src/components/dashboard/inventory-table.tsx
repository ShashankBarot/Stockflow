"use client";

import * as React from "react";
import Link from "next/link";
import { SlidersHorizontal, ArrowUpDown, ExternalLink, Plus } from "lucide-react";
import { LiveFigure } from "@/components/ui/live-figure";
import { SegmentedControl, SegmentOption } from "@/components/ui/segmented-control";
import { RowAction } from "@/components/ui/row-action";
import { BUTTON, BUTTON_PRIMARY, FIELD } from "@/components/ui/control-classes";
import type { Product } from "@/types";

export interface InventoryProduct extends Product {
  currentStock?: number;
  minStockLevel?: number;
  unitPrice?: number;
}

interface InventoryTableProps {
  products: InventoryProduct[];
  isLoading?: boolean;
  onAdjustStock?: (product: Product) => void;
  onCreateProduct?: () => void;
}

type FilterStatus = "all" | "low" | "out" | "healthy";

const STATUS_FILTERS: SegmentOption<FilterStatus>[] = [
  { value: "all", label: "All Items" },
  { value: "low", label: "Low Stock" },
  { value: "out", label: "Depleted" },
  { value: "healthy", label: "Healthy" },
];

export function InventoryTable({
  products,
  isLoading,
  onAdjustStock,
  onCreateProduct,
}: InventoryTableProps) {
  const [search, setSearch] = React.useState("");
  const [statusFilter, setStatusFilter] = React.useState<FilterStatus>("all");
  const [sortField, setSortField] = React.useState<"name" | "stock" | "price">("stock");
  const [sortAsc, setSortAsc] = React.useState(false);

  const filteredProducts = React.useMemo(() => {
    return products
      .filter((p) => {
        const matchesSearch =
          p.name.toLowerCase().includes(search.toLowerCase()) ||
          p.sku.toLowerCase().includes(search.toLowerCase()) ||
          (p.category && p.category.toLowerCase().includes(search.toLowerCase()));

        if (!matchesSearch) return false;

        const currentStock = p.currentStock ?? 0;
        const minStock = p.minStockLevel ?? 10;

        if (statusFilter === "out") return currentStock <= 0;
        if (statusFilter === "low") return currentStock > 0 && currentStock <= minStock;
        if (statusFilter === "healthy") return currentStock > minStock;
        return true;
      })
      .sort((a, b) => {
        let diff = 0;
        if (sortField === "name") diff = a.name.localeCompare(b.name);
        else if (sortField === "stock") {
          const aStock = a.currentStock ?? 0;
          const bStock = b.currentStock ?? 0;
          diff = aStock - bStock;
        } else if (sortField === "price") {
          diff = (a.unitPrice ?? a.basePrice ?? 0) - (b.unitPrice ?? b.basePrice ?? 0);
        }
        return sortAsc ? diff : -diff;
      });
  }, [products, search, statusFilter, sortField, sortAsc]);

  const toggleSort = (field: "name" | "stock" | "price") => {
    if (sortField === field) {
      setSortAsc(!sortAsc);
    } else {
      setSortField(field);
      setSortAsc(false);
    }
  };

  return (
    <div className="flex flex-col rounded-lg border border-border/50 bg-card/60 backdrop-blur-xs">
      {/* Controls Bar */}
      <div className="flex flex-wrap items-center justify-between gap-3 border-b border-border/40 p-4">
        <div className="flex items-center gap-2">
          <input
            type="text"
            placeholder="Filter table rows..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className={`${FIELD} w-48 lg:w-64`}
          />
          <SegmentedControl
            options={STATUS_FILTERS}
            value={statusFilter}
            onChange={setStatusFilter}
            size="sm"
          />
        </div>

        <div className="flex items-center gap-2">
          {onCreateProduct && (
            <button
              type="button"
              onClick={onCreateProduct}
              className={BUTTON_PRIMARY}
            >
              <Plus className="h-3.5 w-3.5" />
              <span>Create Item</span>
            </button>
          )}
        </div>
      </div>

      {/* Dense Continuous Data Surface */}
      <div className="overflow-x-auto">
        <table className="w-full text-left text-xs border-collapse">
          <thead>
            <tr className="border-b border-border/40 bg-foreground/[0.02] text-muted-foreground">
              <th className="py-2.5 pl-4 pr-3 font-medium">
                <button
                  type="button"
                  onClick={() => toggleSort("name")}
                  className="flex items-center gap-1.5 hover:text-foreground"
                >
                  <span>PRODUCT & SKU</span>
                  <ArrowUpDown className="h-3 w-3" />
                </button>
              </th>
              <th className="px-3 py-2.5 font-medium">CATEGORY</th>
              <th className="px-3 py-2.5 font-medium text-right">
                <button
                  type="button"
                  onClick={() => toggleSort("stock")}
                  className="ml-auto flex items-center gap-1.5 hover:text-foreground"
                >
                  <span>ON HAND</span>
                  <ArrowUpDown className="h-3 w-3" />
                </button>
              </th>
              <th className="px-3 py-2.5 font-medium text-right">MIN REORDER</th>
              <th className="px-3 py-2.5 font-medium">STATUS</th>
              <th className="px-3 py-2.5 font-medium text-right">
                <button
                  type="button"
                  onClick={() => toggleSort("price")}
                  className="ml-auto flex items-center gap-1.5 hover:text-foreground"
                >
                  <span>UNIT PRICE</span>
                  <ArrowUpDown className="h-3 w-3" />
                </button>
              </th>
              <th className="px-3 py-2.5 font-medium text-right">VALUATION</th>
              <th className="py-2.5 pl-3 pr-4 text-right font-medium">ACTIONS</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-border/30 font-sans">
            {isLoading ? (
              <tr>
                <td colSpan={8} className="py-12 text-center text-muted-foreground">
                  Synchronizing inventory ledger...
                </td>
              </tr>
            ) : filteredProducts.length === 0 ? (
              <tr>
                <td colSpan={8} className="py-12 text-center text-muted-foreground">
                  No matching inventory records found.
                </td>
              </tr>
            ) : (
              filteredProducts.map((p) => {
                const stock = p.currentStock ?? 0;
                const min = p.minStockLevel ?? 10;
                const price = p.unitPrice ?? p.basePrice ?? 0;
                const valuation = stock * price;

                const isOut = stock <= 0;
                const isLow = !isOut && stock <= min;

                return (
                  <tr
                    key={p.id}
                    className="group transition-colors hover:bg-foreground/[0.03]"
                  >
                    {/* Item & SKU */}
                    <td className="py-2.5 pl-4 pr-3">
                      <div className="flex flex-col">
                        <Link
                          href={`/inventory/${p.id}`}
                          className="font-medium text-foreground hover:underline"
                        >
                          {p.name}
                        </Link>
                        <span className="font-mono text-[10px] text-muted-foreground">
                          {p.sku}
                        </span>
                      </div>
                    </td>

                    {/* Category */}
                    <td className="px-3 py-2.5 text-muted-foreground">
                      {p.category || "General"}
                    </td>

                    {/* Stock level */}
                    <td className="px-3 py-2.5 text-right font-mono font-medium">
                      <span
                        className={
                          isOut
                            ? "text-rose-400 font-bold"
                            : isLow
                            ? "text-amber-400 font-semibold"
                            : "text-foreground"
                        }
                      >
                        <LiveFigure value={stock} />
                      </span>
                    </td>

                    {/* Min level */}
                    <td className="px-3 py-2.5 text-right font-mono text-muted-foreground">
                      <LiveFigure value={min} />
                    </td>

                    {/* Restrained status tag */}
                    <td className="px-3 py-2.5">
                      {isOut ? (
                        <span className="inline-flex items-center gap-1 rounded bg-rose-500/10 px-1.5 py-0.5 text-[10px] font-medium text-rose-400 border border-rose-500/20">
                          <span className="h-1.5 w-1.5 rounded-full bg-rose-500" />
                          Stockout
                        </span>
                      ) : isLow ? (
                        <span className="inline-flex items-center gap-1 rounded bg-amber-500/10 px-1.5 py-0.5 text-[10px] font-medium text-amber-400 border border-amber-500/20">
                          <span className="h-1.5 w-1.5 rounded-full bg-amber-500" />
                          Low Stock
                        </span>
                      ) : (
                        <span className="inline-flex items-center gap-1 rounded bg-emerald-500/10 px-1.5 py-0.5 text-[10px] font-medium text-emerald-400 border border-emerald-500/20">
                          <span className="h-1.5 w-1.5 rounded-full bg-emerald-500" />
                          Optimal
                        </span>
                      )}
                    </td>

                    {/* Unit Price */}
                    <td className="px-3 py-2.5 text-right font-mono text-foreground/90">
                      <LiveFigure value={price} format="currency" />
                    </td>

                    {/* Valuation */}
                    <td className="px-3 py-2.5 text-right font-mono font-medium text-foreground">
                      <LiveFigure value={valuation} format="currency" />
                    </td>

                    {/* Actions */}
                    <td className="py-2.5 pl-3 pr-4 text-right">
                      <div className="flex items-center justify-end gap-1.5">
                        {onAdjustStock && (
                          <button
                            type="button"
                            onClick={() => onAdjustStock(p)}
                            className="rounded px-2 py-1 text-[11px] font-medium text-foreground border border-border/50 bg-foreground/[0.04] hover:bg-foreground/[0.08]"
                          >
                            Adjust
                          </button>
                        )}
                        <RowAction
                          items={[
                            {
                              label: "View Ledger",
                              onClick: () => {
                                window.location.href = `/inventory/${p.id}`;
                              },
                              icon: <ExternalLink className="h-3 w-3" />,
                            },
                            {
                              label: "Quick Adjust",
                              onClick: () => onAdjustStock?.(p),
                            },
                          ]}
                        />
                      </div>
                    </td>
                  </tr>
                );
              })
            )}
          </tbody>
        </table>
      </div>

      {/* Table Footer Summary */}
      <div className="flex items-center justify-between border-t border-border/40 px-4 py-2 text-[11px] font-mono text-muted-foreground">
        <span>Showing {filteredProducts.length} entries</span>
        <span>Operational Ledger Verified</span>
      </div>
    </div>
  );
}
