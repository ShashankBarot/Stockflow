"use client";

import * as React from "react";

interface LiveFigureProps {
  value: number;
  format?: "currency" | "integer" | "percent" | "decimal";
  prefix?: string;
  suffix?: string;
  className?: string;
}

export function LiveFigure({
  value,
  format = "integer",
  prefix = "",
  suffix = "",
  className = "",
}: LiveFigureProps) {
  const formatted = React.useMemo(() => {
    if (format === "currency") {
      return new Intl.NumberFormat("en-US", {
        style: "currency",
        currency: "USD",
        maximumFractionDigits: 0,
      }).format(value);
    }
    if (format === "percent") {
      return `${value > 0 ? "+" : ""}${value.toFixed(1)}%`;
    }
    if (format === "decimal") {
      return new Intl.NumberFormat("en-US", {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2,
      }).format(value);
    }
    return new Intl.NumberFormat("en-US").format(value);
  }, [value, format]);

  return (
    <span className={`font-mono tabular-nums tracking-tight ${className}`}>
      {prefix}
      {formatted}
      {suffix}
    </span>
  );
}
