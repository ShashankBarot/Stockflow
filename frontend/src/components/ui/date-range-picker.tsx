"use client";

import * as React from "react";
import { Calendar as CalendarIcon, ChevronDown } from "lucide-react";

export type DatePreset = "today" | "7d" | "30d" | "90d" | "ytd" | "all";

interface DateRangePickerProps {
  value: DatePreset;
  onChange: (value: DatePreset) => void;
  className?: string;
}

const PRESETS: { value: DatePreset; label: string }[] = [
  { value: "7d", label: "7D" },
  { value: "30d", label: "30D" },
  { value: "90d", label: "90D" },
  { value: "ytd", label: "YTD" },
  { value: "all", label: "ALL" },
];

export function DateRangePicker({
  value,
  onChange,
  className = "",
}: DateRangePickerProps) {
  const [open, setOpen] = React.useState(false);
  const containerRef = React.useRef<HTMLDivElement | null>(null);

  React.useEffect(() => {
    function handleClickOutside(e: MouseEvent) {
      if (containerRef.current && !containerRef.current.contains(e.target as Node)) {
        setOpen(false);
      }
    }
    if (open) {
      document.addEventListener("mousedown", handleClickOutside);
    }
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, [open]);

  const currentLabel = PRESETS.find((p) => p.value === value)?.label ?? value;

  return (
    <div className={`relative inline-block ${className}`} ref={containerRef}>
      <button
        type="button"
        onClick={() => setOpen(!open)}
        className="inline-flex items-center gap-1.5 rounded-md border border-border/50 bg-foreground/[0.04] px-2.5 py-1 text-xs font-medium text-foreground transition-colors hover:bg-foreground/[0.08]"
      >
        <CalendarIcon className="h-3.5 w-3.5 text-muted-foreground" />
        <span>{currentLabel}</span>
        <ChevronDown className="h-3 w-3 text-muted-foreground" />
      </button>

      {open && (
        <div className="absolute right-0 z-50 mt-1 flex w-32 flex-col gap-0.5 rounded-md border border-border/70 bg-popover p-1 shadow-md">
          {PRESETS.map((p) => (
            <button
              key={p.value}
              type="button"
              onClick={() => {
                onChange(p.value);
                setOpen(false);
              }}
              className={`rounded px-2.5 py-1.5 text-xs text-left transition-colors ${
                p.value === value
                  ? "bg-foreground text-background font-semibold"
                  : "text-foreground hover:bg-foreground/[0.08]"
              }`}
            >
              {p.label}
            </button>
          ))}
        </div>
      )}
    </div>
  );
}
