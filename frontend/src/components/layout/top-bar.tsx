"use client";

import * as React from "react";
import { Search, Bell, Building2, ChevronDown, Check, LogOut } from "lucide-react";
import { ICON_BUTTON, FIELD } from "@/components/ui/control-classes";
import { useWarehouses } from "@/hooks/useWarehouses";
import { useAuth } from "@/hooks/useAuth";
import { useRouter } from "next/navigation";

interface TopBarProps {
  onSearchChange?: (term: string) => void;
  searchValue?: string;
  selectedWarehouseId?: string;
  onSelectWarehouse?: (id: string | undefined) => void;
  onLogout?: () => Promise<void>;
}

export function TopBar({
  onSearchChange,
  searchValue = "",
  selectedWarehouseId,
  onSelectWarehouse,
  onLogout,
}: TopBarProps) {
  const { user } = useAuth();
  const router = useRouter();
  const [theme, setTheme] = React.useState<"dark" | "light">("dark");
  const [whOpen, setWhOpen] = React.useState(false);
  const whRef = React.useRef<HTMLDivElement | null>(null);

  const { data: warehousesData } = useWarehouses();
  const warehouses = warehousesData?.data ?? [];

  React.useEffect(() => {
    const isDark = document.documentElement.classList.contains("dark");
    setTheme(isDark ? "dark" : "light");
  }, []);

  const toggleTheme = () => {
    const next = theme === "dark" ? "light" : "dark";
    setTheme(next);
    if (next === "dark") {
      document.documentElement.classList.add("dark");
    } else {
      document.documentElement.classList.remove("dark");
    }
  };

  React.useEffect(() => {
    function handleClickOutside(e: MouseEvent) {
      if (whRef.current && !whRef.current.contains(e.target as Node)) {
        setWhOpen(false);
      }
    }
    if (whOpen) {
      document.addEventListener("mousedown", handleClickOutside);
    }
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, [whOpen]);

  const activeWarehouse = warehouses.find((w) => String(w.id) === selectedWarehouseId);

  return (
    <header className="sticky top-0 z-30 flex h-14 w-full items-center justify-between border-b border-white/[0.055] bg-background/90 px-4 backdrop-blur-md md:px-6">
      {/* Left: Global Search Pill */}
      <div className="flex items-center gap-3">
        <div className="relative w-56 md:w-64">
          <Search className="pointer-events-none absolute left-2.5 top-1/2 h-3.5 w-3.5 -translate-y-1/2 text-muted-foreground/60" />
          <input
            type="text"
            value={searchValue}
            onChange={(e) => onSearchChange?.(e.target.value)}
            placeholder="Search products, SKUs, warehouses..."
            className={`${FIELD} w-full pl-8 text-xs font-normal`}
          />
          <kbd className="pointer-events-none absolute right-2.5 top-1/2 -translate-y-1/2 rounded border border-white/[0.08] bg-foreground/[0.035] px-1.5 py-0.5 text-[9px] font-mono font-medium text-muted-foreground">
            ⌘K
          </kbd>
        </div>
      </div>

      {/* Right Controls */}
      <div className="flex items-center gap-2">
        {/* Warehouse Selector */}
        <div className="relative" ref={whRef}>
          <button
            type="button"
            onClick={() => setWhOpen(!whOpen)}
            className="inline-flex h-8 items-center gap-1.5 rounded-md border border-border/50 bg-foreground/[0.03] px-2.5 text-xs font-medium text-foreground transition-colors hover:bg-foreground/[0.08]"
          >
            <Building2 className="h-3.5 w-3.5 text-muted-foreground" />
            <span className="max-w-[120px] truncate">
              {activeWarehouse ? activeWarehouse.name : "All Warehouses"}
            </span>
            <ChevronDown className="h-3 w-3 text-muted-foreground" />
          </button>

          {whOpen && (
            <div className="absolute right-0 z-50 mt-1 w-52 rounded-md border border-border/70 bg-popover p-1 shadow-md">
              <button
                type="button"
                onClick={() => {
                  onSelectWarehouse?.(undefined);
                  setWhOpen(false);
                }}
                className={`flex w-full items-center justify-between rounded px-2.5 py-1.5 text-xs text-left transition-colors ${
                  !selectedWarehouseId
                    ? "bg-foreground text-background font-semibold"
                    : "text-foreground hover:bg-foreground/[0.08]"
                }`}
              >
                <span>All Facilities</span>
                {!selectedWarehouseId && <Check className="h-3.5 w-3.5" />}
              </button>
              {warehouses.map((wh) => {
                const isSelected = String(wh.id) === selectedWarehouseId;
                return (
                  <button
                    key={wh.id}
                    type="button"
                    onClick={() => {
                      onSelectWarehouse?.(String(wh.id));
                      setWhOpen(false);
                    }}
                    className={`flex w-full items-center justify-between rounded px-2.5 py-1.5 text-xs text-left transition-colors ${
                      isSelected
                        ? "bg-foreground text-background font-semibold"
                        : "text-foreground hover:bg-foreground/[0.08]"
                    }`}
                  >
                    <span className="truncate">{wh.name}</span>
                    {isSelected && <Check className="h-3.5 w-3.5" />}
                  </button>
                );
              })}
            </div>
          )}
        </div>

        {/* Theme Toggle */}
        {/* Notifications */}
        <button
          type="button"
          className={`relative ${ICON_BUTTON}`}
          aria-label="System notifications"
        >
          <Bell className="h-3.5 w-3.5" />
          <span className="absolute right-1.5 top-1.5 h-1.5 w-1.5 rounded-full bg-emerald-500 ring-2 ring-background" />
        </button>

        {/* Operator Badge */}
        <div className="ml-1 flex items-center gap-2 border-l border-white/[0.07] pl-3">
          <div className="flex h-7 w-7 items-center justify-center rounded-full bg-foreground/[0.1] text-[10px] font-semibold text-foreground">
            {(user?.username ?? "OP").slice(0, 2).toUpperCase()}
          </div>
          <div className="hidden sm:block text-left">
            <p className="text-xs font-medium leading-none text-foreground">{user?.username ?? "Operations"}</p>
            <p className="text-[10px] capitalize text-muted-foreground leading-tight">{typeof user?.role === "string" ? user.role.toLowerCase() : user?.role.name.toLowerCase() ?? "Account"}</p>
          </div>
          <button type="button" aria-label="Sign out" title="Sign out" onClick={async () => { await onLogout?.(); router.replace("/login"); }} className="ml-1 rounded-md p-2 text-muted-foreground transition-colors hover:bg-foreground/[0.06] hover:text-foreground"><LogOut className="h-3.5 w-3.5" /></button>
        </div>
      </div>
    </header>
  );
}
