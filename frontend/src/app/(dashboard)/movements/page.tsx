"use client";

import * as React from "react";
import { ArrowDownLeft, ArrowUpRight, ArrowLeftRight, SlidersHorizontal, RefreshCw, Download } from "lucide-react";
import { SegmentedControl, SegmentOption } from "@/components/ui/segmented-control";
import { LiveFigure } from "@/components/ui/live-figure";
import { BUTTON, FIELD } from "@/components/ui/control-classes";
import { useMovements } from "@/hooks/useMovements";
import type { StockMovement, MovementType } from "@/types";

type FilterType = "ALL" | MovementType;

const FILTER_OPTIONS: SegmentOption<FilterType>[] = [
  { value: "ALL", label: "All Movements" },
  { value: "INBOUND", label: "Inbound" },
  { value: "OUTBOUND", label: "Outbound" },
  { value: "TRANSFER", label: "Transfer" },
  { value: "ADJUSTMENT", label: "Adjustments" },
];

const FALLBACK_MOVEMENTS: StockMovement[] = [
  {
    id: 1,
    product: { id: 1, sku: "SKU-9021", name: "Industrial Micro-Sensor 24V", basePrice: 145, category: "Sensors", description: "", createdAt: "" },
    warehouse: { id: 1, name: "Central Hub (ORD-1)", location: "Chicago, IL", createdAt: "" },
    type: "INBOUND",
    quantity: 150,
    referenceNumber: "MOV-99210",
    performedBy: { id: 1, username: "admin", email: "admin@stockos.io", role: { id: 1, name: "Admin" }, createdAt: "" },
    notes: "Supplier batch delivery PO-4091 verified",
    createdAt: new Date(Date.now() - 1000 * 60 * 35).toISOString(),
  },
  {
    id: 2,
    product: { id: 2, sku: "SKU-1044", name: "Thermal Controller Module", basePrice: 280, category: "Controllers", description: "", createdAt: "" },
    warehouse: { id: 1, name: "Central Hub (ORD-1)", location: "Chicago, IL", createdAt: "" },
    type: "OUTBOUND",
    quantity: 24,
    referenceNumber: "MOV-99208",
    performedBy: { id: 1, username: "admin", email: "admin@stockos.io", role: { id: 1, name: "Admin" }, createdAt: "" },
    notes: "Customer dispatch order SO-8831",
    createdAt: new Date(Date.now() - 1000 * 60 * 120).toISOString(),
  },
  {
    id: 3,
    product: { id: 3, sku: "SKU-3180", name: "Linear Actuator 12V 100mm", basePrice: 95, category: "Actuators", description: "", createdAt: "" },
    warehouse: { id: 2, name: "West Coast Log (SFO-2)", location: "San Francisco, CA", createdAt: "" },
    type: "TRANSFER",
    quantity: 40,
    referenceNumber: "MOV-99195",
    performedBy: { id: 1, username: "admin", email: "admin@stockos.io", role: { id: 1, name: "Admin" }, createdAt: "" },
    notes: "Rebalance transfer to East Depository",
    createdAt: new Date(Date.now() - 1000 * 60 * 300).toISOString(),
  },
  {
    id: 4,
    product: { id: 4, sku: "SKU-7720", name: "Precision Shielded Cable Loom", basePrice: 42.5, category: "Cables", description: "", createdAt: "" },
    warehouse: { id: 3, name: "East Depository (JFK-4)", location: "New York, NY", createdAt: "" },
    type: "ADJUSTMENT",
    quantity: 12,
    referenceNumber: "MOV-99182",
    performedBy: { id: 1, username: "admin", email: "admin@stockos.io", role: { id: 1, name: "Admin" }, createdAt: "" },
    notes: "Bi-weekly physical cycle count correction",
    createdAt: new Date(Date.now() - 1000 * 60 * 600).toISOString(),
  },
];

export default function MovementsPage() {
  const [filterType, setFilterType] = React.useState<FilterType>("ALL");
  const [search, setSearch] = React.useState("");

  const { data: movementsData, isLoading, refetch } = useMovements();

  const movements: StockMovement[] = React.useMemo(() => {
    const raw = movementsData?.data && movementsData.data.length > 0 ? movementsData.data : FALLBACK_MOVEMENTS;
    return raw.filter((m) => {
      const matchesType = filterType === "ALL" || m.type === filterType;
      const matchesSearch =
        m.referenceNumber.toLowerCase().includes(search.toLowerCase()) ||
        m.product?.name.toLowerCase().includes(search.toLowerCase()) ||
        m.product?.sku.toLowerCase().includes(search.toLowerCase()) ||
        m.warehouse?.name.toLowerCase().includes(search.toLowerCase());
      return matchesType && matchesSearch;
    });
  }, [movementsData, filterType, search]);

  const stats = React.useMemo(() => {
    let inflow = 0;
    let outflow = 0;
    let transfers = 0;

    for (const m of movements) {
      if (m.type === "INBOUND") inflow += m.quantity;
      else if (m.type === "OUTBOUND") outflow += m.quantity;
      else if (m.type === "TRANSFER") transfers += m.quantity;
    }

    return { inflow, outflow, transfers, net: inflow - outflow };
  }, [movements]);

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between border-b border-border/40 pb-4">
        <div>
          <h1 className="text-xl font-bold tracking-tight text-foreground md:text-2xl">
            Stock Movements Ledger
          </h1>
          <p className="mt-1 text-xs text-muted-foreground">
            Immutable audit record of all physical item inflows, dispatches, transfers, and inventory reconciliations.
          </p>
        </div>

        <div className="flex items-center gap-2">
          <button type="button" onClick={() => refetch()} className={BUTTON}>
            <RefreshCw className="h-3.5 w-3.5" />
            <span>Sync Ledger</span>
          </button>
        </div>
      </div>

      {/* Movement Metrics Summary */}
      <div className="grid grid-cols-2 gap-3 lg:grid-cols-4">
        <div className="rounded-lg border border-border/50 bg-card/60 p-3.5">
          <span className="text-xs text-muted-foreground">Inbound Inflow</span>
          <p className="mt-1 text-xl font-bold font-mono text-emerald-400">
            +<LiveFigure value={stats.inflow} /> units
          </p>
          <span className="text-[10px] font-mono text-muted-foreground/60">Supplier deliveries</span>
        </div>

        <div className="rounded-lg border border-border/50 bg-card/60 p-3.5">
          <span className="text-xs text-muted-foreground">Outbound Dispatch</span>
          <p className="mt-1 text-xl font-bold font-mono text-rose-400">
            -<LiveFigure value={stats.outflow} /> units
          </p>
          <span className="text-[10px] font-mono text-muted-foreground/60">Customer orders</span>
        </div>

        <div className="rounded-lg border border-border/50 bg-card/60 p-3.5">
          <span className="text-xs text-muted-foreground">Net Stock Delta</span>
          <p
            className={`mt-1 text-xl font-bold font-mono ${
              stats.net >= 0 ? "text-emerald-400" : "text-rose-400"
            }`}
          >
            {stats.net >= 0 ? "+" : ""}
            <LiveFigure value={stats.net} /> units
          </p>
          <span className="text-[10px] font-mono text-muted-foreground/60">Net position change</span>
        </div>

        <div className="rounded-lg border border-border/50 bg-card/60 p-3.5">
          <span className="text-xs text-muted-foreground">Transfers Rebalanced</span>
          <p className="mt-1 text-xl font-bold font-mono text-foreground">
            <LiveFigure value={stats.transfers} /> units
          </p>
          <span className="text-[10px] font-mono text-muted-foreground/60">Inter-facility volume</span>
        </div>
      </div>

      {/* Dense Movement Table */}
      <div className="flex flex-col rounded-lg border border-border/50 bg-card/60 backdrop-blur-xs">
        <div className="flex flex-wrap items-center justify-between gap-3 border-b border-border/40 p-4">
          <div className="flex items-center gap-2">
            <input
              type="text"
              placeholder="Search reference, SKU, site..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              className={`${FIELD} w-48 lg:w-64`}
            />
            <SegmentedControl
              options={FILTER_OPTIONS}
              value={filterType}
              onChange={setFilterType}
              size="sm"
            />
          </div>

          <span className="font-mono text-xs text-muted-foreground">
            {movements.length} audit entries
          </span>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs border-collapse">
            <thead>
              <tr className="border-b border-border/40 bg-foreground/[0.02] text-muted-foreground">
                <th className="py-2.5 pl-4 pr-3 font-medium">REFERENCE</th>
                <th className="px-3 py-2.5 font-medium">TYPE</th>
                <th className="px-3 py-2.5 font-medium">PRODUCT</th>
                <th className="px-3 py-2.5 font-medium text-right">QUANTITY</th>
                <th className="px-3 py-2.5 font-medium">FACILITY</th>
                <th className="px-3 py-2.5 font-medium">OPERATOR</th>
                <th className="px-3 py-2.5 font-medium">TIMESTAMP</th>
                <th className="py-2.5 pl-3 pr-4 font-medium text-right">NOTES</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border/30">
              {isLoading ? (
                <tr>
                  <td colSpan={8} className="py-12 text-center text-muted-foreground">
                    Fetching audit ledger records...
                  </td>
                </tr>
              ) : movements.length === 0 ? (
                <tr>
                  <td colSpan={8} className="py-12 text-center text-muted-foreground">
                    No movements recorded matching current filters.
                  </td>
                </tr>
              ) : (
                movements.map((m) => (
                  <tr key={m.id} className="transition-colors hover:bg-foreground/[0.03]">
                    <td className="py-2.5 pl-4 pr-3 font-mono font-medium text-foreground">
                      {m.referenceNumber}
                    </td>

                    <td className="px-3 py-2.5">
                      <span
                        className={`inline-flex items-center gap-1 rounded px-2 py-0.5 text-[10px] font-mono font-medium border ${
                          m.type === "INBOUND"
                            ? "bg-emerald-500/10 text-emerald-400 border-emerald-500/20"
                            : m.type === "OUTBOUND"
                            ? "bg-rose-500/10 text-rose-400 border-rose-500/20"
                            : m.type === "TRANSFER"
                            ? "bg-blue-500/10 text-blue-400 border-blue-500/20"
                            : "bg-foreground/[0.05] text-muted-foreground border-border/40"
                        }`}
                      >
                        {m.type === "INBOUND" ? (
                          <ArrowDownLeft className="h-3 w-3" />
                        ) : m.type === "OUTBOUND" ? (
                          <ArrowUpRight className="h-3 w-3" />
                        ) : (
                          <ArrowLeftRight className="h-3 w-3" />
                        )}
                        {m.type}
                      </span>
                    </td>

                    <td className="px-3 py-2.5">
                      <div className="flex flex-col">
                        <span className="font-medium text-foreground">{m.product?.name}</span>
                        <span className="font-mono text-[10px] text-muted-foreground">
                          {m.product?.sku}
                        </span>
                      </div>
                    </td>

                    <td className="px-3 py-2.5 text-right font-mono font-semibold">
                      <span
                        className={
                          m.type === "INBOUND"
                            ? "text-emerald-400"
                            : m.type === "OUTBOUND"
                            ? "text-rose-400"
                            : "text-foreground"
                        }
                      >
                        {m.type === "INBOUND" ? "+" : m.type === "OUTBOUND" ? "-" : ""}
                        <LiveFigure value={m.quantity} />
                      </span>
                    </td>

                    <td className="px-3 py-2.5 text-muted-foreground font-mono text-[11px]">
                      {m.warehouse?.name}
                    </td>

                    <td className="px-3 py-2.5 text-muted-foreground">
                      {m.performedBy?.username || "Admin"}
                    </td>

                    <td className="px-3 py-2.5 font-mono text-[11px] text-muted-foreground/70">
                      {new Date(m.createdAt).toLocaleString(undefined, {
                        month: "short",
                        day: "numeric",
                        hour: "2-digit",
                        minute: "2-digit",
                      })}
                    </td>

                    <td className="py-2.5 pl-3 pr-4 text-right text-muted-foreground text-[11px] truncate max-w-[200px]">
                      {m.notes || "-"}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
