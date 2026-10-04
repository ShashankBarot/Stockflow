import * as React from "react";
import { cva, type VariantProps } from "class-variance-authority";
import { cn } from "@/lib/utils";

/**
 * Badge — semantic status indicator.
 *
 * Variants map directly to domain states in this application:
 *   success   → In Stock
 *   warning   → Low Stock
 *   danger    → Out of Stock
 *   info      → Informational / Neutral movement
 *   default   → Pending / Neutral
 *   secondary → Muted / System
 *
 * Rules:
 * - No coloured dots before badge text.
 * - Use sentence case for badge labels, not ALL CAPS.
 * - Use only when the state is semantically meaningful, not decorative.
 */

const badgeVariants = cva(
  [
    "inline-flex items-center gap-1",
    "rounded-[--radius-sm] px-2 py-0.5",
    "text-xs font-medium leading-none",
    "whitespace-nowrap",
    "border",
  ].join(" "),
  {
    variants: {
      variant: {
        default: [
          "bg-[--color-recessed] text-[--color-muted]",
          "border-[--color-border]",
        ].join(" "),
        secondary: [
          "bg-[--color-recessed] text-[--color-subtle]",
          "border-transparent",
        ].join(" "),
        success: [
          "bg-[--color-success-bg] text-[--color-success-fg]",
          "border-[--color-success]/20",
        ].join(" "),
        warning: [
          "bg-[--color-warning-bg] text-[--color-warning-fg]",
          "border-[--color-warning]/20",
        ].join(" "),
        danger: [
          "bg-[--color-danger-bg] text-[--color-danger-fg]",
          "border-[--color-danger]/20",
        ].join(" "),
        info: [
          "bg-[--color-info-bg] text-[--color-info-fg]",
          "border-[--color-info]/20",
        ].join(" "),
      },
    },
    defaultVariants: {
      variant: "default",
    },
  }
);

export interface BadgeProps
  extends React.HTMLAttributes<HTMLSpanElement>,
    VariantProps<typeof badgeVariants> {}

function Badge({ className, variant, ...props }: BadgeProps) {
  return (
    <span className={cn(badgeVariants({ variant }), className)} {...props} />
  );
}

export { Badge, badgeVariants };

