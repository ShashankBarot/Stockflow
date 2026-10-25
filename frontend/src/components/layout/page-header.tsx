import * as React from "react";
import { cn } from "@/lib/utils";

/**
 * PageHeader — sticky top bar for dashboard pages.
 *
 * Composition:
 *   <PageHeader
 *     title="Inventory"
 *     description="Manage stock levels across all warehouses."
 *     actions={<Button>Add product</Button>}
 *   />
 *
 * Rules:
 * - Title + description are left-aligned. Actions right-aligned.
 * - Max 1 primary action (accent-filled button).
 * - Sticky — remains visible as the page content scrolls.
 * - No large padding — max pt-4 to avoid floating content below.
 */

interface PageHeaderProps {
  title: string;
  description?: string;
  /** Primary and secondary action buttons */
  actions?: React.ReactNode;
  /** Optional breadcrumb or back-navigation */
  breadcrumb?: React.ReactNode;
  className?: string;
}

function PageHeader({
  title,
  description,
  actions,
  breadcrumb,
  className,
}: PageHeaderProps) {
  return (
    <header
      className={cn(
        "sticky top-0 z-30",
        "border-b border-[--color-border]",
        "bg-[--color-canvas]/95 backdrop-blur-sm",
        "px-3 py-3 sm:px-4 sm:py-4 md:px-6",
        className
      )}
    >
      {breadcrumb && (
        <div className="mb-2 text-xs text-[--color-muted]">{breadcrumb}</div>
      )}
      <div className="flex min-w-0 flex-col items-stretch justify-between gap-3 sm:flex-row sm:items-center sm:gap-4">
        <div className="min-w-0">
          <h1 className="truncate text-lg font-semibold tracking-tight text-[--color-ink]">
            {title}
          </h1>
          {description && (
            <p className="mt-0.5 truncate text-xs text-[--color-muted]">
              {description}
            </p>
          )}
        </div>
        {actions && (
          <div className="flex shrink-0 flex-wrap items-center gap-2">{actions}</div>
        )}
      </div>
    </header>
  );
}

/**
 * PageContent — main content area with consistent padding and max-width.
 */
function PageContent({
  className,
  children,
  ...props
}: React.HTMLAttributes<HTMLDivElement>) {
  return (
    <main
      className={cn("mx-auto flex w-full max-w-[1400px] flex-1 px-3 py-4 sm:px-4 sm:py-6 md:px-6", className)}
      {...props}
    >
      {children}
    </main>
  );
}

/**
 * Section — visually groups a block of content with an optional title.
 * Used inside PageContent to separate logical areas.
 */
interface SectionProps extends React.HTMLAttributes<HTMLDivElement> {
  title?: string;
  description?: string;
  actions?: React.ReactNode;
}

function Section({
  title,
  description,
  actions,
  children,
  className,
  ...props
}: SectionProps) {
  return (
    <section className={cn("flex flex-col gap-4", className)} {...props}>
      {(title || description || actions) && (
        <div className="flex items-start justify-between gap-4">
          <div className="flex flex-col gap-0.5">
            {title && (
              <h2 className="text-sm font-semibold text-[--color-ink]">
                {title}
              </h2>
            )}
            {description && (
              <p className="text-xs text-[--color-muted]">{description}</p>
            )}
          </div>
          {actions && <div className="flex items-center gap-2">{actions}</div>}
        </div>
      )}
      {children}
    </section>
  );
}

/**
 * Breadcrumb — simple text-link breadcrumb trail.
 * Renders as a nav with aria-label for accessibility.
 */
interface BreadcrumbItem {
  label: string;
  href?: string;
}

interface BreadcrumbProps {
  items: BreadcrumbItem[];
}

function Breadcrumb({ items }: BreadcrumbProps) {
  return (
    <nav aria-label="Breadcrumb">
      <ol className="flex items-center gap-1 text-xs text-[--color-muted]">
        {items.map((item, i) => (
          <React.Fragment key={i}>
            {i > 0 && (
              <li aria-hidden="true" className="select-none text-[--color-subtle]">
                /
              </li>
            )}
            <li>
              {item.href && i < items.length - 1 ? (
                <a
                  href={item.href}
                  className="hover:text-[--color-ink] transition-colors duration-100"
                >
                  {item.label}
                </a>
              ) : (
                <span
                  className={i === items.length - 1 ? "text-[--color-ink] font-medium" : ""}
                  aria-current={i === items.length - 1 ? "page" : undefined}
                >
                  {item.label}
                </span>
              )}
            </li>
          </React.Fragment>
        ))}
      </ol>
    </nav>
  );
}

export { PageHeader, PageContent, Section, Breadcrumb };

