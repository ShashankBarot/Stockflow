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
        "px-6 py-4",
        className
      )}
    >
      {breadcrumb && (
        <div className="mb-2 text-xs text-[--color-muted]">{breadcrumb}</div>
      )}
      <div className="flex items-center justify-between gap-4 min-w-0">
        <div className="min-w-0">
          <h1 className="text-lg font-semibold tracking-tight text-[--color-ink] truncate">
            {title}
          </h1>
          {description && (
            <p className="mt-0.5 text-xs text-[--color-muted] truncate">
              {description}
            </p>
          )}
        </div>
        {actions && (
          <div className="flex items-center gap-2 shrink-0">{actions}</div>
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
      className={cn("flex-1 px-6 py-6 max-w-[1400px] mx-auto w-full", className)}
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

