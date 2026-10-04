import * as React from "react";
import { cn } from "@/lib/utils";

/**
 * Table — design-system components.
 *
 * Composition:
 *   <Table>
 *     <TableHeader>
 *       <TableRow>
 *         <TableHead>Column</TableHead>
 *       </TableRow>
 *     </TableHeader>
 *     <TableBody>
 *       <TableRow>
 *         <TableCell>Value</TableCell>
 *         <TableCell numeric>123</TableCell>   ← tabular-nums + right-align
 *       </TableRow>
 *     </TableBody>
 *     <TableFooter>…</TableFooter>
 *   </Table>
 *
 * Rules:
 * - Numeric cells use font-mono + tabular-nums and are right-aligned.
 * - No zebra striping by default — use row hover only.
 * - No heavy borders — single bottom border between rows.
 * - Table is always full-width inside its container.
 * - Wrap in a <div className="overflow-x-auto"> for mobile.
 */

const Table = React.forwardRef<
  HTMLTableElement,
  React.HTMLAttributes<HTMLTableElement>
>(({ className, ...props }, ref) => (
  <div className="w-full overflow-x-auto">
    <table
      ref={ref}
      className={cn("w-full caption-bottom text-sm", className)}
      {...props}
    />
  </div>
));
Table.displayName = "Table";

const TableHeader = React.forwardRef<
  HTMLTableSectionElement,
  React.HTMLAttributes<HTMLTableSectionElement>
>(({ className, ...props }, ref) => (
  <thead
    ref={ref}
    className={cn(
      "border-b border-[--color-border]",
      "[&_tr]:border-0",
      className
    )}
    {...props}
  />
));
TableHeader.displayName = "TableHeader";

const TableBody = React.forwardRef<
  HTMLTableSectionElement,
  React.HTMLAttributes<HTMLTableSectionElement>
>(({ className, ...props }, ref) => (
  <tbody
    ref={ref}
    className={cn("[&_tr:last-child]:border-0", className)}
    {...props}
  />
));
TableBody.displayName = "TableBody";

const TableFooter = React.forwardRef<
  HTMLTableSectionElement,
  React.HTMLAttributes<HTMLTableSectionElement>
>(({ className, ...props }, ref) => (
  <tfoot
    ref={ref}
    className={cn(
      "border-t border-[--color-border]",
      "bg-[--color-recessed]",
      "font-medium",
      className
    )}
    {...props}
  />
));
TableFooter.displayName = "TableFooter";

const TableRow = React.forwardRef<
  HTMLTableRowElement,
  React.HTMLAttributes<HTMLTableRowElement>
>(({ className, ...props }, ref) => (
  <tr
    ref={ref}
    className={cn(
      "border-b border-[--color-border]",
      "transition-colors duration-100 ease-out",
      "hover:bg-[--color-recessed]",
      "data-[selected=true]:bg-[--color-accent-surface]",
      className
    )}
    {...props}
  />
));
TableRow.displayName = "TableRow";

const TableHead = React.forwardRef<
  HTMLTableCellElement,
  React.ThHTMLAttributes<HTMLTableCellElement>
>(({ className, ...props }, ref) => (
  <th
    ref={ref}
    className={cn(
      "h-9 px-4 text-left align-middle",
      "text-xs font-medium text-[--color-muted]",
      "tracking-wide uppercase",
      "whitespace-nowrap",
      "[&:has([role=checkbox])]:pr-0 [&>[role=checkbox]]:translate-y-[2px]",
      className
    )}
    {...props}
  />
));
TableHead.displayName = "TableHead";

interface TableCellProps extends React.TdHTMLAttributes<HTMLTableCellElement> {
  /** Applies monospace tabular-nums and right-align for numeric data. */
  numeric?: boolean;
}

const TableCell = React.forwardRef<HTMLTableCellElement, TableCellProps>(
  ({ className, numeric, ...props }, ref) => (
    <td
      ref={ref}
      className={cn(
        "px-4 py-3 align-middle",
        "text-sm text-[--color-ink]",
        "[&:has([role=checkbox])]:pr-0 [&>[role=checkbox]]:translate-y-[2px]",
        numeric && [
          "font-mono text-right tabular-nums",
          "text-[--color-ink]",
        ],
        className
      )}
      {...props}
    />
  )
);
TableCell.displayName = "TableCell";

const TableCaption = React.forwardRef<
  HTMLTableCaptionElement,
  React.HTMLAttributes<HTMLTableCaptionElement>
>(({ className, ...props }, ref) => (
  <caption
    ref={ref}
    className={cn(
      "mt-3 text-xs text-[--color-muted] text-left px-4",
      className
    )}
    {...props}
  />
));
TableCaption.displayName = "TableCaption";

export {
  Table,
  TableHeader,
  TableBody,
  TableFooter,
  TableHead,
  TableRow,
  TableCell,
  TableCaption,
};

