"use client";

import * as React from "react";
import { TrendingUp, AlertTriangle, AlertOctagon, PackageCheck } from "lucide-react";
import { LiveFigure } from "@/components/ui/live-figure";

export interface MetricItem {
  id: string;
  label: string;
  value: number;
  format?: "currency" | "integer" | "percent";
  change?: number; // e.g. +4.2%
  changeLabel?: string;
  sentiment?: "positive" | "warning" | "danger" | "neutral";
  icon?: React.ReactNode;
}

interface MetricsStripProps {
  metrics?: MetricItem[];
}

const DEFAULT_METRICS: MetricItem[] = [
  {
    id: "total_val",
    label: "Total Valuation",
    value: 128450,
    format: "currency",
    change: 3.4,
    changeLabel: "vs last month",
    sentiment: "positive",
    icon: <TrendingUp className="h-3.5 w-3.5" />,
  },
  {
    id: "units_stock",
    label: "Units in Stock",
    value: 4821,
    format: "integer",
    change: 1.8,
    changeLabel: "net inflow",
    sentiment: "positive",
    icon: <PackageCheck className="h-3.5 w-3.5" />,
  },
  {
    id: "low_stock",
    label: "Low Stock Items",
    value: 14,
    format: "integer",
    change: -2,
    changeLabel: "reorder needed",
    sentiment: "warning",
    icon: <AlertTriangle className="h-3.5 w-3.5 text-amber-500" />,
  },
  {
    id: "out_of_stock",
    label: "Depleted (Zero Qty)",
    value: 4,
    format: "integer",
    change: 1,
    changeLabel: "stockouts",
    sentiment: "danger",
    icon: <AlertOctagon className="h-3.5 w-3.5 text-rose-500" />,
  },
];

export function MetricsStrip({ metrics = DEFAULT_METRICS }: MetricsStripProps) {
  return (
    <section aria-label="Inventory metrics" className="grid grid-cols-2 border-y border-white/[0.07] py-3 lg:grid-cols-4">
      {metrics.map((m) => {
        const isWarning = m.sentiment === "warning";
        const isDanger = m.sentiment === "danger";

        return (
          <div
            key={m.id}
            className="flex min-w-0 flex-col justify-between px-3 py-1 first:pl-0 lg:border-r lg:border-white/[0.07] lg:px-4 lg:first:pl-0 lg:last:border-0"
          >
            <div className="flex items-center justify-between text-muted-foreground">
              <span className="text-[11px] font-medium text-muted-foreground">{m.label}</span>
            </div>

            <div className="mt-1.5 flex flex-wrap items-baseline justify-between gap-x-2">
              <span
                className={`text-[25px] font-medium tracking-tight tabular-nums ${
                  isDanger
                    ? "text-[var(--negative)]"
                    : isWarning
                    ? "text-[var(--warning)]"
                    : "text-foreground"
                }`}
              >
                <LiveFigure value={m.value} format={m.format} />
              </span>

              {m.change !== undefined && (
                <div className="flex items-center gap-1 text-[11px] font-mono">
                  <span
                    className={
                      m.change > 0 && !isDanger && !isWarning
                        ? "text-[var(--positive)]"
                        : isDanger || m.change < 0
                        ? "text-[var(--negative)]"
                        : "text-muted-foreground"
                    }
                  >
                    {m.change > 0 ? "+" : ""}
                    {m.change}%
                  </span>
                  <span className="hidden xl:inline text-muted-foreground/50">
                    {m.changeLabel}
                  </span>
                </div>
              )}
            </div>
          </div>
        );
      })}
    </section>
  );
}
