"use client";

import * as React from "react";
import { motion } from "motion/react";

export interface SegmentOption<T extends string = string> {
  value: T;
  label: string;
}

interface SegmentedControlProps<T extends string = string> {
  options: SegmentOption<T>[];
  value: T;
  onChange: (value: T) => void;
  className?: string;
  size?: "sm" | "default";
}

export function SegmentedControl<T extends string = string>({
  options,
  value,
  onChange,
  className = "",
  size = "default",
}: SegmentedControlProps<T>) {
  const isSm = size === "sm";

  return (
    <div
      className={`relative inline-flex items-center rounded-md border border-border/50 bg-foreground/[0.04] p-0.5 ${className}`}
      role="tablist"
    >
      {options.map((opt) => {
        const isSelected = opt.value === value;
        return (
          <button
            key={opt.value}
            type="button"
            role="tab"
            aria-selected={isSelected}
            onClick={() => onChange(opt.value)}
            className={`relative z-10 font-medium transition-colors ${
              isSm ? "px-2 py-0.5 text-[11px]" : "px-3 py-1 text-xs"
            } ${
              isSelected
                ? "text-background font-semibold"
                : "text-muted-foreground hover:text-foreground"
            }`}
          >
            {opt.label}
            {isSelected && (
              <motion.div
                layoutId="segmented-thumb"
                className="absolute inset-0 -z-10 rounded bg-foreground shadow-sm"
                transition={{ type: "spring", stiffness: 450, damping: 35 }}
              />
            )}
          </button>
        );
      })}
    </div>
  );
}
