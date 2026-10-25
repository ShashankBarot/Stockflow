"use client";

import * as React from "react";
import { ArrowUpRight, ArrowDownRight, ArrowLeftRight, Warehouse, Activity, Zap } from "lucide-react";
import { LiveFigure } from "@/components/ui/live-figure";
import { useInventory } from "@/hooks/useInventory";
import { useMovements } from "@/hooks/useMovements";
import { useWarehouses } from "@/hooks/useWarehouses";

export function InsightPanel() {
  const { data: inventoryData } = useInventory({ page: 0, size: 100 });
  const { data: warehousesData } = useWarehouses();
  const { data: movementsData } = useMovements({ page: 0, size: 4 });
  const inventory = inventoryData?.data ?? [];
  const warehouseItems = warehousesData?.data ?? [];
  const movements = movementsData?.data ?? [];
  const optimalCount = inventory.filter((item) => item.quantity > item.minThreshold).length;
  const lowCount = inventory.filter((item) => item.quantity > 0 && item.quantity <= item.minThreshold).length;
  const criticalCount = inventory.filter((item) => item.quantity <= 0).length;
  const inventoryCount = inventory.length;
  const percentage = (count: number) => inventoryCount ? Math.round((count / inventoryCount) * 100) : 0;
  const healthyPercentage = percentage(optimalCount);
  const lowPercentage = percentage(lowCount);
  const criticalPercentage = percentage(criticalCount);

  const warehouses = warehouseItems.map((warehouse) => {
    const rows = inventory.filter((item) => item.warehouse?.id === warehouse.id);
    const current = rows.reduce((total, item) => total + item.quantity, 0);
    const capacity = rows.reduce((total, item) => total + (item.maxCapacity ?? 0), 0);
    return {
      id: warehouse.id,
      name: warehouse.name,
      current,
      capacity,
      usage: capacity > 0 ? Math.min(100, Math.round((current / capacity) * 100)) : 0,
    };
  });

  const topMovers = movements.map((movement) => ({
    id: movement.id,
    sku: movement.product?.sku ?? `MOV-${movement.id}`,
    name: movement.product?.name ?? "Unknown product",
    delta: `${movement.type === "INBOUND" ? "+" : movement.type === "OUTBOUND" ? "−" : ""}${movement.quantity} units`,
    dir: movement.type === "INBOUND" ? "in" : movement.type === "OUTBOUND" ? "out" : "transfer",
    rate: movement.type.toLowerCase(),
  }));

  return (
    <div className="flex flex-col gap-4 xl:sticky xl:top-[4.5rem]">
      {/* Stock Health Radar */}
      <section className="border-b border-white/[0.07] pb-4">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-1.5">
            <Activity className="h-3.5 w-3.5 text-muted-foreground" />
            <span className="text-xs font-semibold tracking-wider text-muted-foreground uppercase">
            Operational health
            </span>
          </div>
          <span className="font-mono text-sm font-medium text-[var(--positive)]">
            {inventoryCount ? `${healthyPercentage}%` : "—"}
          </span>
        </div>

        {/* Segmented bar */}
        <div className="mt-3 flex h-2 w-full overflow-hidden rounded-full bg-foreground/[0.05]">
          <div className="bg-[var(--positive)] transition-all" style={{ width: `${healthyPercentage}%` }} />
          <div className="bg-[var(--warning)] transition-all" style={{ width: `${lowPercentage}%` }} />
          <div className="bg-[var(--negative)] transition-all" style={{ width: `${criticalPercentage}%` }} />
        </div>

        <div className="mt-2.5 flex justify-between text-[10px] font-mono text-muted-foreground">
          <span className="flex items-center gap-1">
            <span className="h-1.5 w-1.5 rounded-full bg-[var(--positive)]" /> Optimal ({healthyPercentage}%)
          </span>
          <span className="flex items-center gap-1">
            <span className="h-1.5 w-1.5 rounded-full bg-[var(--warning)]" /> Low ({lowPercentage}%)
          </span>
          <span className="flex items-center gap-1">
            <span className="h-1.5 w-1.5 rounded-full bg-[var(--negative)]" /> Critical ({criticalPercentage}%)
          </span>
        </div>
      </section>

      {/* Facility Allocation */}
      <section className="border-b border-white/[0.07] pb-4">
        <div className="flex items-center justify-between pb-2">
          <div className="flex items-center gap-1.5">
            <Warehouse className="h-3.5 w-3.5 text-muted-foreground" />
            <span className="text-xs font-semibold tracking-wider text-muted-foreground uppercase">
              Warehouse utilization
            </span>
          </div>
          <span className="text-[11px] font-mono text-muted-foreground/70">{warehouseItems.length} sites</span>
        </div>

        <div className="mt-3 space-y-3">
          {warehouses.length === 0 ? (
            <p className="py-3 text-xs text-muted-foreground">No warehouse data yet.</p>
          ) : warehouses.map((wh) => (
            <div key={wh.id} className="space-y-1">
              <div className="flex items-center justify-between text-xs">
                <span className="truncate text-foreground/90 font-medium">{wh.name}</span>
                <span className="font-mono text-muted-foreground">{wh.capacity > 0 ? `${wh.usage}%` : "—"}</span>
              </div>
              {wh.capacity > 0 && <div className="h-1.5 w-full overflow-hidden rounded-full bg-foreground/[0.05]">
                <div
                  className={`h-full rounded-full transition-all ${
                    wh.usage > 90
                      ? "bg-[var(--negative)]"
                      : wh.usage > 75
                      ? "bg-[var(--warning)]"
                      : "bg-foreground/70"
                  }`}
                  style={{ width: `${wh.usage}%` }}
                />
              </div>}
              <div className="flex justify-between text-[10px] font-mono text-muted-foreground/60">
                <span><LiveFigure value={wh.current} /> units</span>
                <span>{wh.capacity > 0 ? <>Capacity <LiveFigure value={wh.capacity} /></> : "Capacity not set"}</span>
              </div>
            </div>
          ))}
        </div>
      </section>

      {/* High Velocity Movers */}
      <section className="pb-2">
        <div className="flex items-center justify-between pb-2">
          <div className="flex items-center gap-1.5">
            <Zap className="h-3.5 w-3.5 text-muted-foreground" />
            <span className="text-xs font-semibold tracking-wider text-muted-foreground uppercase">
              Recent movements
            </span>
          </div>
              <span className="text-[10px] font-mono text-muted-foreground/70">Latest records</span>
        </div>

        <div className="mt-2 divide-y divide-border/40">
          {topMovers.length === 0 ? (
            <p className="py-3 text-xs text-muted-foreground">No stock movements recorded yet.</p>
          ) : topMovers.map((m) => (
            <div key={m.id} className="flex items-center justify-between py-2 text-xs">
              <div className="min-w-0 pr-2">
                <div className="flex items-center gap-1.5">
                  <span className="font-mono text-[11px] font-medium text-foreground">{m.sku}</span>
                  <span className="rounded bg-foreground/[0.04] px-1 py-0.2 text-[9px] font-mono text-muted-foreground">
                    {m.rate}
                  </span>
                </div>
                <p className="truncate text-[11px] text-muted-foreground">{m.name}</p>
              </div>

              <div className="flex items-center gap-1 shrink-0 font-mono text-xs">
                {m.dir === "in" ? (
                  <ArrowUpRight className="h-3.5 w-3.5 text-emerald-400" />
                ) : m.dir === "out" ? (
                  <ArrowDownRight className="h-3.5 w-3.5 text-rose-400" />
                ) : (
                  <ArrowLeftRight className="h-3.5 w-3.5 text-muted-foreground" />
                )}
                  <span className={m.dir === "in" ? "text-[var(--positive)]" : "text-[var(--negative)]"}>
                  {m.delta}
                </span>
              </div>
            </div>
          ))}
        </div>
      </section>
    </div>
  );
}
