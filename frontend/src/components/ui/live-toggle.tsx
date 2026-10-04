"use client";

import * as React from "react";

interface LiveToggleProps {
  enabled: boolean;
  onToggle: (state: boolean) => void;
  label?: string;
  className?: string;
}

export function LiveToggle({
  enabled,
  onToggle,
  label = "Live",
  className = "",
}: LiveToggleProps) {
  return (
    <button
      type="button"
      onClick={() => onToggle(!enabled)}
      className={`inline-flex items-center gap-2 rounded-md border border-border/50 bg-foreground/[0.03] px-2.5 py-1 text-xs font-medium text-foreground transition-all hover:bg-foreground/[0.06] ${className}`}
      aria-pressed={enabled}
    >
      <span className="relative flex h-2 w-2">
        {enabled && (
          <span className="absolute inline-flex h-full w-full animate-ping rounded-full bg-emerald-400 opacity-75" />
        )}
        <span
          className={`relative inline-flex h-2 w-2 rounded-full ${
            enabled ? "bg-emerald-500" : "bg-muted-foreground/40"
          }`}
        />
      </span>
      <span className="text-[11px] uppercase tracking-wider text-muted-foreground">
        {label}
      </span>
    </button>
  );
}
