"use client";

import * as React from "react";
import { LiveFigure } from "@/components/ui/live-figure";
import type { Product } from "@/types";

interface InventoryChartProduct extends Product {
  currentStock?: number;
}

interface InventoryChartProps {
  products: InventoryChartProduct[];
}

export function InventoryChart({ products }: InventoryChartProps) {
  const [hoverIndex, setHoverIndex] = React.useState<number | null>(null);
  const points = React.useMemo(
    () => products
      .map((product) => ({
        label: product.name,
        value: (product.currentStock ?? 0) * (product.basePrice ?? 0),
      }))
      .sort((a, b) => b.value - a.value)
      .slice(0, 8),
    [products]
  );
  const activePoint = hoverIndex !== null ? points[hoverIndex] : points[0];

  const width = 600;
  const height = 180;
  const paddingX = 20;
  const paddingY = 24;
  const maxValue = Math.max(...points.map((point) => point.value), 1) * 1.12;

  const getCoordinates = (index: number, value: number) => {
    const denominator = Math.max(points.length - 1, 1);
    const x = paddingX + (index / denominator) * (width - paddingX * 2);
    const y = height - paddingY - (value / maxValue) * (height - paddingY * 2);
    return { x, y };
  };

  const pathD = points.reduce((path, point, index) => {
    const { x, y } = getCoordinates(index, point.value);
    return index === 0 ? `M ${x} ${y}` : `${path} L ${x} ${y}`;
  }, "");
  const areaD = points.length
    ? `${pathD} L ${width - paddingX} ${height - paddingY} L ${paddingX} ${height - paddingY} Z`
    : "";

  return (
    <section aria-label="Current inventory value by product" className="flex flex-col border-b border-white/[0.07] pb-4">
      <div className="flex flex-wrap items-end justify-between gap-3 border-b border-border/40 pb-3">
        <div className="min-w-0">
          <span className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">
            Value by product
          </span>
          {activePoint ? (
            <>
              <h2 className="mt-1 truncate text-sm font-medium text-foreground">{activePoint.label}</h2>
              <p className="mt-0.5 font-mono text-xl font-medium tabular-nums text-foreground">
                <LiveFigure value={activePoint.value} format="currency" />
              </p>
            </>
          ) : (
            <p className="mt-2 text-sm text-muted-foreground">Add products and stock to see valuation.</p>
          )}
        </div>
        {points.length > 0 && (
          <span className="pb-1 text-[10px] font-mono text-muted-foreground/70">
            Top {points.length} items · current stock
          </span>
        )}
      </div>

      {points.length > 0 && (
        <div className="relative mt-4 h-48 w-full select-none">
          <svg
            viewBox={`0 0 ${width} ${height}`}
            className="h-full w-full overflow-visible"
            preserveAspectRatio="none"
            onPointerLeave={() => setHoverIndex(null)}
            onPointerMove={(event) => {
              const rect = event.currentTarget.getBoundingClientRect();
              const ratio = (event.clientX - rect.left) / rect.width;
              const index = Math.min(points.length - 1, Math.max(0, Math.round(ratio * points.length)));
              setHoverIndex(index);
            }}
          >
            <defs>
              <linearGradient id="inventoryValueGradient" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0%" stopColor="currentColor" stopOpacity="0.16" />
                <stop offset="100%" stopColor="currentColor" stopOpacity="0" />
              </linearGradient>
            </defs>

            {[paddingY, height / 2, height - paddingY].map((y) => (
              <line
                key={y}
                x1={paddingX}
                y1={y}
                x2={width - paddingX}
                y2={y}
                stroke="currentColor"
                strokeOpacity="0.06"
                strokeDasharray="3 3"
              />
            ))}

            <path d={areaD} fill="url(#inventoryValueGradient)" className="text-foreground" />
            <path d={pathD} fill="none" stroke="currentColor" strokeWidth="1.8" className="text-foreground" />

            {hoverIndex !== null && points[hoverIndex] && (() => {
              const { x, y } = getCoordinates(hoverIndex, points[hoverIndex].value);
              return (
                <g>
                  <line x1={x} y1={paddingY} x2={x} y2={height - paddingY} stroke="currentColor" strokeOpacity="0.3" strokeDasharray="2 2" />
                  <circle cx={x} cy={y} r="4.5" className="fill-foreground stroke-background" strokeWidth="2" />
                </g>
              );
            })()}
          </svg>

          <div className="mt-2 flex justify-between gap-2 px-2 text-[10px] font-mono text-muted-foreground/60">
            {points.map((point) => (
              <span key={point.label} className="min-w-0 truncate text-center" title={point.label}>
                {point.label}
              </span>
            ))}
          </div>
        </div>
      )}
    </section>
  );
}
