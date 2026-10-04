"use client";

import * as React from "react";
import { Plus, Download, RefreshCw } from "lucide-react";
import { InventoryTable } from "@/components/dashboard/inventory-table";
import { AdjustStockDrawer } from "@/components/inventory/adjust-stock-drawer";
import { CreateProductModal } from "@/components/inventory/create-product-modal";
import { BUTTON, BUTTON_PRIMARY } from "@/components/ui/control-classes";
import { useProducts, useInventory } from "@/hooks/useInventory";
import type { Product } from "@/types";

export default function InventoryPage() {
  const [adjustingProduct, setAdjustingProduct] = React.useState<Product | null>(null);
  const [isCreateOpen, setIsCreateOpen] = React.useState(false);

  const { data: productsData, isLoading, refetch } = useProducts(0, 50);
  const { data: inventoryData } = useInventory();

  const products: Product[] = React.useMemo(() => {
    const rawList = productsData?.data && productsData.data.length > 0 ? productsData.data : [];
    const invMap = new Map<number, number>();
    if (inventoryData?.data) {
      for (const item of inventoryData.data) {
        if (item.product?.id) {
          invMap.set(item.product.id, (invMap.get(item.product.id) || 0) + item.quantity);
        }
      }
    }

    return rawList.map((p) => {
      const stock = invMap.get(p.id) ?? 24;
      return {
        ...p,
        currentStock: stock,
        unitPrice: p.basePrice,
        minStockLevel: 10,
      } as Product & { currentStock: number; unitPrice: number; minStockLevel: number };
    });
  }, [productsData, inventoryData]);

  const handleExportCSV = () => {
    const headers = ["ID", "SKU", "Name", "Category", "Stock", "BasePrice"];
    const rows = products.map((p) => [
      p.id,
      `"${p.sku}"`,
      `"${p.name}"`,
      `"${p.category || ""}"`,
      (p as unknown as { currentStock?: number }).currentStock ?? 0,
      p.basePrice,
    ]);
    const csvContent =
      "data:text/csv;charset=utf-8," +
      [headers.join(","), ...rows.map((e) => e.join(","))].join("\n");
    const encodedUri = encodeURI(csvContent);
    const link = document.createElement("a");
    link.setAttribute("href", encodedUri);
    link.setAttribute("download", `inventory-ledger-${Date.now()}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between border-b border-border/40 pb-4">
        <div>
          <h1 className="text-xl font-bold tracking-tight text-foreground md:text-2xl">
            Inventory Assets
          </h1>
          <p className="mt-1 text-xs text-muted-foreground">
            Complete inventory catalog, stock levels, safety buffers, and unit valuations.
          </p>
        </div>

        <div className="flex items-center gap-2">
          <button type="button" onClick={() => refetch()} className={BUTTON}>
            <RefreshCw className="h-3.5 w-3.5" />
            <span>Sync</span>
          </button>
          <button type="button" onClick={handleExportCSV} className={BUTTON}>
            <Download className="h-3.5 w-3.5" />
            <span>Export CSV</span>
          </button>
          <button
            type="button"
            onClick={() => setIsCreateOpen(true)}
            className={BUTTON_PRIMARY}
          >
            <Plus className="h-3.5 w-3.5" />
            <span>Create Item</span>
          </button>
        </div>
      </div>

      <InventoryTable
        products={products}
        isLoading={isLoading}
        onAdjustStock={(prod) => setAdjustingProduct(prod)}
        onCreateProduct={() => setIsCreateOpen(true)}
      />

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
