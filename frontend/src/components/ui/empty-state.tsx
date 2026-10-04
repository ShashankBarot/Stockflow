import * as React from "react";
import { cn } from "@/lib/utils";

/**
 * Empty state — composed placeholder when a data view has no content.
 *
 * Rules:
 * - Never just "No data found" text alone.
 * - Always include a description of what can be done.
 * - Primary action optional (e.g., "Add first product").
 * - No generic icon — caller supplies a contextually appropriate one.
 *
 * Usage:
 *   import { PackageIcon } from "lucide-react";
 *   <EmptyState
 *     icon={<PackageIcon />}
 *     title="No products yet"
 *     description="Add your first product to start tracking inventory."
 *     action={<Button>Add product</Button>}
 *   />
 */

interface EmptyStateProps {
  icon?: React.ReactNode;
  title: string;
  description?: string;
  action?: React.ReactNode;
  className?: string;
}

function EmptyState({
  icon,
  title,
  description,
  action,
  className,
}: EmptyStateProps) {
  return (
    <div
      className={cn(
        "flex flex-col items-center justify-center",
        "py-16 px-6 text-center",
        className
      )}
      role="status"
    >
      {icon && (
        <div
          className="mb-4 text-[--color-subtle] [&>svg]:size-10"
          aria-hidden="true"
        >
          {icon}
        </div>
      )}
      <p className="text-sm font-medium text-[--color-ink] max-w-[32ch]">
        {title}
      </p>
      {description && (
        <p className="mt-1 text-xs text-[--color-muted] max-w-[40ch] leading-relaxed">
          {description}
        </p>
      )}
      {action && <div className="mt-5">{action}</div>}
    </div>
  );
}

/**
 * ErrorState — composed placeholder for failed data loads.
 *
 * Usage:
 *   <ErrorState
 *     title="Could not load inventory"
 *     description="Check your connection and try again."
 *     action={<Button variant="secondary" onClick={retry}>Retry</Button>}
 *   />
 */
interface ErrorStateProps {
  title: string;
  description?: string;
  action?: React.ReactNode;
  className?: string;
}

function ErrorState({
  title,
  description,
  action,
  className,
}: ErrorStateProps) {
  return (
    <div
      className={cn(
        "flex flex-col items-center justify-center",
        "py-16 px-6 text-center",
        className
      )}
      role="alert"
      aria-live="assertive"
    >
      {/* Simple X mark — no heavy icon library dependency */}
      <div
        className={cn(
          "mb-4 flex h-10 w-10 items-center justify-center",
          "rounded-full bg-[--color-danger-bg]"
        )}
        aria-hidden="true"
      >
        <svg
          width="20"
          height="20"
          viewBox="0 0 20 20"
          fill="none"
          className="text-[--color-danger]"
          aria-hidden="true"
        >
          <path
            d="M6 6l8 8M14 6l-8 8"
            stroke="currentColor"
            strokeWidth="1.75"
            strokeLinecap="round"
          />
        </svg>
      </div>
      <p className="text-sm font-medium text-[--color-ink] max-w-[32ch]">
        {title}
      </p>
      {description && (
        <p className="mt-1 text-xs text-[--color-muted] max-w-[40ch] leading-relaxed">
          {description}
        </p>
      )}
      {action && <div className="mt-5">{action}</div>}
    </div>
  );
}

/**
 * PageError — full page error boundary fallback.
 * Distinct from ErrorState: no centred layout assumption, self-contained.
 */
interface PageErrorProps {
  title?: string;
  description?: string;
  action?: React.ReactNode;
}

function PageError({
  title = "Something went wrong",
  description = "An unexpected error occurred. Reload the page to try again.",
  action,
}: PageErrorProps) {
  return (
    <div className="flex min-h-[60dvh] flex-col items-center justify-center px-6">
      <ErrorState title={title} description={description} action={action} />
    </div>
  );
}

export { EmptyState, ErrorState, PageError };

