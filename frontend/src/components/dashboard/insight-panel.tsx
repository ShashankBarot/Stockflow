"use client";

import * as React from "react";
import { ArrowUpRight, ArrowDownRight, Warehouse, Activity, Zap } from "lucide-react";
import { LiveFigure } from "@/components/ui/live-figure";

export function InsightPanel() {
  const warehouses = [
    { name: "Central Hub (ORD-1)", usage: 78, capacity: 5000, current: 3900 },
    { name: "West Coast Log (SFO-2)", usage: 64, capacity: 3200, current: 2048 },
    { name: "East Depository (JFK-4)", usage: 92, capacity: 2500, current: 2300 },
  ];

  const topMovers = [
    { sku: "SKU-9021", name: "Industrial Micro-Sensor", delta: "+142 units", dir: "in", rate: "Fast" },
    { sku: "SKU-1044", name: "Thermal Controller Mod", delta: "-88 units", dir: "out", rate: "High" },
    { sku: "SKU-3180", name: "Linear Actuator 12V", delta: "-64 units", dir: "out", rate: "Steady" },
    { sku: "SKU-7720", name: "Precision Cable Loom", delta: "+210 units", dir: "in", rate: "Surge" },
  ];

  return (
    <div className="flex flex-col gap-4">
      {/* Stock Health Radar */}
      <div className="rounded-lg border border-border/50 bg-card/60 p-4 backdrop-blur-xs">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-1.5">
            <Activity className="h-3.5 w-3.5 text-muted-foreground" />
            <span className="text-xs font-semibold tracking-wider text-muted-foreground uppercase">
              Operational Health
            </span>
          </div>
          <span className="font-mono text-xs font-semibold text-emerald-400">92.4%</span>
        </div>

        {/* Segmented bar */}
        <div className="mt-3 flex h-2 w-full overflow-hidden rounded-full bg-foreground/[0.05]">
          <div className="bg-emerald-500 transition-all" style={{ width: "84%" }} />
          <div className="bg-amber-500 transition-all" style={{ width: "12%" }} />
          <div className="bg-rose-500 transition-all" style={{ width: "4%" }} />
        </div>

        <div className="mt-2.5 flex justify-between text-[10px] font-mono text-muted-foreground">
          <span className="flex items-center gap-1">
            <span className="h-1.5 w-1.5 rounded-full bg-emerald-500" /> Optimal (84%)
          </span>
          <span className="flex items-center gap-1">
            <span className="h-1.5 w-1.5 rounded-full bg-amber-500" /> Low (12%)
          </span>
          <span className="flex items-center gap-1">
            <span className="h-1.5 w-1.5 rounded-full bg-rose-500" /> Critical (4%)
          </span>
        </div>
      </div>

      {/* Facility Allocation */}
      <div className="rounded-lg border border-border/50 bg-card/60 p-4 backdrop-blur-xs">
        <div className="flex items-center justify-between pb-2">
          <div className="flex items-center gap-1.5">
            <Warehouse className="h-3.5 w-3.5 text-muted-foreground" />
            <span className="text-xs font-semibold tracking-wider text-muted-foreground uppercase">
              Facility Utilization
            </span>
          </div>
          <span className="text-[11px] font-mono text-muted-foreground/70">3 Active Sites</span>
        </div>

        <div className="mt-3 space-y-3">
          {warehouses.map((wh) => (
            <div key={wh.name} className="space-y-1">
              <div className="flex items-center justify-between text-xs">
                <span className="truncate text-foreground/90 font-medium">{wh.name}</span>
                <span className="font-mono text-muted-foreground">{wh.usage}%</span>
              </div>
              <div className="h-1.5 w-full overflow-hidden rounded-full bg-foreground/[0.05]">
                <div
                  className={`h-full rounded-full transition-all ${
                    wh.usage > 90
                      ? "bg-rose-500"
                      : wh.usage > 75
                      ? "bg-amber-500"
                      : "bg-foreground/70"
                  }`}
                  style={{ width: `${wh.usage}%` }}
                />
              </div>
              <div className="flex justify-between text-[10px] font-mono text-muted-foreground/60">
                <span><LiveFigure value={wh.current} /> units</span>
                <span>Max <LiveFigure value={wh.capacity} /></span>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* High Velocity Movers */}
      <div className="rounded-lg border border-border/50 bg-card/60 p-4 backdrop-blur-xs">
        <div className="flex items-center justify-between pb-2">
          <div className="flex items-center gap-1.5">
            <Zap className="h-3.5 w-3.5 text-muted-foreground" />
            <span className="text-xs font-semibold tracking-wider text-muted-foreground uppercase">
              Velocity Feed
            </span>
          </div>
          <span className="text-[10px] font-mono text-muted-foreground/70">Last 24h</span>
        </div>

        <div className="mt-2 divide-y divide-border/40">
          {topMovers.map((m) => (
            <div key={m.sku} className="flex items-center justify-between py-2 text-xs">
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
                ) : (
                  <ArrowDownRight className="h-3.5 w-3.5 text-rose-400" />
                )}
                <span className={m.dir === "in" ? "text-emerald-400" : "text-rose-400"}>
                  {m.delta}
                </span>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
