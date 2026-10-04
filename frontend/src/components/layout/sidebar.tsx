"use client";

import * as React from "react";
import Link from "next/link";
import { usePathname } from "next/navigation";
import { useAuth } from "@/hooks/useAuth";
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
  roles?: string[];
}

export interface NavSectionConfig {
  title?: string;
  items: NavItemConfig[];
}

export const DEFAULT_NAV_SECTIONS: NavSectionConfig[] = [
  {
    title: "",
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
    title: "",
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
      {
        label: "Settings",
        href: "/settings",
        icon: <Settings className="h-4 w-4" />,
        roles: ["ADMIN"],
      },
    ],
  },
];

interface NavItemProps {
  item: NavItemConfig;
}

function NavItem({ item }: NavItemProps) {
  const pathname = usePathname();
  const { user } = useAuth();
  const role = typeof user?.role === "string" ? user.role : user?.role.name;
  if (item.roles && (!role || !item.roles.includes(role))) return null;
  const isActive =
    item.href === "/"
      ? pathname === "/"
      : pathname === item.href || pathname.startsWith(item.href + "/");

  return (
    <li>
      <Link
        href={item.href}
        className={`group flex items-center gap-2.5 rounded-full px-2.5 py-2 text-[13px] font-medium transition-colors duration-150 ${
          isActive
            ? "bg-foreground/[0.09] text-foreground"
            : "text-muted-foreground hover:bg-foreground/[0.045] hover:text-foreground"
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
      className={`sticky top-0 flex h-[100dvh] w-[196px] shrink-0 flex-col border-r border-white/[0.055] bg-background ${className}`}
      aria-label="Application navigation"
    >
      {/* Brand Header */}
      <div className="flex h-14 items-center gap-2.5 px-4">
        {header || (
          <div className="flex items-center gap-2">
            <div className="flex h-7 w-7 items-center justify-center rounded-md bg-foreground text-background">
              <Package className="h-4 w-4" />
            </div>
            <div>
              <span className="text-[13px] font-semibold tracking-tight text-foreground">
                Stockroom
              </span>
            </div>
          </div>
        )}
      </div>

      {/* Navigation Sections */}
      <nav className="flex-1 space-y-5 overflow-y-auto px-2.5 py-4">
        {sections.map((section, idx) => (
          <div key={idx} className="space-y-1">
            {section.title && (
            <p className="px-2 pb-1 text-[9px] font-semibold tracking-wider text-muted-foreground/50 uppercase">
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
      <div className="mt-auto p-3">
        {footer || (
          <div className="space-y-1">
            <div className="mt-2 flex items-center justify-between rounded-md px-2.5 py-1.5">
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
