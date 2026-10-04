"use client";

import * as React from "react";
import { Download, FileSpreadsheet, FileText, CheckCircle2, TrendingUp, ShieldAlert, Calendar } from "lucide-react";
import { BUTTON, BUTTON_PRIMARY } from "@/components/ui/control-classes";
import { LiveFigure } from "@/components/ui/live-figure";
import { useProducts, useInventory } from "@/hooks/useInventory";
import { useMovements } from "@/hooks/useMovements";

export default function ReportsPage() {
  const { data: productsData } = useProducts(0, 100);
  const { data: inventoryData } = useInventory();
  const { data: movementsData } = useMovements();

  const handleExportAllCSV = () => {
    const products = productsData?.data ?? [];
    const headers = ["ID", "SKU", "Name", "Category", "BasePrice", "CreatedAt"];
    const rows = products.map((p) => [
      p.id,
      `"${p.sku}"`,
      `"${p.name}"`,
      `"${p.category || ""}"`,
      p.basePrice,
      `"${p.createdAt}"`,
    ]);
    const csvContent =
      "data:text/csv;charset=utf-8," +
      [headers.join(","), ...rows.map((e) => e.join(","))].join("\n");
    const encodedUri = encodeURI(csvContent);
    const link = document.createElement("a");
    link.setAttribute("href", encodedUri);
    link.setAttribute("download", `comprehensive-stock-audit-${Date.now()}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  const reports = [
    {
      title: "Monthly Inventory Valuation Audit",
      description: "Comprehensive breakdown of all SKUs, unit costs, extended inventory valuations, and facility balances.",
      frequency: "End of Month",
      records: productsData?.totalElements || 48,
      type: "Valuation",
    },
    {
      title: "Stock Movement & Dispatch Ledger",
      description: "Complete chronological audit trail of all receipts, customer fulfillments, inter-warehouse transfers, and write-offs.",
      frequency: "Weekly",
      records: movementsData?.totalElements || 124,
      type: "Operations",
    },
    {
      title: "Low Stock & Reorder Alert Log",
      description: "Items currently below safety buffer thresholds requiring purchase order issuance.",
      frequency: "Realtime",
      records: 14,
      type: "Replenishment",
    },
    {
      title: "Facility Storage Allocation Report",
      description: "Volumetric storage metrics and capacity utilization across all active warehouses.",
      frequency: "Bi-weekly",
      records: 3,
      type: "Facilities",
    },
  ];

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between border-b border-border/40 pb-4">
        <div>
          <h1 className="text-xl font-bold tracking-tight text-foreground md:text-2xl">
            Operational Reports & Audits
          </h1>
          <p className="mt-1 text-xs text-muted-foreground">
            Generate, inspect, and export compliance-ready inventory ledgers and valuation reports.
          </p>
        </div>

        <button type="button" onClick={handleExportAllCSV} className={BUTTON_PRIMARY}>
          <Download className="h-3.5 w-3.5" />
          <span>Export Master CSV</span>
        </button>
      </div>

      {/* Reports Grid */}
      <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
        {reports.map((r, idx) => (
          <div
            key={idx}
            className="flex flex-col justify-between rounded-lg border border-border/50 bg-card/60 p-4 transition-colors hover:border-border/80"
          >
            <div>
              <div className="flex items-center justify-between">
                <span className="rounded bg-foreground/[0.04] px-2 py-0.5 font-mono text-[10px] text-muted-foreground border border-border/40">
                  {r.type}
                </span>
                <span className="flex items-center gap-1 text-[11px] font-mono text-muted-foreground">
                  <Calendar className="h-3 w-3" />
                  {r.frequency}
                </span>
              </div>

              <h2 className="mt-2 text-sm font-semibold text-foreground">{r.title}</h2>
              <p className="mt-1 text-xs text-muted-foreground leading-relaxed">
                {r.description}
              </p>
            </div>

            <div className="mt-5 flex items-center justify-between border-t border-border/40 pt-3">
              <span className="font-mono text-[11px] text-muted-foreground">
                {r.records} rows available
              </span>

              <button
                type="button"
                onClick={handleExportAllCSV}
                className="inline-flex items-center gap-1.5 rounded px-2.5 py-1 text-xs font-medium text-foreground border border-border/50 bg-foreground/[0.03] hover:bg-foreground/[0.07]"
              >
                <Download className="h-3 w-3" />
                <span>Download CSV</span>
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
