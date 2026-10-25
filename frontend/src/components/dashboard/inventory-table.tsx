"use client";

import * as React from "react";
import Link from "next/link";
import { ArrowUpDown, ExternalLink, Plus, Search } from "lucide-react";
import { LiveFigure } from "@/components/ui/live-figure";
import { SegmentedControl, SegmentOption } from "@/components/ui/segmented-control";
import { RowAction } from "@/components/ui/row-action";
import { BUTTON_PRIMARY, FIELD } from "@/components/ui/control-classes";
import type { Product } from "@/types";

export interface InventoryProduct extends Product {
  currentStock?: number;
  minStockLevel?: number;
  unitPrice?: number;
}

interface InventoryTableProps {
  products: InventoryProduct[];
  isLoading?: boolean;
  hasError?: boolean;
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
  hasError,
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
        const minStock = p.minStockLevel ?? 0;

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
    <section aria-label="Inventory records" className="flex min-w-0 flex-col">
      {/* Controls Bar */}
      <div className="flex flex-col items-stretch justify-between gap-3 border-b border-white/[0.07] pb-3 pt-1 sm:flex-row sm:items-center">
        <div className="flex min-w-0 flex-col gap-2 sm:flex-row sm:flex-wrap sm:items-center">
          <div className="relative w-full sm:w-auto">
            <Search className="pointer-events-none absolute left-2.5 top-1/2 h-3.5 w-3.5 -translate-y-1/2 text-muted-foreground/60" />
          <input
            type="text"
            placeholder="Search inventory..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className={`${FIELD} w-full pl-8 sm:w-48 lg:w-56`}
          />
          </div>
          <SegmentedControl
            options={STATUS_FILTERS}
            value={statusFilter}
            onChange={setStatusFilter}
            size="sm"
            className="max-w-full self-start"
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
      <div className="hidden overflow-x-auto md:block">
        <table className="w-full min-w-[760px] border-collapse text-left text-xs">
          <thead>
            <tr className="border-b border-white/[0.075] text-[10px] uppercase tracking-wide text-muted-foreground">
              <th className="py-2.5 pl-4 pr-3 font-medium">
                <button
                  type="button"
                  onClick={() => toggleSort("name")}
                    className="flex items-center gap-1.5 transition-colors hover:text-foreground"
                >
                  <span>Product</span>
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
                  <span>On hand</span>
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
                  <span>Unit price</span>
                  <ArrowUpDown className="h-3 w-3" />
                </button>
              </th>
              <th className="px-3 py-2.5 font-medium text-right">VALUATION</th>
              <th className="py-2.5 pl-3 pr-4 text-right font-medium">ACTIONS</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-white/[0.045] font-sans">
            {isLoading ? (
              <tr>
                <td colSpan={8} className="py-12 text-center text-muted-foreground">
                  Synchronizing inventory ledger...
                </td>
              </tr>
            ) : hasError ? (
              <tr><td colSpan={8} role="alert" className="py-12 text-center text-rose-300">Inventory data could not be loaded. Check the backend connection and sync again.</td></tr>
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
                    className="group transition-colors hover:bg-white/[0.025]"
                  >
                    {/* Item & SKU */}
                    <td className="py-2.5 pl-4 pr-3">
                      <div className="flex flex-col">
                        <Link
                          href={`/inventory/${p.id}`}
                          className="font-medium text-foreground/90 transition-colors hover:text-foreground"
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
              {p.category || "—"}
                    </td>

                    {/* Stock level */}
                    <td className="px-3 py-2.5 text-right font-mono font-medium">
                      <span
                        className={
                          isOut
                            ? "font-semibold text-[var(--negative)]"
                            : isLow
                            ? "font-medium text-[var(--warning)]"
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
                        <span className="inline-flex items-center gap-1 text-[10px] font-medium text-[var(--negative)]">
                          <span className="h-1.5 w-1.5 rounded-full bg-[var(--negative)]" />
                          Stockout
                        </span>
                      ) : isLow ? (
                        <span className="inline-flex items-center gap-1 text-[10px] font-medium text-[var(--warning)]">
                          <span className="h-1.5 w-1.5 rounded-full bg-[var(--warning)]" />
                          Low Stock
                        </span>
                      ) : (
                        <span className="inline-flex items-center gap-1 text-[10px] font-medium text-[var(--positive)]">
                          <span className="h-1.5 w-1.5 rounded-full bg-[var(--positive)]" />
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
                            className="rounded px-2 py-1 text-[11px] font-medium text-muted-foreground opacity-0 transition-all hover:bg-white/[0.06] hover:text-foreground focus-visible:opacity-100 group-hover:opacity-100"
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

      {/* Compact inventory rows for touch screens */}
      <div className="grid gap-2 py-3 md:hidden">
        {isLoading ? (
          <div className="py-10 text-center text-sm text-muted-foreground">
            Synchronizing inventory ledger...
          </div>
        ) : hasError ? (
          <div role="alert" className="py-10 text-center text-sm text-rose-300">Inventory data could not be loaded. Check the backend connection and sync again.</div>
        ) : filteredProducts.length === 0 ? (
          <div className="py-10 text-center text-sm text-muted-foreground">
            No matching inventory records found.
          </div>
        ) : (
          filteredProducts.map((product) => {
            const stock = product.currentStock ?? 0;
            const minimum = product.minStockLevel ?? 0;
            const price = product.unitPrice ?? product.basePrice ?? 0;
            const isOut = stock <= 0;
            const isLow = !isOut && stock <= minimum;

            return (
              <article
                key={product.id}
                className="rounded-md border border-white/[0.07] bg-foreground/[0.02] p-3"
              >
                <div className="flex min-w-0 items-start justify-between gap-3">
                  <div className="min-w-0">
                    <Link
                      href={`/inventory/${product.id}`}
                      className="block truncate text-sm font-medium text-foreground hover:underline"
                    >
                      {product.name}
                    </Link>
                    <p className="mt-0.5 truncate font-mono text-[10px] text-muted-foreground">
                      {product.sku} <span aria-hidden="true">·</span> {product.category || "—"}
                    </p>
                  </div>
                  <span
                    className={`shrink-0 rounded-sm px-1.5 py-1 text-[10px] font-medium ${
                      isOut
                        ? "bg-[var(--negative)]/10 text-[var(--negative)]"
                        : isLow
                        ? "bg-[var(--warning)]/10 text-[var(--warning)]"
                        : "bg-[var(--positive)]/10 text-[var(--positive)]"
                    }`}
                  >
                    {isOut ? "Depleted" : isLow ? "Low stock" : "Healthy"}
                  </span>
                </div>

                <dl className="mt-3 grid grid-cols-3 gap-2 border-t border-white/[0.06] pt-2.5">
                  <div>
                    <dt className="text-[9px] uppercase tracking-wide text-muted-foreground">On hand</dt>
                    <dd className="mt-0.5 font-mono text-xs font-medium tabular-nums text-foreground">
                      <LiveFigure value={stock} />
                      <span className="text-muted-foreground"> / </span>
                      <LiveFigure value={minimum} />
                    </dd>
                  </div>
                  <div>
                    <dt className="text-[9px] uppercase tracking-wide text-muted-foreground">Unit price</dt>
                    <dd className="mt-0.5 font-mono text-xs tabular-nums text-foreground">
                      <LiveFigure value={price} format="currency" />
                    </dd>
                  </div>
                  <div>
                    <dt className="text-[9px] uppercase tracking-wide text-muted-foreground">Value</dt>
                    <dd className="mt-0.5 font-mono text-xs font-medium tabular-nums text-foreground">
                      <LiveFigure value={stock * price} format="currency" />
                    </dd>
                  </div>
                </dl>

                <div className="mt-2.5 flex items-center justify-end gap-2">
                  {onAdjustStock && (
                    <button
                      type="button"
                      onClick={() => onAdjustStock(product)}
                      className="min-h-9 rounded px-3 text-xs font-medium text-muted-foreground transition-colors hover:bg-foreground/[0.06] hover:text-foreground"
                    >
                      Adjust stock
                    </button>
                  )}
                  <Link
                    href={`/inventory/${product.id}`}
                    className="inline-flex min-h-9 items-center rounded bg-foreground/[0.06] px-3 text-xs font-medium text-foreground transition-colors hover:bg-foreground/[0.1]"
                  >
                    View item
                  </Link>
                </div>
              </article>
            );
          })
        )}
      </div>

      {/* Table Footer Summary */}
      <div className="flex items-center justify-between border-t border-white/[0.07] px-1 py-2 text-[10px] font-mono text-muted-foreground">
        <span>Showing {filteredProducts.length} entries</span>
        <span>Operational Ledger Verified</span>
      </div>
    </section>
  );
}
