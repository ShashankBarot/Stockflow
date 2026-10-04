"use client";

import * as React from "react";
import { Search, Bell, Sun, Moon, Building2, ChevronDown, Check } from "lucide-react";
import { ICON_BUTTON, FIELD } from "@/components/ui/control-classes";
import { useWarehouses } from "@/hooks/useWarehouses";

interface TopBarProps {
  onSearchChange?: (term: string) => void;
  searchValue?: string;
  selectedWarehouseId?: string;
  onSelectWarehouse?: (id: string | undefined) => void;
}

export function TopBar({
  onSearchChange,
  searchValue = "",
  selectedWarehouseId,
  onSelectWarehouse,
}: TopBarProps) {
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
    <header className="sticky top-0 z-30 flex h-14 w-full items-center justify-between border-b border-border/40 bg-background/80 px-4 md:px-6 backdrop-blur-md">
      {/* Left: Global Search Pill */}
      <div className="flex items-center gap-3">
        <div className="relative w-64 md:w-80">
          <Search className="pointer-events-none absolute left-2.5 top-1/2 h-3.5 w-3.5 -translate-y-1/2 text-muted-foreground/60" />
          <input
            type="text"
            value={searchValue}
            onChange={(e) => onSearchChange?.(e.target.value)}
            placeholder="Search SKU, product, warehouse..."
            className={`${FIELD} w-full pl-8 text-xs font-normal`}
          />
          <kbd className="pointer-events-none absolute right-2.5 top-1/2 -translate-y-1/2 rounded border border-border/60 bg-foreground/[0.04] px-1.5 py-0.5 text-[9px] font-mono font-medium text-muted-foreground">
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
        <button
          type="button"
          onClick={toggleTheme}
          className={ICON_BUTTON}
          aria-label="Toggle theme"
        >
          {theme === "dark" ? (
            <Sun className="h-3.5 w-3.5" />
          ) : (
            <Moon className="h-3.5 w-3.5" />
          )}
        </button>

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
        <div className="ml-2 flex items-center gap-2 border-l border-border/40 pl-3">
          <div className="flex h-7 w-7 items-center justify-center rounded-full bg-foreground/10 border border-border/50 text-xs font-semibold text-foreground">
            OP
          </div>
          <div className="hidden sm:block text-left">
            <p className="text-xs font-medium leading-none text-foreground">Ops Terminal</p>
            <p className="text-[10px] text-muted-foreground leading-tight">Admin Level 1</p>
          </div>
        </div>
      </div>
    </header>
  );
}
