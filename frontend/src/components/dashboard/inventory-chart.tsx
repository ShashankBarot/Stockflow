"use client";

import * as React from "react";
import { SegmentedControl, SegmentOption } from "@/components/ui/segmented-control";
import { LiveFigure } from "@/components/ui/live-figure";

type Timeframe = "7D" | "30D" | "90D" | "YTD";

const TIMEFRAME_OPTIONS: SegmentOption<Timeframe>[] = [
  { value: "7D", label: "7D" },
  { value: "30D", label: "30D" },
  { value: "90D", label: "90D" },
  { value: "YTD", label: "YTD" },
];

const DATA_POINTS: Record<Timeframe, { date: string; value: number; volume: number }[]> = {
  "7D": [
    { date: "Oct 1", value: 122400, volume: 140 },
    { date: "Oct 2", value: 123800, volume: 190 },
    { date: "Oct 3", value: 121900, volume: 85 },
    { date: "Oct 4", value: 125100, volume: 220 },
    { date: "Oct 5", value: 126400, volume: 160 },
    { date: "Oct 6", value: 127900, volume: 310 },
    { date: "Oct 7", value: 128450, volume: 280 },
  ],
  "30D": [
    { date: "Sep 8", value: 114000, volume: 320 },
    { date: "Sep 15", value: 118500, volume: 450 },
    { date: "Sep 22", value: 121000, volume: 390 },
    { date: "Sep 29", value: 125000, volume: 520 },
    { date: "Oct 7", value: 128450, volume: 480 },
  ],
  "90D": [
    { date: "Jul", value: 98000, volume: 1100 },
    { date: "Aug", value: 108000, volume: 1350 },
    { date: "Sep", value: 120000, volume: 1600 },
    { date: "Oct", value: 128450, volume: 1420 },
  ],
  "YTD": [
    { date: "Jan", value: 84000, volume: 2100 },
    { date: "Mar", value: 92000, volume: 2450 },
    { date: "May", value: 101000, volume: 2890 },
    { date: "Jul", value: 112000, volume: 3200 },
    { date: "Sep", value: 124000, volume: 3600 },
    { date: "Oct", value: 128450, volume: 3950 },
  ],
};

export function InventoryChart() {
  const [timeframe, setTimeframe] = React.useState<Timeframe>("30D");
  const [hoverIndex, setHoverIndex] = React.useState<number | null>(null);

  const points = DATA_POINTS[timeframe];
  const activePoint = hoverIndex !== null ? points[hoverIndex] : points[points.length - 1];

  // SVG Geometry
  const width = 600;
  const height = 180;
  const paddingX = 20;
  const paddingY = 24;

  const values = points.map((p) => p.value);
  const minVal = Math.min(...values) * 0.98;
  const maxVal = Math.max(...values) * 1.02;

  const getCoordinates = (idx: number, val: number) => {
    const x = paddingX + (idx / (points.length - 1)) * (width - paddingX * 2);
    const y = height - paddingY - ((val - minVal) / (maxVal - minVal || 1)) * (height - paddingY * 2);
    return { x, y };
  };

  const pathD = points.reduce((acc, curr, idx) => {
    const { x, y } = getCoordinates(idx, curr.value);
    return idx === 0 ? `M ${x} ${y}` : `${acc} L ${x} ${y}`;
  }, "");

  const areaD = `${pathD} L ${width - paddingX} ${height} L ${paddingX} ${height} Z`;

  return (
    <section aria-label="Inventory valuation" className="flex flex-col border-b border-white/[0.07] pb-3">
      {/* Header with live readout and timeframe controls */}
      <div className="flex flex-wrap items-center justify-between gap-3 border-b border-border/40 pb-3">
        <div>
          <div className="flex items-center gap-2">
            <span className="text-xs font-semibold tracking-wider text-muted-foreground uppercase">
              Inventory valuation
            </span>
            <span className="rounded bg-foreground/[0.05] px-1.5 py-0.5 font-mono text-[10px] text-muted-foreground">
              {activePoint.date}
            </span>
          </div>
          <div className="mt-1 flex items-baseline gap-2">
            <span className="text-[23px] font-medium tracking-tight tabular-nums text-foreground">
              <LiveFigure value={activePoint.value} format="currency" />
            </span>
            <span className="text-xs font-mono text-[var(--positive)]">
              +{(((activePoint.value - points[0].value) / points[0].value) * 100).toFixed(1)}%
            </span>
          </div>
        </div>

        <div className="flex items-center gap-2">
          <SegmentedControl
            options={TIMEFRAME_OPTIONS}
            value={timeframe}
            onChange={setTimeframe}
            size="sm"
          />
        </div>
      </div>

      {/* Interactive SVG Sparkline Canvas */}
      <div className="relative mt-4 h-48 w-full select-none">
        <svg
          viewBox={`0 0 ${width} ${height}`}
          className="h-full w-full overflow-visible"
          preserveAspectRatio="none"
          onMouseLeave={() => setHoverIndex(null)}
          onMouseMove={(e) => {
            const rect = e.currentTarget.getBoundingClientRect();
            const ratio = (e.clientX - rect.left) / rect.width;
            const idx = Math.min(
              points.length - 1,
              Math.max(0, Math.round(ratio * (points.length - 1)))
            );
            setHoverIndex(idx);
          }}
        >
          <defs>
            <linearGradient id="chartGradient" x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%" stopColor="currentColor" stopOpacity="0.16" />
              <stop offset="100%" stopColor="currentColor" stopOpacity="0.00" />
            </linearGradient>
          </defs>

          {/* Grid lines */}
          <line
            x1={paddingX}
            y1={paddingY}
            x2={width - paddingX}
            y2={paddingY}
            stroke="currentColor"
            strokeOpacity="0.06"
            strokeDasharray="3 3"
          />
          <line
            x1={paddingX}
            y1={height / 2}
            x2={width - paddingX}
            y2={height / 2}
            stroke="currentColor"
            strokeOpacity="0.06"
            strokeDasharray="3 3"
          />
          <line
            x1={paddingX}
            y1={height - paddingY}
            x2={width - paddingX}
            y2={height - paddingY}
            stroke="currentColor"
            strokeOpacity="0.06"
            strokeDasharray="3 3"
          />

          {/* Gradient Area Fill */}
          <path d={areaD} fill="url(#chartGradient)" className="text-foreground" />

          {/* Sparkline stroke */}
          <path
            d={pathD}
            fill="none"
            stroke="currentColor"
            strokeWidth="1.8"
            className="text-foreground"
          />

          {/* Active Hover Crosshair and Dot */}
          {hoverIndex !== null && (
            <g>
              {(() => {
                const { x, y } = getCoordinates(hoverIndex, points[hoverIndex].value);
                return (
                  <>
                    <line
                      x1={x}
                      y1={paddingY}
                      x2={x}
                      y2={height - paddingY}
                      stroke="currentColor"
                      strokeOpacity="0.3"
                      strokeWidth="1"
                      strokeDasharray="2 2"
                    />
                    <circle
                      cx={x}
                      cy={y}
                      r="4.5"
                      className="fill-foreground stroke-background"
                      strokeWidth="2"
                    />
                  </>
                );
              })()}
            </g>
          )}
        </svg>

        {/* X-Axis Labels */}
        <div className="mt-2 flex justify-between px-2 text-[10px] font-mono text-muted-foreground/60">
          {points.map((p, i) => (
            <span key={i}>{p.date}</span>
          ))}
        </div>
      </div>
    </section>
  );
}
