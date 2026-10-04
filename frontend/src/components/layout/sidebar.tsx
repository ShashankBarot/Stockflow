"use client";

import * as React from "react";
import Link from "next/link";
import { usePathname } from "next/navigation";
import {
  LayoutDashboard,
  Boxes,
  ArrowLeftRight,
  Warehouse,
  BarChart3,
  Settings,
  ShieldCheck,
  Package,
} from "lucide-react";

export interface NavItemConfig {
  label: string;
  href: string;
  icon?: React.ReactNode;
  badge?: number;
}

export interface NavSectionConfig {
  title?: string;
  items: NavItemConfig[];
}

export const DEFAULT_NAV_SECTIONS: NavSectionConfig[] = [
  {
    title: "OPERATIONS",
    items: [
      {
        label: "Overview",
        href: "/",
        icon: <LayoutDashboard className="h-4 w-4" />,
      },
      {
        label: "Inventory",
        href: "/inventory",
        icon: <Boxes className="h-4 w-4" />,
      },
      {
        label: "Stock Movements",
        href: "/movements",
        icon: <ArrowLeftRight className="h-4 w-4" />,
      },
    ],
  },
  {
    title: "FACILITIES & INSIGHTS",
    items: [
      {
        label: "Warehouses",
        href: "/warehouses",
        icon: <Warehouse className="h-4 w-4" />,
      },
      {
        label: "Reports & Logs",
        href: "/reports",
        icon: <BarChart3 className="h-4 w-4" />,
      },
    ],
  },
];

interface NavItemProps {
  item: NavItemConfig;
}

function NavItem({ item }: NavItemProps) {
  const pathname = usePathname();
  const isActive =
    item.href === "/"
      ? pathname === "/"
      : pathname === item.href || pathname.startsWith(item.href + "/");

  return (
    <li>
      <Link
        href={item.href}
        className={`group flex items-center gap-2.5 rounded-md px-2.5 py-1.5 text-xs font-medium transition-all ${
          isActive
            ? "bg-foreground/[0.08] text-foreground border border-border/60 shadow-xs"
            : "text-muted-foreground hover:bg-foreground/[0.03] hover:text-foreground border border-transparent"
        }`}
        aria-current={isActive ? "page" : undefined}
      >
        <span
          className={`flex-shrink-0 transition-colors ${
            isActive ? "text-foreground" : "text-muted-foreground/70 group-hover:text-foreground"
          }`}
        >
          {item.icon}
        </span>
        <span className="flex-1 truncate">{item.label}</span>
        {item.badge != null && item.badge > 0 && (
          <span
            className={`flex h-4 min-w-4 items-center justify-center rounded-full px-1 text-[10px] font-mono tabular-nums ${
              isActive
                ? "bg-foreground text-background font-semibold"
                : "bg-foreground/[0.06] text-muted-foreground"
            }`}
          >
            {item.badge}
          </span>
        )}
      </Link>
    </li>
  );
}

export interface AppSidebarProps {
  sections?: NavSectionConfig[];
  header?: React.ReactNode;
  footer?: React.ReactNode;
  className?: string;
}

export function AppSidebar({
  sections = DEFAULT_NAV_SECTIONS,
  header,
  footer,
  className = "",
}: AppSidebarProps) {
  return (
    <aside
      className={`sticky top-0 flex h-[100dvh] w-60 shrink-0 flex-col border-r border-border/40 bg-card/40 backdrop-blur-md ${className}`}
      aria-label="Application navigation"
    >
      {/* Brand Header */}
      <div className="flex h-14 items-center gap-2.5 border-b border-border/40 px-4">
        {header || (
          <div className="flex items-center gap-2">
            <div className="flex h-7 w-7 items-center justify-center rounded-md bg-foreground text-background shadow-xs">
              <Package className="h-4 w-4" />
            </div>
            <div>
              <span className="text-sm font-semibold tracking-tight text-foreground">
                StockOS
              </span>
              <span className="ml-1.5 rounded bg-emerald-500/10 px-1 py-0.2 text-[9px] font-mono text-emerald-400 border border-emerald-500/20">
                PRO
              </span>
            </div>
          </div>
        )}
      </div>

      {/* Navigation Sections */}
      <nav className="flex-1 space-y-6 overflow-y-auto px-3 py-4">
        {sections.map((section, idx) => (
          <div key={idx} className="space-y-1">
            {section.title && (
              <p className="px-2 pb-1 text-[10px] font-semibold tracking-wider text-muted-foreground/60 uppercase">
                {section.title}
              </p>
            )}
            <ul className="space-y-0.5">
              {section.items.map((item) => (
                <NavItem key={item.href} item={item} />
              ))}
            </ul>
          </div>
        ))}
      </nav>

      {/* Fixed Footer */}
      <div className="border-t border-border/40 p-3">
        {footer || (
          <div className="space-y-1">
            <Link
              href="/settings"
              className="flex items-center gap-2.5 rounded-md px-2.5 py-1.5 text-xs font-medium text-muted-foreground transition-colors hover:bg-foreground/[0.04] hover:text-foreground"
            >
              <Settings className="h-4 w-4 text-muted-foreground/70" />
              <span>Settings</span>
            </Link>
            <div className="flex items-center justify-between rounded-md bg-foreground/[0.02] px-2.5 py-1.5 border border-border/30">
              <div className="flex items-center gap-1.5">
                <ShieldCheck className="h-3.5 w-3.5 text-emerald-500" />
                <span className="text-[11px] text-muted-foreground">Cluster Sync</span>
              </div>
              <span className="font-mono text-[10px] text-emerald-400">99.98%</span>
            </div>
          </div>
        )}
      </div>
    </aside>
  );
}
