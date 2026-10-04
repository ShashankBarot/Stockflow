import * as React from "react";
import { cn } from "@/lib/utils";

/**
 * Skeleton — shimmer placeholder for loading states.
 *
 * Rules:
 * - Always match the EXACT dimensions of the content it replaces.
 * - No generic circular spinner for skeleton states.
 * - Compose multiple Skeleton elements to mirror the real layout.
 *
 * Usage:
 *   <Skeleton className="h-9 w-32" />         ← button skeleton
 *   <Skeleton className="h-4 w-48" />         ← text line skeleton
 *   <Skeleton className="h-full w-full" />    ← fills parent
 */
function Skeleton({
  className,
  ...props
}: React.HTMLAttributes<HTMLDivElement>) {
  return (
    <div
      className={cn("skeleton rounded-[--radius-md]", className)}
      aria-hidden="true"
      {...props}
    />
  );
}

/**
 * TableSkeleton — skeleton for a full data table.
 *
 * Usage:
 *   <TableSkeleton rows={5} columns={4} />
 */
function TableSkeleton({
  rows = 5,
  columns = 4,
}: {
  rows?: number;
  columns?: number;
}) {
  return (
    <div className="w-full" aria-label="Loading table data" aria-busy="true">
      {/* Header */}
      <div className="flex gap-4 px-4 py-3 border-b border-[--color-border] bg-[--color-recessed]">
        {Array.from({ length: columns }).map((_, i) => (
          <Skeleton key={i} className="h-3 flex-1" />
        ))}
      </div>
      {/* Rows */}
      {Array.from({ length: rows }).map((_, rowIdx) => (
        <div
          key={rowIdx}
          className="flex gap-4 px-4 py-3 border-b border-[--color-border]"
        >
          {Array.from({ length: columns }).map((_, colIdx) => (
            <Skeleton
              key={colIdx}
              className={cn(
                "h-4 flex-1",
                // Last column narrower (numeric column pattern)
                colIdx === columns - 1 && "max-w-[80px]"
              )}
            />
          ))}
        </div>
      ))}
    </div>
  );
}

/**
 * CardSkeleton — skeleton for a KPI metric card.
 */
function CardSkeleton() {
  return (
    <div
      className="rounded-[--radius-lg] border border-[--color-border] bg-[--color-surface] p-5 flex flex-col gap-3"
      aria-label="Loading"
      aria-busy="true"
    >
      <Skeleton className="h-3 w-24" />
      <Skeleton className="h-7 w-16" />
      <Skeleton className="h-3 w-32" />
    </div>
  );
}

export { Skeleton, TableSkeleton, CardSkeleton };

